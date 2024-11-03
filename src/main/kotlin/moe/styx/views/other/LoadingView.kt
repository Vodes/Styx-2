package moe.styx.views.other

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.lifecycle.LifecycleEffectOnce
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.styx.Styx_2.BuildConfig
import moe.styx.common.compose.components.layout.MainScaffold
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.isWindows
import moe.styx.common.util.Log
import moe.styx.common.util.launchThreaded
import moe.styx.logic.utils.MpvUtils
import moe.styx.logic.utils.downloadNewInstaller
import moe.styx.logic.utils.isUpToDate
import moe.styx.views.anime.AnimeOverview
import moe.styx.views.data.MainDataViewModel
import java.awt.Desktop
import java.net.URI

class LoadingView : Screen {

    @OptIn(ExperimentalVoyagerApi::class)
    @Composable
    override fun Content() {
        Log.d { "Loadingview" }
        val nav = LocalGlobalNavigator.current
        val sm = nav.rememberNavigatorScreenModel("main-vm") { MainDataViewModel() }
        val progress = Storage.loadingProgress.collectAsState()
        var outdated by remember { mutableStateOf<Boolean?>(null) }

        LifecycleEffectOnce {
            launchThreaded {
                Log.d { "Checking version" }
                if (ServerStatus.lastKnown != ServerStatus.UNKNOWN && !isUpToDate()) {
                    if (isWindows)
                        downloadNewInstaller()
                    Log.d { "Is outdated" }
                    outdated = true
                }
            }
        }

        LaunchedEffect(Unit) {
            delay(1000)
            launch {
                Storage.loadData()
                sm.updateData()
                MpvUtils.checkVersionAndDownload()
                nav.replaceAll(AnimeOverview())
            }
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
            var modifier = Modifier.padding(10.dp)
            if (!isWindows)
                modifier = modifier.weight(1f)
            Text(
                "This version of Styx is outdated.",
                modifier,
                style = MaterialTheme.typography.headlineMedium
            )
            if (isWindows)
                Text(
                    "Attempting to download the new version automatically. Please wait a bit. If nothing happens, feel free to do it manually below.",
                    Modifier.weight(1f).padding(16.dp)
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