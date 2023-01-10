package views.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import isUiModeDark
import settings

class SettingsView : Screen {

    @Composable
    override fun Content() {
        val scaffoldState = rememberScaffoldState()
        val nav = LocalNavigator.currentOrThrow
        val darkMode = remember { isUiModeDark }
        Scaffold(scaffoldState = scaffoldState, topBar = {
            TopAppBar(
                title = { Text("Settings") },
                backgroundColor = MaterialTheme.colors.secondary,
                actions = {
                    IconButton(onClick = { nav.pop() }, content = { Icon(Icons.Filled.Close, null) })
                }
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
            }
        }
    }
}