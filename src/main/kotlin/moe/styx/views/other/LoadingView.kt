package moe.styx.views.other

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.styx.Styx__.BuildConfig
import moe.styx.components.MainScaffold
import moe.styx.logic.data.DataManager
import moe.styx.logic.login.ServerStatus
import moe.styx.logic.utils.MpvUtils
import moe.styx.logic.utils.isUpToDate
import moe.styx.navigation.LocalGlobalNavigator
import moe.styx.views.anime.AnimeOverview
import java.awt.Desktop
import java.net.URI

class LoadingView : Screen {

    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current
        val coroutineScope = rememberCoroutineScope()
        val progress = remember { mutableStateOf("") }

        if (ServerStatus.lastKnown != ServerStatus.UNKNOWN && !isUpToDate()) {
            OutdatedVersion()
            return
        }

        coroutineScope.launch {
            launch { DataManager.load { progress.value = it } }
            while (!DataManager.isLoaded.value) {
                delay(500)
            }
            MpvUtils.checkVersionAndDownload()
            nav.replaceAll(AnimeOverview())
        }

        MainScaffold(title = "Loading", addPopButton = false) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.padding(10.dp).align(Alignment.TopCenter)) {
                    Text(
                        text = progress.value,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 25.dp)
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 15.dp).fillMaxSize(.4F)
                    )
                }
            }
        }
    }
}

@Composable
fun OutdatedVersion() {
    MainScaffold(title = "Outdated", addPopButton = false) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "This version of Styx is outdated.\nPlease update.",
                Modifier.padding(10.dp).weight(1f),
                style = MaterialTheme.typography.headlineMedium
            )
            Button({
                if (Desktop.isDesktopSupported())
                    Desktop.getDesktop().browse(URI(BuildConfig.SITE_URL))
            }) {
                Text("Open ${BuildConfig.SITE}")
            }
        }
    }
}