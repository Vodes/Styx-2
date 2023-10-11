package moe.styx.views.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import moe.styx.isUiModeDark
import moe.styx.logic.login.login
import moe.styx.moe.styx.components.SettingsCheckbox
import moe.styx.moe.styx.components.misc.PopButton
import moe.styx.moe.styx.navigation.LocalGlobalNavigator

class SettingsView : Screen {

    @Composable
    override fun Content() {
        val scaffoldState = rememberScaffoldState()
        val nav = LocalGlobalNavigator.current
        val darkMode = remember { isUiModeDark }

        Scaffold(scaffoldState = scaffoldState, topBar = {
            TopAppBar(
                title = { Text("Settings") },
                backgroundColor = MaterialTheme.colors.secondary,
                actions = { PopButton(nav) }
            )
        }) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState(0), true)) {
                Column(Modifier.padding(5.dp).weight(1F)) {

                    Column(Modifier.padding(5.dp)) {
                        Text("Layout Options", style = MaterialTheme.typography.h5, modifier = Modifier.padding(5.dp))
                        SettingsCheckbox("Darkmode", "darkmode", true, onUpdate = { darkMode.value = it })
                        SettingsCheckbox("Show Names by default", "display-names", false)

                        Divider(Modifier.padding(5.dp), thickness = 2.dp)

                        SettingsCheckbox("Use list for shows", "shows-list", false)
                        SettingsCheckbox("Use list for movies", "movies-list", false)
                    }

                    Column(Modifier.padding(5.dp)) {
                        Text("MPV Options", style = MaterialTheme.typography.h5, modifier = Modifier.padding(5.dp))
                        SettingsCheckbox("Use system MPV", "mpv-system", false)
                        SettingsCheckbox("Try to use flatpak (Linux only)", "mpv-flatpak", false)
                        Divider(Modifier.padding(5.dp), thickness = 2.dp)
                        SettingsCheckbox(
                            "Play next automatically",
                            "mpv-play-next",
                            true,
                            description = "Plays next episode (if any) when you reached the end and are paused/stopped."
                        )
                        Divider(Modifier.padding(5.dp), thickness = 2.dp)
                    }
                }
                Text(
                    if (login != null) "Logged in as: ${login!!.name}" else "You're not logged in right now.",
                    Modifier.padding(10.dp)
                )
            }
        }
    }
}