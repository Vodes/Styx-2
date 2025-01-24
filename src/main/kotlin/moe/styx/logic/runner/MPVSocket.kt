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
import java.io.File

class MPVSocket {
    private val socket: AFUNIXSocket = AFUNIXSocket.newInstance()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val commandResponseQueue = mutableMapOf<Int, Boolean?>()

    val rawDataMap = mutableMapOf<String, String?>()

    fun connect() {
        AFUNIXSocket.ensureSupported()
        val file = if (isWindows)
            File("\\\\.\\pipe\\styx-mpvsocket")
        else
            File("/tmp/styx-mpvsocket")
        socket.connect(AFUNIXSocketAddress.of(file))
    }

    fun addListeners() = runBlocking {
        while (!socket.isConnected || socket.isClosed)
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
        val writer = socket.outputStream.writer()
        var success = true
        try {
            writer.appendLine("""{ "command": $cmd }""")
            writer.flush()
        } catch (e: Exception) {
            Log.e("MPV", e) { "Failed to send raw command: $cmd" }
            success = false
        }
        return success
    }

    fun command(command: String, requestID: Int, params: List<Any> = emptyList(), waitForResponse: Boolean = false): Boolean {
        val writer = socket.outputStream.writer()
        var success = true
        val paramsString = if (params.isEmpty()) "" else ", ${params.joinToString(", ") { if (it is String) "\"$it\"" else "$it" }}"
        val commandString = """{ "command": [ "$command" $paramsString ], "request_id": $requestID }"""
        try {
            writer.appendLine(commandString)
            writer.flush()
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
        val reader = socket.inputStream.bufferedReader(Charsets.UTF_8)
        var line: String
        while (reader.readLine().also { line = it } != null && socket.isConnected) {
            if (line.contains("property-change", ignoreCase = false)) {
                val parsed = json.decodeFromString<MPVEvent>(line)
                rawDataMap[parsed.name] = parsed.data
            }
            if (line.contains("request_id") && line.contains("error")) {
                val parsed = json.decodeFromString<MPVResponse>(line)
                if (commandResponseQueue.containsKey(parsed.requestID))
                    commandResponseQueue[parsed.requestID] = parsed.isOK()
            }
        }
        reader.close()
    }

    fun closeAllTheThings() {
        runCatching { socket.outputStream.close() }
        runCatching { socket.inputStream.close() }
        runCatching { socket.close() }
    }
}

@Serializable
data class MPVEvent(val event: String, val id: Int? = null, val name: String, val data: String? = null)

@Serializable
data class MPVResponse(@SerialName("request_id") val requestID: Int, val error: String) {
    fun isOK() = error == "success"
}