package moe.styx.logic.runner

import androidx.compose.runtime.getValue
import com.russhwolf.settings.get
import kotlinx.coroutines.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import moe.styx.logic.Endpoints
import moe.styx.logic.data.DataManager
import moe.styx.logic.login.login
import moe.styx.settings
import moe.styx.types.MediaEntry
import moe.styx.types.json
import java.io.*

var currentPlayer: MpvInstance? = null

fun launchMPV(entry: MediaEntry, append: Boolean, onFail: (String) -> Unit = {}) {
    if (currentPlayer == null) {
        currentPlayer = MpvInstance()
        currentPlayer!!.start(entry, onFail) {
            currentPlayer = null
            if (it > 0) {
                onFail("Playback ended with a bad status code:\n$it")
            }
            println("Playback ended: $it")
            MpvStatus.current = MpvStatus.current.copy(file = "", paused = true)
        }
    } else {
        val result = currentPlayer!!.play(entry, append)
        if (!result)
            onFail("Failed to add episode to the queue!")
    }
}

class MpvInstance {
    private lateinit var process: Process
    private val isWindows = System.getProperty("os.name").contains("win", true)
    private val tryFlatpak = settings["mpv-flatpak", false]
    private val instanceJob = Job()

    private fun openSocket(): RandomAccessFile {
        val socket = File(if (isWindows) "\\\\.\\pipe\\styx-mpvsocket" else "/tmp/styx-mpvsocket")
        return RandomAccessFile(socket, "rw")
    }

    fun runCommand(command: String): Boolean {
        try {
            val socket = openSocket()
            socket.write((command + "\n").toByteArray())
            return true
        } catch (_: Exception) {
        }
        return false
    }

    fun createScope(): CoroutineScope {
        return CoroutineScope(instanceJob)
    }

    fun start(mediaEntry: MediaEntry, onFail: (String) -> Unit = {}, onFinish: (Int) -> Unit = {}): Boolean {
        var mpvExecutable = getExecutableFromPath("mpv")
        if (!isWindows && tryFlatpak) {
            mpvExecutable = getExecutableFromPath("flatpak")
        }
        if (mpvExecutable == null) {
            onFail("MPV could not be found.")
            currentPlayer = null
            return false
        }
        if (login == null || login!!.watchToken.isBlank()) {
            onFail("You are not logged in right now.")
            currentPlayer = null
            return false
        }
        val url = "${Endpoints.WATCH.url()}/${mediaEntry.GUID}?token=${login!!.watchToken}"
        val pipe = if (isWindows) """--input-ipc-server=\\.\pipe\styx-mpvsocket""" else "--input-ipc-server=/tmp/styx-mpvsocket"
        val commands = if (!isWindows && tryFlatpak) listOf(
            mpvExecutable.absolutePath,
            "run",
            "io.mpv.Mpv",
            url,
            pipe,
            "--keep-open=yes"
        ) else listOf(mpvExecutable.absolutePath, url, pipe, "--keep-open=yes")
        createScope().launch {
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
                instanceJob.complete()
            }

            launch {
                delay(2000L)
                while (currentPlayer != null) {
                    val json = JsonObject(
                        mapOf(
                            "pos" to JsonPrimitive("\${playback-time}"),
                            "pos-percent" to JsonPrimitive("\${percent-pos}"),
                            "paused" to JsonPrimitive("\${pause}"),
                            "path" to JsonPrimitive("\${filename}"),
                            "playlist-count" to JsonPrimitive("\${playlist-count}"),
                            "playlist-current-pos" to JsonPrimitive("\${playlist-current-pos}"),
                            "eof" to JsonPrimitive("\${eof-reached}")
                        )
                    )
                    runCommand("print-text ${json.toString()}")
                    println(MpvStatus.current)
                    delay(1500L)
                }
            }
        }
        return true
    }

    fun play(mediaEntry: MediaEntry, append: Boolean = true): Boolean {
        val current = MpvStatus.current.copy()
        val url = "${Endpoints.WATCH.url()}/${mediaEntry.GUID}?token=${login!!.watchToken}"
        val appendType = if (current.percentage == 100 && current.eof) "append-play" else "append"
        val options = if (append) " $appendType" else ""
        val loaded = runCommand("loadfile \"$url\"$options")
        if (loaded && current.percentage == 100 && (current.paused || current.eof))
            runCommand("set pause no").also { return it }
        return false
    }

    private fun parseOutput(stream: InputStream) {
        var output = ""
        val inputStream = BufferedReader(InputStreamReader(stream))
        while (inputStream.readLine()?.also { output = it.trim() } != null) {
            if (!output.startsWith("{") || !output.endsWith("}"))
                continue
            try {
                MpvStatus.updateCurrent(output)
            } catch (_: Exception) {
            }
        }
        inputStream.close()
    }
}

data class MpvStatus(
    val pos: String,
    val percentage: Int,
    val file: String,
    val paused: Boolean,
    val playlistCurrent: Int,
    val playlistSize: Int,
    val eof: Boolean
) {
    companion object {
        var current = MpvStatus("00:00:00", 0, "", true, 0, 0, false)
        private var was100 = false

        fun updateCurrent(output: String) {
            val obj = json.decodeFromString<Map<String, String>>(output)

            var path = obj["path"]
            path = if (path.isNullOrBlank()) "" else
                if (path.contains("?")) path.split("?")[0] else path

            val percent = obj["pos-percent"]?.toIntOrNull() ?: 0
            val eof = obj["eof"]?.equals("yes") ?: false
            val playlistCurrent = obj["playlist-current-pos"]?.toIntOrNull() ?: 0
            val playlistSize = obj["playlist-count"]?.toIntOrNull() ?: 1

            var shouldAutoplay = percent == 100 && eof && playlistCurrent + 1 == playlistSize

            if (!was100 && shouldAutoplay && settings["mpv-play-next", true]) {
                was100 = true
                shouldAutoplay = true
            } else {
                shouldAutoplay = false
            }

            was100 = current.percentage == 100 && eof

            current = MpvStatus(
                obj["pos"] ?: "00:00:00",
                percent,
                path,
                obj["paused"]?.equals("yes") ?: true,
                playlistCurrent,
                playlistSize,
                eof
            )
            if (shouldAutoplay)
                attemptPlayNext()
        }
    }
}

fun attemptPlayNext() {
    val entryList by DataManager.entries
    val mediaList by DataManager.media

    val entry = entryList.find { it.GUID == MpvStatus.current.file } ?: return
    val parentMedia = mediaList.find { it.GUID == entry.mediaID } ?: return

    val current = entry.entryNumber.toDoubleOrNull() ?: 0.0
    val entries =
        entryList.filter {
            val num = it.entryNumber.toDoubleOrNull() ?: 0.0
            it.mediaID == parentMedia.GUID && num > current
        }
    val next = entries.minByOrNull { it.entryNumber.toDoubleOrNull() ?: 9999.0 } ?: return
    if (currentPlayer == null || !currentPlayer!!.runCommand("show-text \"Playing next episode in 5 seconds...\" 1500"))
        return

    currentPlayer!!.createScope().launch {
        delay(5000L)
        if (MpvStatus.current.eof)
            launchMPV(next, true)
    }
}