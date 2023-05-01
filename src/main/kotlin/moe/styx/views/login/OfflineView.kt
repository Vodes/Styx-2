package moe.styx.moe.styx.views.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import moe.styx.moe.styx.logic.login.ServerStatus
import moe.styx.moe.styx.navigation.LocalGlobalNavigator
import moe.styx.moe.styx.views.other.LoadingView

class OfflineView : Screen {

    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current
        val scaffoldState = rememberScaffoldState()
        Scaffold(scaffoldState = scaffoldState, topBar = {
            TopAppBar(
                title = { Text("Loading") },
                backgroundColor = MaterialTheme.colors.secondary,
            )
        }) {
            Column(Modifier.padding(5.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Offline-Mode", style = MaterialTheme.typography.h3)
                Text(ServerStatus.getLastKnownText(), style = MaterialTheme.typography.h5)
                Text(
                    "Feel free to keep using Styx with the data you have from your last use.",
                    Modifier.padding(20.dp).weight(1f)
                )

                Button({ nav.replaceAll(LoadingView()) }) {
                    Text("OK")
                }
            }
        }
    }

}