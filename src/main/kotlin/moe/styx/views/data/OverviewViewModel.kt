package moe.styx.views.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import moe.styx.common.compose.http.isLoggedIn
import moe.styx.common.compose.http.login
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.util.Log
import moe.styx.logic.utils.MpvUtils
import moe.styx.logic.utils.isUpToDate

class OverviewViewModel : ScreenModel {
    var isLoggedIn by mutableStateOf<Boolean?>(null)
        private set

    var isOffline by mutableStateOf<Boolean?>(null)
        private set

    var isOutdated by mutableStateOf<Boolean?>(null)
        private set

    init {
        Log.d { "Checking login..." }
        if (isLoggedIn == null) {
            isLoggedIn = isLoggedIn()
            Log.d { "Logged in: $isLoggedIn | ${login?.name}" }
        }
    }

    fun runChecks() {
        screenModelScope.launch {
            launch { isOutdated = !isUpToDate() }
            isOffline = ServerStatus.lastKnown in arrayOf(ServerStatus.OFFLINE, ServerStatus.UNKNOWN, ServerStatus.TIMEOUT)
            MpvUtils.checkVersionAndDownload()
        }
    }
}