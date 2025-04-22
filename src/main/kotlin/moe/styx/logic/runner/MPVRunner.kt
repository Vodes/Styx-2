package moe.styx.logic.runner

import com.russhwolf.settings.get
import io.github.xxfast.kstore.extensions.getOrEmpty
import kotlinx.coroutines.*
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.files.getBlocking
import moe.styx.common.compose.http.Endpoints
import moe.styx.common.compose.http.login
import moe.styx.common.compose.settings
import moe.styx.common.compose.threads.RequestQueue
import moe.styx.common.compose.utils.MpvPreferences
import moe.styx.common.data.MediaEntry
import moe.styx.common.data.MediaWatched
import moe.styx.common.extension.currentUnixSeconds
import moe.styx.common.extension.eqI
import moe.styx.common.isWindows
import moe.styx.common.util.Log
import moe.styx.common.util.launchGlobal
import moe.styx.common.util.launchThreaded
import moe.styx.logic.Files
import java.io.*

var currentPlayer: MpvInstance? = null

data class MpvFinishStatus(val statusCode: Int, val message: String = "") {
    val isOK: Boolean = statusCode == 0
}

fun launchMPV(entry: MediaEntry, append: Boolean, onClose: (MpvFinishStatus) -> Unit = {}) {
    if (currentPlayer == null) {
        currentPlayer = MpvInstance()
        currentPlayer!!.start(entry, onClose) { processCode ->
            currentPlayer = null
            if (processCode > 0) {
                onClose(MpvFinishStatus(processCode, "Playback ended with a bad status code: $processCode"))
            } else {
                val currentEntry =
                    runBlocking { Storage.stores.entryStore.getOrEmpty() }.find { MpvStatus.current.file eqI it.GUID }
                if (MpvStatus.current.file.isNotBlank() && currentEntry != null && MpvStatus.current.seconds > 5) {
                    launchThreaded {
                        val watched = MediaWatched(
                            currentEntry.GUID,
                            login?.userID ?: "",
                            currentUnixSeconds(),
                            MpvStatus.current.seconds.toLong(),
                            MpvStatus.current.percentage.toFloat(),
                            MpvStatus.current.percentage.toFloat()
                        )
                        RequestQueue.updateWatched(watched).first.join()
                        onClose(MpvFinishStatus(0))
                    }
                }
            }
            MpvStatus.current = MpvStatus.current.copy(file = "", paused = true)
        }
    } else {
        val result = currentPlayer!!.play(entry, append)
        if (!result)
            onClose(MpvFinishStatus(-1, "Failed to add episode to the queue!"))
    }
}

class MpvInstance {
    private lateinit var process: Process
    private val tryFlatpak = settings["mpv-flatpak", false]
    private val instanceJob = Job()
    private var firstPrint = true
    var mpvSock = MPVSocket()
    var onClose: (MpvFinishStatus) -> Unit = {}

    fun createScope(): CoroutineScope {
        return CoroutineScope(instanceJob)
    }

    fun start(mediaEntry: MediaEntry, onClose: (MpvFinishStatus) -> Unit = {}, onFinish: (Int) -> Unit): Boolean {
        this.onClose = onClose
        val systemMpv = settings["mpv-system", !isWindows]
        val useConfigRegardless = settings["mpv-system-styx-conf", !isWindows]
        val mpvExecutable = if (systemMpv || !isWindows) {
            if (!isWindows && tryFlatpak)
                getExecutableFromPath("flatpak")
            else
                getExecutableFromPath("mpv")
        } else
            File(Files.getMpvDir(), "mpv.exe")

        if (mpvExecutable == null || !mpvExecutable.exists()) {
            onClose(MpvFinishStatus(404, "MPV executable not found!"))
            currentPlayer = null
            return false
        }
        val downloadedEntry = runBlocking { Storage.stores.downloadedStore.getOrEmpty() }.find { it.entryID eqI mediaEntry.GUID }
        val uri = downloadedEntry?.path ?: "${Endpoints.WATCH.url()}/${mediaEntry.GUID}?token=${login?.watchToken}"
        if ((login == null || login!!.watchToken.isBlank()) && downloadedEntry == null) {
            onClose(MpvFinishStatus(403, "You are not logged in or online and don't have this downloaded!"))
            currentPlayer = null
            return false
        }
        val pipe = if (isWindows) """--input-ipc-server=\\.\pipe\styx-mpvsocket""" else "--input-ipc-server=/tmp/styx-mpvsocket"
        val commands = if (!isWindows && tryFlatpak) mutableListOf(
            mpvExecutable.absolutePath,
            "run",
            "io.mpv.Mpv",
            uri,
            pipe,
            "--keep-open=yes",
            "--force-window=yes"
        ) else mutableListOf(mpvExecutable.absolutePath, uri, pipe, "--keep-open=yes")
        val pref = MpvPreferences.getOrDefault()
        commands.add("-slang=${pref.getSlangArg()}")
        commands.add("-alang=${pref.getAlangArg()}")
        if (useConfigRegardless || !systemMpv) {
            commands.add("--config-dir=${Files.getMpvConfDir().absolutePath}")
            commands.add("--profile=${pref.getPlatformProfile()}")
        }

        val watched = runBlocking { Storage.stores.watchedStore.getOrEmpty() }.find { it.entryID eqI mediaEntry.GUID }
        if (watched != null && watched.progress > 5)
            commands.add("--start=${watched.progress - 5}")

        createScope().launch {
            process = ProcessBuilder(commands).directory(mpvExecutable.parentFile)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE).start()

            launchThreaded {
                printLogs(process.inputStream)
            }
            launchThreaded {
                printLogs(process.errorStream)
            }
            launch {
                onFinish(process.waitFor())
                instanceJob.complete()
                mpvSock.closeAllTheThings()
            }
            launchGlobal {
                while (firstPrint || !MpvStatus.current.isAvailable())
                    delay(200)

                delay(4000)
                val startSet = mpvSock.command("set", 11, listOf("start", "0"), waitForResponse = true)
                if (!startSet)
                    Log.w("MPV") { "Could not reset start value!" }
            }

            launchGlobal {
                while (firstPrint)
                    delay(200)
                delay(500)
                var connected = false
                while (!connected && instanceJob.isActive) {
                    runCatching {
                        mpvSock.connect()
                        mpvSock.addListeners()
                        connected = true
                    }.onFailure {
                        Log.e(exception = it) { "Could not connect to mpv socket!" }
                    }
                    delay(5000L)
                }
                delay(1000L)
                while (currentPlayer != null) {
                    delay(100L)
                    if (mpvSock.rawDataMap.isNotEmpty()) {
                        MpvStatus.updateCurrent(mpvSock.rawDataMap)
                    }
                }
            }
        }
        return true
    }

    fun play(mediaEntry: MediaEntry, append: Boolean = true): Boolean {
        val current = MpvStatus.current.copy()
        val downloadedEntry = runBlocking { Storage.stores.downloadedStore.getOrEmpty() }.find { it.entryID eqI mediaEntry.GUID }
        val uri = downloadedEntry?.okioPath?.normalized()?.toString()?.replace("\\", "\\\\")
            ?: "${Endpoints.WATCH.url()}/${mediaEntry.GUID}?token=${login?.watchToken}"
        val loaded = mpvSock.command(
            "loadfile",
            35,
            mutableListOf(uri).apply {
                if (append)
                    add(if (current.percentage == 100 && current.eof) "append-play" else "append")
            },
            true
        )
        if (loaded) {
            if (current.percentage == 100 && (current.paused || current.eof))
                mpvSock.command("set", 0, listOf("pause", "no"))

            if (!append) {
                val watched = Storage.stores.watchedStore.getBlocking().find { it.entryID eqI mediaEntry.GUID }
                if (watched != null)
                    launchThreaded {
                        delay(100)
                        while (!MpvStatus.current.isAvailable() || !MpvStatus.current.file.equals(mediaEntry.GUID, true))
                            delay(350)
                        mpvSock.command("set", 0, listOf("pause", "yes"))
                        mpvSock.command("set_property", 0, listOf("playback-time", watched.progress - 5))
                    }
            }
        }

        return loaded
    }

    private fun printLogs(stream: InputStream) {
        var output = ""
        val inputStream = BufferedReader(InputStreamReader(stream))
        inputStream.use {
            while (inputStream.readLine()?.also { output = it.trim() } != null) {
                if (output.isBlank())
                    continue
                firstPrint = false
                if (!output.startsWith("{") || !output.endsWith("}")) {
                    if (!output.startsWith("AV") && !output.startsWith("(Paused)") && !output.startsWith("(...)"))
                        Log.d { "[MPV] - ${output.split("?token")[0]}" }
                }
            }
        }
    }
}


fun attemptPlayNext() {
    val entryList = runBlocking { Storage.stores.entryStore.getOrEmpty() }
    val mediaList = runBlocking { Storage.stores.mediaStore.getOrEmpty() }

    val entry = entryList.find { it.GUID == MpvStatus.current.file } ?: return
    val parentMedia = mediaList.find { it.GUID == entry.mediaID } ?: return

    val current = entry.entryNumber.toDoubleOrNull() ?: 0.0
    val entries =
        entryList.filter {
            val num = it.entryNumber.toDoubleOrNull() ?: 0.0
            it.mediaID == parentMedia.GUID && num > current
        }
    val next = entries.minByOrNull { it.entryNumber.toDoubleOrNull() ?: 9999.0 } ?: return

    if (currentPlayer == null || !currentPlayer!!.mpvSock.command(
            "show-text",
            0,
            listOf("Playing next episode in 5 seconds...", 1500),
            waitForResponse = true
        )
    )
        return

    currentPlayer!!.createScope().launch {
        delay(5000L)
        if (MpvStatus.current.eof)
            launchMPV(next, true)
    }
}