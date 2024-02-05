package moe.styx.views.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import moe.styx.Main.isUiModeDark
import moe.styx.components.MainScaffold
import moe.styx.components.SettingsCheckbox
import moe.styx.components.misc.MpvVersionAndDownload
import moe.styx.logic.login.ServerStatus
import moe.styx.logic.login.isLoggedIn
import moe.styx.logic.login.login
import moe.styx.navigation.LocalGlobalNavigator
import moe.styx.views.login.LoginView
import moe.styx.views.login.OfflineView
import moe.styx.views.other.LoadingView

class SettingsView : Screen {

    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current
        var darkMode by remember { isUiModeDark }
        val scrollState = rememberScrollState(0)
        MainScaffold(title = "Settings") {
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
                    Divider(Modifier.padding(5.dp), thickness = 2.dp)

                    Text("MPV Options", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(5.dp))
                    Button({ nav.push(MpvConfigView()) }, Modifier.padding(5.dp, 4.dp)) {
                        Text("Open Mpv Configuration")
                    }
                    MpvVersionAndDownload()
                }
                Divider(Modifier.padding(5.dp), thickness = 2.dp)
                LoggedInComponent(nav)
            }
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