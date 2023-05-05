package moe.styx.views.settings

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.russhwolf.settings.get
import moe.styx.isUiModeDark
import moe.styx.logic.login.login
import moe.styx.moe.styx.components.misc.PopButton
import moe.styx.moe.styx.components.SettingsCheckbox
import moe.styx.moe.styx.navigation.LocalGlobalNavigator
import moe.styx.settings

class SettingsView : Screen {

    @Composable
    override fun Content() {
        val scaffoldState = rememberScaffoldState()
        val nav = LocalGlobalNavigator.current
        val darkMode = remember { isUiModeDark }
        val showNamesAllTheTime = remember { mutableStateOf(settings["display-names", false]) }

        var num = 0

        Scaffold(scaffoldState = scaffoldState, topBar = {
            TopAppBar(
                title = { Text("Settings") },
                backgroundColor = MaterialTheme.colors.secondary,
                actions = { PopButton(nav) }
            )
        }) {
            val scrollState = ScrollState(0)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
                Column(Modifier.padding(5.dp).scrollable(scrollState, Orientation.Vertical)) {
                    SettingsCheckbox("Darkmode", "darkmode", true, onUpdate = { darkMode.value = it })

                    Column(Modifier.padding(5.dp)) {
                        Text("Layout Options", style = MaterialTheme.typography.h5, modifier = Modifier.padding(5.dp))
                        SettingsCheckbox("Show Names by default", "display-names", false)

                        Divider(Modifier.padding(5.dp), thickness = 2.dp)

                        SettingsCheckbox("Use list for shows", "shows-list", false)
                        SettingsCheckbox("Use list for movies", "movies-list", false)
                    }

                    Column(Modifier.padding(5.dp)) {
                        Text("MPV Options", style = MaterialTheme.typography.h5, modifier = Modifier.padding(5.dp))
                        SettingsCheckbox("Use system MPV", "mpv-system", false)
                        SettingsCheckbox("Try to use flatpak (Linux only)", "mpv-flatpak", false)
                    }
                }
                Text(
                    if (login != null) "Logged in as: ${login!!.name}" else "You're not logged in right now.",
                    Modifier.padding(10.dp).align(Alignment.BottomStart)
                )
            }
        }
    }
}