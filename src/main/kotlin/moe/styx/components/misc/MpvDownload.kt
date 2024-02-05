package moe.styx.components.misc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.get
import kotlinx.coroutines.delay
import moe.styx.Main
import moe.styx.logic.utils.MpvUtils.isMpvDownloading

@Composable
fun MpvVersionAndDownload() {
    var currentVersion by remember { mutableStateOf(Main.settings["mpv-version", "None"]) }
    var downloading by remember { mutableStateOf(isMpvDownloading) }
    LaunchedEffect(Unit) {
        while (true) {
            currentVersion = Main.settings["mpv-version", "None"]
            downloading = isMpvDownloading
            delay(400)
        }
    }
    Column(Modifier.padding(7.dp)) {
        Text("Local version: ${currentVersion.split("mpv-").getOrNull(1)?.removeSuffix(".zip") ?: currentVersion}")
        if (downloading) {
            Text("Is downloading", color = MaterialTheme.colorScheme.secondary)
        }
    }
}