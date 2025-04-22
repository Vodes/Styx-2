package moe.styx.logic.pipe

import moe.styx.logic.Files
import okhttp3.internal.closeQuietly
import java.io.File

class WinPipe(val path: String) : Pipe() {
    var process: Process? = null
    val reader by lazy { process!!.inputStream.bufferedReader() }
    val writer by lazy { process!!.outputStream.bufferedWriter() }

    override fun connect() {
        val file = File(Files.getMpvDir(), "mpv-ipc-bridge.exe")
        if (!file.exists()) {
            throw Exception("MPV ipc bridge not found!")
        }
        process = ProcessBuilder(listOf(file.absolutePath, path))
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectInput(ProcessBuilder.Redirect.PIPE)
            .start()
    }

    override val isConnected: Boolean
        get() {
            return process?.isAlive == true
        }

    override val isClosed: Boolean
        get() {
            return process?.isAlive != true
        }

    override fun writeLine(line: String) {
        writer.write(line.trim() + "\n")
        writer.flush()
    }

    override fun readLine(): String? {
        return reader.readLine()
    }

    override fun close() {
        runCatching {
            reader.closeQuietly()
            writer.closeQuietly()
            process?.destroy()
            process = null
        }
    }
}