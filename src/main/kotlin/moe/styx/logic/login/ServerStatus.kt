package moe.styx.logic.login

import io.ktor.http.*

enum class ServerStatus {
    ONLINE, OFFLINE, ERROR, TIMEOUT, UNAUTHORIZED, BANNED, UNKNOWN;

    companion object {
        var lastKnown = UNKNOWN;

        fun setLastKnown(status: HttpStatusCode) {
            lastKnown = when (status.value) {
                in 200..299 -> ONLINE
                401 -> UNAUTHORIZED
                403 -> BANNED
                in listOf(502, 503, 521, 523) -> OFFLINE
                in listOf(504, 522, 524) -> TIMEOUT
                else -> ERROR
            }
        }

        fun getLastKnownText(): String {
            return when (lastKnown) {
                OFFLINE -> "The server or API seems to be offline."
                TIMEOUT -> "Connection to the server has timed out.\n" +
                        "If you know for sure that the server is up, please check your connection."

                UNAUTHORIZED -> "Your device is not authorized. Please login."
                BANNED -> "You have been banned. Please contact the admin."

                else -> "An error has occurred on the server side.\nPlease contact the admin."
            }
        }
    }
}