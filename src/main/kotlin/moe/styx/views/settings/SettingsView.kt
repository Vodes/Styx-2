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
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import moe.styx.Main.isUiModeDark
import moe.styx.common.compose.components.misc.Toggles
import moe.styx.common.compose.components.misc.Toggles.settingsContainer
import moe.styx.common.compose.extensions.SimpleTab
import moe.styx.common.compose.http.isLoggedIn
import moe.styx.common.compose.http.login
import moe.styx.common.compose.settings
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
                Column(Modifier.settingsContainer()) {
                    Text("Layout Options", modifier = Modifier.padding(10.dp, 7.dp), style = MaterialTheme.typography.titleLarge)
                    Toggles.ContainerSwitch("Darkmode", value = darkMode) { darkMode = it }
                    Toggles.ContainerSwitch("Show names by default", value = settings["display-names", false]) { settings["display-names"] = it }
                    Toggles.ContainerSwitch(
                        "Show episode summaries",
                        value = settings["display-ep-synopsis", false]
                    ) { settings["display-ep-synopsis"] = it }
                    Toggles.ContainerSwitch(
                        "Prefer german titles and summaries",
                        value = settings["prefer-german-metadata", false]
                    ) { settings["prefer-german-metadata"] = it }
                    Row(Modifier.fillMaxWidth()) {
                        Toggles.ContainerSwitch(
                            "Use list for shows",
                            modifier = Modifier.weight(1f),
                            value = settings["shows-list", false],
                            paddingValues = Toggles.rowStartPadding
                        ) { settings["shows-list"] = it }
                        Toggles.ContainerSwitch(
                            "Use list for movies",
                            modifier = Modifier.weight(1f),
                            value = settings["movies-list", false],
                            paddingValues = Toggles.rowEndPadding
                        ) { settings["movies-list"] = it }
                    }
                    Toggles.ContainerSwitch(
                        "Sort episodes ascendingly",
                        value = settings["episode-asc", false],
                        paddingValues = Toggles.colEndPadding
                    ) { settings["episode-asc"] = it }
                }
                Column(Modifier.settingsContainer()) {
                    Text("Discord", modifier = Modifier.padding(10.dp, 7.dp), style = MaterialTheme.typography.titleLarge)

                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
                        Toggles.ContainerSwitch(
                            "Enable RPC",
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            value = settings["discord-rpc", true],
                            paddingValues = Toggles.rowStartPadding
                        ) {
                            settings["discord-rpc"] = it
                            if (it && !DiscordRPC.isStarted())
                                DiscordRPC.start()
                            else if (!it && DiscordRPC.isStarted()) {
                                DiscordRPC.clearActivity()
                            }
                        }

                        Toggles.ContainerSwitch(
                            "Show RPC when idle",
                            "Disabling this means the discord status will only show while you're watching something.",
                            value = settings["discord-rpc-idle", true], modifier = Modifier.weight(1f), paddingValues = Toggles.rowEndPadding
                        ) { settings["discord-rpc-idle"] = it }
                    }
                    Spacer(Modifier.height(5.dp))
                }
                Column(Modifier.settingsContainer()) {
                    Text("MPV Options", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(10.dp, 7.dp))
                    Button({ nav.push(MpvConfigView()) }, Modifier.padding(8.dp, 4.dp)) {
                        Text("Open Mpv Configuration")
                    }
                    MpvVersionAndDownload()
                }
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