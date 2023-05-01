package moe.styx.moe.styx.logic.login

import io.ktor.http.*

enum class ServerStatus {
    ONLINE, OFFLINE, ERROR, TIMEOUT, UNKNOWN;

    companion object {
        var lastKnown = UNKNOWN;

        fun setLastKnown(status: HttpStatusCode) {
            when (status.value) {
                in 200..299 -> lastKnown = ONLINE
                in listOf(502, 503, 521, 523) -> lastKnown = OFFLINE
                in listOf(504, 522, 524) -> lastKnown = TIMEOUT
                else -> lastKnown = ERROR
            }
        }

        fun getLastKnownText(): String {
            return when (lastKnown) {
                OFFLINE -> "The server or API seems to be offline."
                TIMEOUT -> "Connection to the server has timed out.\n" +
                        "If you know for sure that the server is up, please check your connection."

                else -> "An error has occurred on the server side.\nPlease contact the admin."
            }
        }
    }
}