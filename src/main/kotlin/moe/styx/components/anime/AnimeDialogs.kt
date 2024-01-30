package moe.styx.components.anime

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import moe.styx.logic.runner.launchMPV
import moe.styx.types.MediaEntry

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