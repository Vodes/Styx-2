package moe.styx.logic.runner

import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.styx.common.isWindows
import moe.styx.common.json
import moe.styx.common.util.Log
import moe.styx.common.util.launchThreaded
import org.newsclub.net.unix.AFUNIXSocket
import org.newsclub.net.unix.AFUNIXSocketAddress
import java.io.BufferedReader
import java.io.File
import java.io.RandomAccessFile

class NamedPipeOrUnixSocket() {
    private val socket: AFUNIXSocket = AFUNIXSocket.newInstance()
    private var socketReader: BufferedReader? = null
    private lateinit var pipe: RandomAccessFile

    fun connect() {
        if (isWindows)
            pipe = RandomAccessFile("\\\\.\\pipe\\styx-mpvsocket", "rw")
        else {
            AFUNIXSocket.ensureSupported()
            val file = File("/tmp/styx-mpvsocket")
            socket.connect(AFUNIXSocketAddress.of(file))
        }
    }

    val isConnected: Boolean
        get() {
            return if (isWindows) true
            else socket.isConnected
        }

    val isClosed: Boolean
        get() {
            return if (isWindows) false
            else socket.isClosed
        }

    fun writeLine(line: String) {
        if (isWindows) {
            pipe.writeBytes(line.trim() + "\n")
        } else {
            val writer = socket.outputStream.writer()
            writer.appendLine(line.trim())
            writer.flush()
        }
    }

    fun readLine(): String? {
        return runCatching {
            if (isWindows)
                return pipe.readLine()
            else {
                if (socketReader == null)
                    socketReader = socket.inputStream.bufferedReader(Charsets.UTF_8)
                return socketReader!!.readLine()
            }

        }.getOrNull()
    }

    fun closeAllTheThings() {
        if (isWindows) {
            runCatching { pipe.close() }
            return
        }
        runCatching { socket.outputStream.close() }
        runCatching { socket.inputStream.close() }
        runCatching { socket.close() }
    }
}


class MPVSocket {
    private val sysSocket = NamedPipeOrUnixSocket()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val commandResponseQueue = mutableMapOf<Int, Boolean?>()

    val rawDataMap = mutableMapOf<String, String?>()

    fun connect() = sysSocket.connect()

    fun addListeners() = runBlocking {
        while (!sysSocket.isConnected || sysSocket.isClosed)
            delay(50)
        listOf(
            "pause",
            "path",
            "playback-time",
            "percent-pos",
            "eof-reached",
            "time-remaining",
            "playlist-current-pos",
            "playlist-count"
        ).forEachIndexed { idx, prop ->
            command("observe_property_string", "5$idx".toInt(), listOf(idx, prop))
        }
        processListeners()
    }

    fun rawCommand(cmd: String): Boolean {
        var success = true
        try {
            sysSocket.writeLine("""{ "command": $cmd }""")
        } catch (e: Exception) {
            Log.e("MPV", e) { "Failed to send raw command: $cmd" }
            success = false
        }
        return success
    }

    fun command(command: String, requestID: Int, params: List<Any> = emptyList(), waitForResponse: Boolean = false): Boolean {
        var success = true
        val paramsString = if (params.isEmpty()) "" else ", ${params.joinToString(", ") { if (it is String) "\"$it\"" else "$it" }}"
        val commandString = """{ "command": [ "$command" $paramsString ], "request_id": $requestID }"""
        try {
            sysSocket.writeLine(commandString)
            if (waitForResponse)
                commandResponseQueue[requestID] = null
        } catch (e: Exception) {
            Log.e("MPV", e) { "Failed to send command: $commandString" }
            success = false
        }
        if (!waitForResponse)
            return success

        var current: Boolean?
        runBlocking {
            while (commandResponseQueue.getOrDefault(requestID, null).also { current = it } == null) {
                delay(50)
                current = null
            }
        }
        commandResponseQueue.remove(requestID)
        return current!!
    }

    private fun processListeners() = launchThreaded {
        var line: String?
        while (sysSocket.readLine().also { line = it } != null && sysSocket.isConnected) {
            if (line!!.contains("property-change", ignoreCase = false)) {
                val parsed = json.decodeFromString<MPVEvent>(line!!)
                rawDataMap[parsed.name] = parsed.data
            }
            if (line!!.contains("request_id") && line!!.contains("error")) {
                val parsed = json.decodeFromString<MPVResponse>(line!!)
                if (commandResponseQueue.containsKey(parsed.requestID))
                    commandResponseQueue[parsed.requestID] = parsed.isOK()
            }
        }
    }

    fun closeAllTheThings() = sysSocket.closeAllTheThings()
}

@Serializable
data class MPVEvent(val event: String, val id: Int? = null, val name: String, val data: String? = null)

@Serializable
data class MPVResponse(@SerialName("request_id") val requestID: Int, val error: String) {
    fun isOK() = error == "success"
}