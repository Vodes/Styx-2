package moe.styx.logic.runner

import androidx.compose.runtime.getValue
import com.russhwolf.settings.get
import kotlinx.coroutines.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import moe.styx.Main.settings
import moe.styx.common.data.MediaEntry
import moe.styx.common.data.MediaWatched
import moe.styx.common.extension.currentUnixSeconds
import moe.styx.common.extension.eqI
import moe.styx.common.isWindows
import moe.styx.common.json
import moe.styx.common.util.launchThreaded
import moe.styx.logic.Endpoints
import moe.styx.logic.data.DataManager
import moe.styx.logic.login.login
import moe.styx.logic.loops.RequestQueue
import moe.styx.logic.utils.Log
import moe.styx.logic.utils.MpvUtils
import java.io.*

var currentPlayer: MpvInstance? = null

fun launchMPV(entry: MediaEntry, append: Boolean, onFail: (String) -> Unit = {}, execUpdate: () -> Unit = {}) {
    if (currentPlayer == null) {
        currentPlayer = MpvInstance()
        currentPlayer!!.start(entry, onFail, execUpdate) { processCode ->
            currentPlayer = null
            if (processCode > 0) {
                onFail("Playback ended with a bad status code:\n$processCode")
            } else {
                val currentEntry = DataManager.entries.value.find { MpvStatus.current.file eqI it.GUID }
                if (MpvStatus.current.file.isNotBlank() && currentEntry != null && MpvStatus.current.seconds > 5) {
                    val watched = MediaWatched(
                        currentEntry.GUID,
                        login?.userID ?: "",
                        currentUnixSeconds(),
                        MpvStatus.current.seconds.toLong(),
                        MpvStatus.current.percentage.toFloat(),
                        MpvStatus.current.percentage.toFloat()
                    )
                    RequestQueue.updateWatched(watched)
                    execUpdate()
                }
            }
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
    private val isWindows = isWindows()
    private val tryFlatpak = settings["mpv-flatpak", false]
    private val instanceJob = Job()
    private var firstPrint = true
    val execUpdate: () -> Unit = {}

    private fun openSocket(): RandomAccessFile {
        val socket = File(if (isWindows) "\\\\.\\pipe\\styx-mpvsocket" else "/tmp/styx-mpvsocket")
        return RandomAccessFile(socket, "rw")
    }

    fun runCommand(command: String): Boolean {
        runCatching {
            val socket = openSocket()
            socket.write((command + "\n").toByteArray())
            runCatching { socket.close() }
        }.onFailure { return false }
        return true
    }

    fun createScope(): CoroutineScope {
        return CoroutineScope(instanceJob)
    }

    fun start(mediaEntry: MediaEntry, onFail: (String) -> Unit = {}, execUpdate: () -> Unit = {}, onFinish: (Int) -> Unit = {}): Boolean {
        val systemMpv = settings["mpv-system", !isWindows]
        val useConfigRegardless = settings["mpv-system-styx-conf", false]
        val mpvExecutable = if (systemMpv || !isWindows) {
            if (!isWindows && tryFlatpak)
                getExecutableFromPath("flatpak")
            else
                getExecutableFromPath("mpv")
        } else
            File(DataManager.getMpvDir(), "mpv.exe")

        if (mpvExecutable == null || !mpvExecutable.exists()) {
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
        val commands = if (!isWindows && tryFlatpak) mutableListOf(
            mpvExecutable.absolutePath,
            "run",
            "io.mpv.Mpv",
            url,
            pipe,
            "--keep-open=yes"
        ) else mutableListOf(mpvExecutable.absolutePath, url, pipe, "--keep-open=yes")
        commands.add("-slang=${MpvUtils.getSlangArg()}")
        commands.add("-alang=${MpvUtils.getAlangArg()}")
        if (useConfigRegardless || !systemMpv) {
            commands.add("--config-dir=${DataManager.getMpvConfDir().absolutePath}")
            commands.add("--profile=${MpvUtils.getProfile()}")
        }

        val watched = DataManager.watched.value.find { it.entryID eqI mediaEntry.GUID }
        if (watched != null && watched.progress > 5)
            commands.add("--start=${watched.progress - 5}")

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
                while (firstPrint || !MpvStatus.current.isAvailable())
                    delay(200)
                delay(4000)
                runCommand("set start 0")
            }

            launch {
                while (firstPrint)
                    delay(200)
                delay(1000L)
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
                    delay(650L)
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
        if (loaded) {
            if (current.percentage == 100 && (current.paused || current.eof))
                runCommand("set pause no").also { return it }

            if (!append) {
                val watched = DataManager.watched.value.find { it.entryID eqI mediaEntry.GUID }
                if (watched != null)
                    launchThreaded {
                        delay(100)
                        while (!MpvStatus.current.isAvailable() || !MpvStatus.current.file.equals(mediaEntry.GUID, true))
                            delay(350)
                        runCommand("set pause yes")
                        runCommand("set playback-time ${watched.progress - 5}")
                    }
            }
        }

        return loaded
    }

    private fun parseOutput(stream: InputStream) {
        var output = ""
        val inputStream = BufferedReader(InputStreamReader(stream))
        while (inputStream.readLine()?.also { output = it.trim() } != null) {
            if (output.isBlank())
                continue
            firstPrint = false
            if (!output.startsWith("{") || !output.endsWith("}")) {
                if (!output.startsWith("AV") && !output.startsWith("(Paused)") && !output.startsWith("(...)"))
                    Log.d { "[MPV] - ${output.split("?token")[0]}" }
                continue
            }
            runCatching { MpvStatus.updateCurrent(output) }
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
            path = if (path.isNullOrBlank()) "" else path.split("?")[0]

            val percent = obj["pos-percent"]?.toIntOrNull() ?: -1
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

            val new = MpvStatus(
                obj["pos"] ?: "00:00:00",
                percent,
                path,
                obj["paused"]?.equals("yes") ?: true,
                playlistCurrent,
                playlistSize,
                eof
            )

            if (current.file.isNotBlank() && new.file.isNotBlank() && !current.file.trim().equals(new.file.trim(), true)) {
                val previousEntry = DataManager.entries.value.find { current.file eqI it.GUID }
                if (previousEntry != null && current.seconds > 5) {
                    val watched = MediaWatched(
                        previousEntry.GUID,
                        login?.userID ?: "",
                        currentUnixSeconds(),
                        current.seconds.toLong(),
                        current.percentage.toFloat(),
                        current.percentage.toFloat()
                    )
                    RequestQueue.updateWatched(watched)
                    currentPlayer?.let { it.execUpdate() }
                }
            }
            current = new
            if (shouldAutoplay)
                attemptPlayNext()
        }
    }

    val seconds: Int
        get() {
            return runCatching {
                val time = java.time.LocalTime.parse(pos)
                return time.hour * 3600 + time.minute * 60 + time.second
            }.getOrNull() ?: 0
        }

    fun isAvailable(): Boolean {
        return !(pos.contains("unavailable", true) || file.contains("unavailable", true))
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