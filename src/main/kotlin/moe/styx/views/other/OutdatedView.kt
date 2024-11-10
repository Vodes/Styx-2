package moe.styx.views.other

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.lifecycle.LifecycleEffectOnce
import cafe.adriel.voyager.core.screen.Screen
import moe.styx.Styx_2.BuildConfig
import moe.styx.common.compose.components.layout.MainScaffold
import moe.styx.common.isWindows
import moe.styx.logic.runner.openURI
import moe.styx.logic.utils.downloadNewInstaller

class OutdatedView : Screen {

    @OptIn(ExperimentalVoyagerApi::class)
    @Composable
    override fun Content() {
        LifecycleEffectOnce {
            if (isWindows)
                downloadNewInstaller()
        }

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
                    openURI("${BuildConfig.SITE_URL}/user")
                }) {
                    Text("Open ${BuildConfig.SITE}")
                }
            }
        }
    }
}