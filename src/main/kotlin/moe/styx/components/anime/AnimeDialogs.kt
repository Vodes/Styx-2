package moe.styx.components.anime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.styx.components.TextWithCheckBox
import moe.styx.components.misc.OutlinedText
import moe.styx.logic.data.DataManager
import moe.styx.logic.runner.launchMPV
import moe.styx.theme.AppShapes
import moe.styx.types.MediaEntry
import moe.styx.types.eqI
import moe.styx.types.toBoolean

@Composable
fun FailedDialog(message: String, modifier: Modifier = Modifier, buttonModifier: Modifier = Modifier, onDismiss: (Boolean) -> Unit = {}) {
    AlertDialog(
        { onDismiss(false) },
        modifier = modifier,
        title = { Text("Failed to start player") },
        text = { Text(message) },
        dismissButton = {
            Button({ onDismiss(false) }, modifier = buttonModifier) { Text("OK") }
        },
        confirmButton = {
            Button({ onDismiss(true) }, modifier = buttonModifier) { Text("Open Settings") }
        }
    )
}

@Composable
fun AppendDialog(
    mediaEntry: MediaEntry,
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    execUpdate: () -> Unit = {},
    onFail: (String) -> Unit = {}
) {
    AlertDialog(
        { onDismiss() },
        modifier = modifier,
        title = { Text("Choose playback") },
        text = { Text("Do you want to start playing now or append to the current playlist?") },
        dismissButton = {
            Button({
                launchMPV(mediaEntry, false, { onFail(it) }, execUpdate = execUpdate)
                onDismiss()
            }, modifier = buttonModifier) { Text("Play now") }
        },
        confirmButton = {
            Button({
                launchMPV(mediaEntry, true, { onFail(it) }, execUpdate = execUpdate)
                onDismiss()
            }, modifier = buttonModifier) { Text("Append") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaInfoDialog(mediaEntry: MediaEntry, onDismiss: () -> Unit) {
    val mediaInfo = DataManager.mediainfo.value.find { it.entryID eqI mediaEntry.GUID }
    AlertDialog(onDismiss) {
        Surface(color = MaterialTheme.colorScheme.surface, shape = AppShapes.medium) {
            Column(Modifier.padding(10.dp)) {
                if (mediaInfo == null)
                    Text("Could not find details on this file.")
                else {
                    Text("Video Information", Modifier.padding(3.dp, 10.dp), style = MaterialTheme.typography.titleLarge)
                    Row(Modifier.padding(6.dp, 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedText("${mediaInfo.videoCodec} ${mediaInfo.videoBitdepth}-bit")
                        OutlinedText(mediaInfo.videoRes.split("x").getOrNull(1)?.let { "${it}p" } ?: mediaInfo.videoRes)
                    }
                    Text("Other Tracks", Modifier.padding(3.dp, 10.dp, 0.dp, 5.dp), style = MaterialTheme.typography.titleLarge)
                    Row(Modifier.padding(6.dp, 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextWithCheckBox("Has english dub", mediaInfo.hasEnglishDub.toBoolean(), enabled = false)
                        TextWithCheckBox("Has german dub", mediaInfo.hasGermanDub.toBoolean(), enabled = false)
                        TextWithCheckBox("Has german sub", mediaInfo.hasGermanSub.toBoolean(), enabled = false)
                    }
                }
            }
        }
    }
}