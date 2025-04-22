package moe.styx.logic.runner

import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.styx.common.isWindows
import moe.styx.common.json
import moe.styx.common.util.Log
import moe.styx.common.util.launchThreaded
import moe.styx.logic.pipe.UnixPipe
import moe.styx.logic.pipe.WinPipe

class MPVSocket {
    private val sysSocket = if (isWindows) WinPipe("\\\\.\\pipe\\styx-mpvsocket") else UnixPipe("/tmp/styx-mpvsocket")
    private val scope = CoroutineScope(Dispatchers.IO)
    private val commandResponseQueue = mutableMapOf<Int, Boolean?>()

    val properties = mapOf(
        1 to "pause",
        2 to "path",
        3 to "playback-time",
        4 to "percent-pos",
        5 to "eof-reached",
        6 to "time-remaining",
        7 to "playlist-current-pos",
        8 to "playlist-count"
    )
    val rawDataMap = mutableMapOf<String, String?>()

    fun connect() = sysSocket.connect()

    fun addListeners() = runBlocking {
        while (!sysSocket.isConnected || sysSocket.isClosed)
            delay(50)
        properties.forEach { idx, prop ->
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
                var propName = parsed.name
                if (propName == null) {
                    if (parsed.id != null) {
                        propName = properties.getOrDefault(parsed.id, null)
                        if (propName == null)
                            continue
                    } else
                        continue
                }
                rawDataMap[propName] = parsed.data
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
data class MPVEvent(val event: String, val id: Int? = null, val name: String? = null, val data: String? = null)

@Serializable
data class MPVResponse(@SerialName("request_id") val requestID: Int, val error: String) {
    fun isOK() = error == "success"
}