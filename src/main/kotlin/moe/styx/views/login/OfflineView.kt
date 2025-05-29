package moe.styx.views.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import moe.styx.common.compose.components.AppShapes
import moe.styx.common.compose.components.layout.MainScaffold
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.views.anime.AnimeOverview

class OfflineView : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current
        MainScaffold(title = "Loading") {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Offline-Mode", style = MaterialTheme.typography.displaySmall)
                Text(ServerStatus.getLastKnownText(), style = MaterialTheme.typography.headlineSmall)
                Text("Feel free to keep using Styx with the data you have from your last use.", Modifier.padding(0.dp, 15.dp).weight(1f))

                Button({ nav.replaceAll(AnimeOverview()) }, shape = AppShapes.medium) {
                    Text("OK")
                }
            }
        }
    }
}