package moe.styx.views.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.Navigator
import moe.styx.Main.isUiModeDark
import moe.styx.common.compose.components.misc.SettingsCheckbox
import moe.styx.common.compose.extensions.SimpleTab
import moe.styx.common.compose.http.isLoggedIn
import moe.styx.common.compose.http.login
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.components.misc.MpvVersionAndDownload
import moe.styx.logic.DiscordRPC
import moe.styx.views.login.LoginView
import moe.styx.views.login.OfflineView
import moe.styx.views.other.LoadingView

class SettingsView : SimpleTab("Settings", Icons.Default.Settings) {

    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current
        var darkMode by remember { isUiModeDark }
        val scrollState = rememberScrollState(0)
        Column(Modifier.fillMaxSize().padding(5.dp)) {
            Column(Modifier.padding(5.dp).weight(1F).verticalScroll(scrollState, true)) {
                Text("Layout Options", modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.titleLarge)
                SettingsCheckbox("Darkmode", "darkmode", darkMode, onUpdate = { darkMode = it })
                SettingsCheckbox("Show Names by default", "display-names", false)

                SettingsCheckbox("Show episode summaries", "display-ep-synopsis", false)
                SettingsCheckbox("Prefer german titles and summaries", "prefer-german-metadata", false)

                Divider(Modifier.padding(5.dp), thickness = 2.dp)
                SettingsCheckbox("Use list for shows", "shows-list", false)
                SettingsCheckbox("Use list for movies", "movies-list", false)
                SettingsCheckbox("Sort episodes ascendingly", "episode-asc", false)
                HorizontalDivider(Modifier.padding(5.dp), thickness = 2.dp)
                Row {
                    SettingsCheckbox(
                        "Discord RPC", "discord-rpc", true
                    ) {
                        if (it && !DiscordRPC.isStarted())
                            DiscordRPC.start()
                        else if (!it && DiscordRPC.isStarted()) {
                            DiscordRPC.clearActivity()
                        }
                    }
                    SettingsCheckbox("Show RPC when idle", "discord-rpc-idle", true)
                }
                Text(
                    "Show Styx (& what you're watching) on your discord activity.",
                    Modifier.padding(12.dp, 2.dp, 0.dp, 6.dp),
                    style = MaterialTheme.typography.bodySmall
                )
                Divider(Modifier.padding(5.dp), thickness = 2.dp)

                Text("MPV Options", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(5.dp))
                Button({ nav.push(MpvConfigView()) }, Modifier.padding(5.dp, 4.dp)) {
                    Text("Open Mpv Configuration")
                }
                MpvVersionAndDownload()
            }
            HorizontalDivider(Modifier.padding(5.dp), thickness = 2.dp)
            LoggedInComponent(nav)
        }
    }
}

@Composable
fun LoggedInComponent(nav: Navigator) {
    val primaryColor = MaterialTheme.colorScheme.primary
    if (login != null) {
        Text(
            "Logged in as: ${login!!.name}",
            Modifier.padding(10.dp)
        )
    } else {
        Text("You're not logged in right now.", Modifier.padding(10.dp).drawBehind {
            val strokeWidthPx = 1.dp.toPx()
            val verticalOffset = size.height - 1.sp.toPx()
            drawLine(
                color = primaryColor,
                strokeWidth = strokeWidthPx,
                start = Offset(0f, verticalOffset),
                end = Offset(size.width, verticalOffset)
            )
        }.clickable {
            val view = if (isLoggedIn())
                LoadingView()
            else {
                if (ServerStatus.lastKnown !in listOf(ServerStatus.ONLINE, ServerStatus.UNAUTHORIZED))
                    OfflineView()
                else
                    LoginView()
            }
            nav.replaceAll(view)
        })
    }
}