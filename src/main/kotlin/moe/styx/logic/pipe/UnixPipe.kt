package moe.styx.logic.pipe

import org.newsclub.net.unix.AFUNIXSocket
import org.newsclub.net.unix.AFUNIXSocketAddress
import java.io.BufferedReader
import java.io.File

class UnixPipe(val path: String) : Pipe() {
    private val socket: AFUNIXSocket = AFUNIXSocket.newInstance()
    private var socketReader: BufferedReader? = null

    init {
        AFUNIXSocket.ensureSupported()
    }

    override fun connect() {
        val file = File(path)
        socket.connect(AFUNIXSocketAddress.of(file))
    }

    override val isConnected: Boolean = socket.isConnected

    override val isClosed: Boolean = socket.isClosed

    override fun writeLine(line: String) {
        val writer = socket.outputStream.writer()
        writer.appendLine(line.trim())
        writer.flush()
    }

    override fun readLine(): String? {
        if (socketReader == null)
            socketReader = socket.inputStream.bufferedReader(Charsets.UTF_8)
        return socketReader!!.readLine()
    }

    override fun close() {
        runCatching { socket.outputStream.close() }
        runCatching { socket.inputStream.close() }
        runCatching { socket.close() }
    }
}