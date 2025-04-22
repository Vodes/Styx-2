package moe.styx.logic.pipe

abstract class Pipe {

    abstract fun connect()

    abstract val isConnected: Boolean
    abstract val isClosed: Boolean

    abstract fun writeLine(line: String)
    abstract fun readLine(): String?

    abstract fun close()

    fun closeAllTheThings() = close()
}