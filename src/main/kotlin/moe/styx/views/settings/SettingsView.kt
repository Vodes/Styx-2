package moe.styx.views.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import moe.styx.isUiModeDark
import moe.styx.logic.login.login
import moe.styx.moe.styx.components.PopButton
import moe.styx.moe.styx.navigation.LocalGlobalNavigator
import moe.styx.settings

class SettingsView : Screen {

    @Composable
    override fun Content() {
        val scaffoldState = rememberScaffoldState()
        val nav = LocalGlobalNavigator.current
        val darkMode = remember { isUiModeDark }

        var num = 0

        Scaffold(scaffoldState = scaffoldState, topBar = {
            TopAppBar(
                title = { Text("Settings") },
                backgroundColor = MaterialTheme.colors.secondary,
                actions = { PopButton(nav) }
            )
        }) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(modifier = Modifier.height(50.dp)) {
                    Text(text = "Darkmode", modifier = Modifier.align(Alignment.CenterVertically))
                    Checkbox(
                        checked = darkMode.value, onCheckedChange = {
                            settings.putBoolean("darkmode", it)
                            darkMode.value = it
                        },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
                Text("Logged in as: ${login!!.name}")
            }
        }
    }
}