package moe.styx.views.settings.sub

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import moe.styx.Styx_2.BuildConfig
import moe.styx.common.compose.components.misc.Toggles
import moe.styx.common.compose.settings
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.logic.runner.openURI

@Composable
fun TrackingSettings() {
    val nav = LocalGlobalNavigator.current
    val sm = nav.rememberNavigatorScreenModel("main-vm") { MainDataViewModel() }
    Column(Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
        Toggles.ContainerSwitch(
            "Auto-Sync",
            "Automatically update remote list status after watching something.",
            value = settings["auto-sync", false]
        ) {
            settings["auto-sync"] = it
        }
        Column(Modifier.fillMaxWidth().padding(12.dp, 5.dp)) {
            if (sm.anilistUser == null) {
                Text("AniList not connected!", style = MaterialTheme.typography.titleMedium)
            } else {
                Text("AniList user connected: ${sm.anilistUser?.name}", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "To dis-/connect an account, please go to the website and check the anilist tab in the user section.\nYou will have to restart this app after doing so.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(6.dp, 4.dp)
            )
            Button({
                openURI("${BuildConfig.SITE_URL}/user")
            }) {
                Text("Open ${BuildConfig.SITE}")
            }
        }
        Spacer(Modifier.height(2.dp))
    }
}