package moe.styx.logic.loops

import kotlinx.coroutines.delay
import moe.styx.logic.Endpoints
import moe.styx.logic.launchGlobal
import moe.styx.logic.login.login
import moe.styx.logic.runner.MpvStatus
import moe.styx.logic.runner.currentPlayer
import moe.styx.logic.sendObjectWithResponse
import moe.styx.types.*

object Heartbeats {
    var currentUsers: List<ActiveUser> = emptyList()
    var listeningTo: String? = null
    fun start() {
        launchGlobal {
            delay(3000)
            while (true) {
                if (login == null) {
                    delay(10000)
                    continue
                }
                val mediaActivity = if (currentPlayer != null && MpvStatus.current.file.isNotEmpty() && MpvStatus.current.percentage > -1)
                    MediaActivity(MpvStatus.current.file, MpvStatus.current.seconds.toLong(), !MpvStatus.current.paused)
                else null
                val response = sendObjectWithResponse(Endpoints.HEARTBEAT, ClientHeartbeat(login!!.accessToken, mediaActivity, null))
                if (response != null && response.code == 200 && !response.message.isNullOrBlank()) {
                    currentUsers = json.decodeFromString(response.message!!)
                }
//                if (currentUsers.isNotEmpty())
//                    println("Online Users:\n${currentUsers.map { "${it.user.name} (${it.deviceType})" }}")

                if (listeningTo != null && currentUsers.find { it.user.GUID eqI listeningTo } == null)
                    listeningTo = null
                // TODO: Once we establish the use of `listeningTo` we should also only wait 5000 if that's given
                if (mediaActivity == null)
                    delay(7000)
                else
                    delay(3000)
            }
        }
    }
}