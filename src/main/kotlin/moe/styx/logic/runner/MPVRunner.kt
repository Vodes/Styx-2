package moe.styx.moe.styx.logic.runner

import com.russhwolf.settings.get
import kotlinx.coroutines.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import moe.styx.Endpoints
import moe.styx.json
import moe.styx.logic.login.login
import moe.styx.settings
import moe.styx.types.MediaEntry
import java.io.*

var currentPlayer: MpvInstance? = null

fun launchMPV(entry: MediaEntry, onFail: (String) -> Unit = {}) {
    if (currentPlayer == null) {
        currentPlayer = MpvInstance()
        currentPlayer!!.start(entry, onFail) { currentPlayer = null }
    } else {
        val result = currentPlayer!!.appendToQueue(entry)
        if (!result)
            onFail("Failed to add episode to the queue!")
    }
}

class MpvInstance {
    private lateinit var process: Process
    private val isWindows = System.getProperty("os.name").contains("win", true)
    private val tryFlatpak = settings["mpv-flatpak", false]

    @OptIn(DelicateCoroutinesApi::class)
    fun start(mediaEntry: MediaEntry, onFail: (String) -> Unit = {}, onFinish: (Int) -> Unit = {}): Boolean {
        var mpvExecutable = getExecutableFromPath(if (isWindows) "mpv.exe" else "mpv")
        if (!isWindows && tryFlatpak) {
            mpvExecutable = getExecutableFromPath("flatpak")
        }
        if (mpvExecutable == null) {
            onFail("MPV could not be found.")
            return false
        }
        val url = "${Endpoints.WATCH.url()}/${mediaEntry.GUID}?token=${login!!.watchToken}"
        val pipe = if (isWindows) """--input-ipc-server=\\.\pipe\styx-mpvsocket""" else "--input-ipc-server=/tmp/styx-mpvsocket"
        var commands = if (!isWindows && tryFlatpak) listOf(
            mpvExecutable.absolutePath,
            "run",
            "io.mpv.Mpv",
            url,
            pipe
        ) else listOf(mpvExecutable.absolutePath, url, pipe)
        GlobalScope.launch {
            process = ProcessBuilder(commands).directory(mpvExecutable.parentFile)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE).start()

            launch {
                parseOutput(process.inputStream)
            }
            launch {
                parseOutput(process.errorStream)
            }
            launch {
                onFinish(process.waitFor())
            }

            launch {
                delay(2000L)
                while (true) {
                    try {
                        val socket = File(if (isWindows) "\\\\.\\pipe\\styx-mpvsocket" else "/tmp/styx-mpvsocket")
                        val socketAccess = RandomAccessFile(socket, "rw")
                        val json = JsonObject(
                            mapOf(
                                "pos" to JsonPrimitive("\${playback-time}"),
                                "pos-percent" to JsonPrimitive("\${percent-pos}"),
                                "paused" to JsonPrimitive("\${pause}"),
                                "path" to JsonPrimitive("\${filename}")
                            )
                        )
                        socketAccess.write("print-text ${json.toString()}\n".toByteArray())
                    } catch (_: Exception) {
                    }
                    println(MpvStatus.current)
                    delay(1500L)
                }
            }
        }
        return true
    }

    fun appendToQueue(mediaEntry: MediaEntry): Boolean {
        try {
            val socket = File(if (isWindows) "\\\\.\\pipe\\styx-mpvsocket" else "/tmp/styx-mpvsocket")
            val socketAccess = RandomAccessFile(socket, "rw")
            val url = "${Endpoints.WATCH.url()}/${mediaEntry.GUID}?token=${login!!.watchToken}"
            socketAccess.write("loadfile \"$url\" append\n".toByteArray())
            return true
        } catch (_: Exception) {
        }
        return false
    }
}

data class MpvStatus(val pos: String, val percentage: Int, val file: String, val paused: Boolean) {
    companion object {
        var current = MpvStatus("00:00:00", 0, "", true)

        fun fromOut(output: String): MpvStatus {
            val obj = json.decodeFromString<Map<String, String>>(output)

            var path = obj["path"]
            path = if (path.isNullOrBlank()) "" else
                if (path.contains("?")) path.split("?")[0] else path

            return MpvStatus(
                obj["pos"] ?: "00:00:00",
                obj["pos-percent"]?.toInt() ?: 0,
                path,
                obj["paused"]?.equals("yes") ?: true
            )
        }
    }
}

fun parseOutput(stream: InputStream) {
    var output = ""
    val inputStream = BufferedReader(InputStreamReader(stream))
    while (inputStream.readLine()?.also { output = it.trim() } != null) {
        if (!output.startsWith("{") || !output.endsWith("}"))
            continue
        MpvStatus.current = MpvStatus.fromOut(output)
    }
    inputStream.close()
}