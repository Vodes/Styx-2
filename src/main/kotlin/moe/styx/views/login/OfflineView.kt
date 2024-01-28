package moe.styx.views.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import moe.styx.logic.login.ServerStatus
import moe.styx.navigation.LocalGlobalNavigator
import moe.styx.views.other.LoadingView

class OfflineView : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current
        Scaffold(topBar = {
            TopAppBar(
                title = { Text("Loading") },
//                backgroundColor = MaterialTheme.colors.secondary,
            )
        }) {
            Column(Modifier.padding(it).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Offline-Mode", style = MaterialTheme.typography.displaySmall)
                Text(ServerStatus.getLastKnownText(), style = MaterialTheme.typography.headlineSmall)
                Text("Feel free to keep using Styx with the data you have from your last use.", Modifier.padding(0.dp, 15.dp).weight(1f))

                Button({ nav.replaceAll(LoadingView()) }) {
                    Text("OK")
                }
            }
        }
    }

}