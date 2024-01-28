package moe.styx.views.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import moe.styx.isUiModeDark
import moe.styx.logic.login.login
import moe.styx.components.SettingsCheckbox
import moe.styx.components.misc.PopButton
import moe.styx.navigation.LocalGlobalNavigator

class SettingsView : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current
        var darkMode by remember { isUiModeDark }

        Scaffold(topBar = {
            TopAppBar(
                title = { Text("Settings") },
                //backgroundColor = MaterialTheme.colors.secondary,
                actions = { PopButton(nav) }
            )
        }) { paddingValues ->
            Column(Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState(0), true)) {
                Column(Modifier.padding(5.dp).weight(1F)) {

                    Column(Modifier.padding(5.dp)) {
                        Text("Layout Options", modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.bodyMedium)
                        SettingsCheckbox("Darkmode", "darkmode", darkMode, onUpdate = { darkMode = it })
                        SettingsCheckbox("Show Names by default", "display-names", false)

                        Divider(Modifier.padding(5.dp), thickness = 2.dp)

                        SettingsCheckbox("Use list for shows", "shows-list", false)
                        SettingsCheckbox("Use list for movies", "movies-list", false)
                    }

                    Column(Modifier.padding(5.dp)) {
                        Text("MPV Options", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(5.dp))
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