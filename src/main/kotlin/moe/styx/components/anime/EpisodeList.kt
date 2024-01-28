package moe.styx.moe.styx.components.anime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.styx.logic.utils.readableSize
import moe.styx.moe.styx.logic.runner.currentPlayer
import moe.styx.moe.styx.logic.runner.launchMPV
import moe.styx.moe.styx.navigation.LocalGlobalNavigator
import moe.styx.types.MediaEntry
import moe.styx.views.settings.SettingsView

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun EpisodeList(episodes: List<MediaEntry>, showSelection: MutableState<Boolean>) {
    val nav = LocalGlobalNavigator.current
    Column(Modifier.fillMaxHeight().fillMaxWidth()) {
        val selected = remember { mutableStateMapOf<String, Boolean>() }

        AnimatedVisibility(showSelection.value) {
            ElevatedCard(Modifier.padding(4.dp).fillMaxWidth().height(30.dp)) {
                Box {
                    Text(
                        if (selected.containsValue(true)) "Selected: ${selected.filter { it.value }.size}" else "Selection",
                        Modifier.padding(4.dp).align(Alignment.TopStart)
                    )

                    Text(
                        "Imagine a download and/or seen button here",
                        Modifier.padding(4.dp).align(Alignment.TopEnd),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }

        var showFailedDialog by remember { mutableStateOf(false) }
        var failedToPlayMessage by remember { mutableStateOf("") }
        if (showFailedDialog) {
            FailedDialog(failedToPlayMessage, Modifier.fillMaxWidth(0.6F), Modifier.align(Alignment.CenterHorizontally)) {
                showFailedDialog = false
                if (it) nav.push(SettingsView())
            }
        }
        var selectedMedia by remember { mutableStateOf<MediaEntry?>(null) }
        var showAppendDialog by remember { mutableStateOf(false) }
        if (showAppendDialog && selectedMedia != null) {
            AppendDialog(selectedMedia!!, Modifier.fillMaxWidth(0.6F), Modifier.align(Alignment.CenterHorizontally), {
                showAppendDialog = false
            }) {
                failedToPlayMessage = it
                showFailedDialog = true
            }
        }

        LazyColumn {
            items(episodes.size) { i ->
                Column(
                    Modifier.padding(10.dp, 5.dp).fillMaxWidth().defaultMinSize(0.dp, 50.dp)
                        .combinedClickable(onClick = {
                            if (showSelection.value) {
                                selected[episodes[i].GUID] = !selected.getOrDefault(episodes[i].GUID, false)
                                return@combinedClickable
                            }
                            if (currentPlayer == null) {
                                launchMPV(episodes[i], false) {
                                    failedToPlayMessage = it
                                    showFailedDialog = true
                                }
                            } else {
                                selectedMedia = episodes[i]
                                showAppendDialog = true
                            }
                        }, onLongClick = {
                            showSelection.value = !showSelection.value
                        })
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        var checked by mutableStateOf(selected.getOrDefault(episodes[i].GUID, false))
                        var isIn by mutableStateOf(false)
                        var timeIn by mutableStateOf(0L)
                        val scope = rememberCoroutineScope()
                        AnimatedVisibility(showSelection.value) {
                            val pointer = Modifier.onPointerEvent(PointerEventType.Enter) {
                                isIn = true
                                scope.launch {
                                    while (isIn && timeIn < 85) {
                                        timeIn++
                                        delay(20)
                                    }
                                }

                            }.onPointerEvent(PointerEventType.Exit) {
                                isIn = false
                                scope.launch {
                                    while (!isIn && timeIn > 0) {
                                        timeIn--
                                        delay(2)
                                    }
                                }
                            }

                            Column {
                                Column {
                                    AnimatedVisibility(timeIn > 35 && i > 0) {
                                        IconButton({
                                            episodes.filter { episodes.indexOf(it) < i }.forEach {
                                                selected.put(
                                                    it.GUID,
                                                    !selected.getOrDefault(it.GUID, false)
                                                )
                                            }
                                        }, modifier = pointer) {
                                            Icon(
                                                Icons.Default.KeyboardArrowUp,
                                                ""
                                            )
                                        }
                                        Spacer(Modifier.height(2.dp))
                                    }
                                }
                                Checkbox(checked, modifier = pointer, onCheckedChange = {
                                    checked = !checked
                                    selected.put(episodes[i].GUID, checked)
                                })
                                Column {
                                    AnimatedVisibility(timeIn > 35 && i < (episodes.size - 1)) {
                                        Spacer(Modifier.height(2.dp))
                                        IconButton({
                                            episodes.filter { episodes.indexOf(it) > i }.forEach {
                                                selected.put(
                                                    it.GUID,
                                                    !selected.getOrDefault(it.GUID, false)
                                                )
                                            }
                                        }, modifier = pointer) {
                                            Icon(
                                                Icons.Default.KeyboardArrowDown,
                                                ""
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Column(Modifier.fillMaxWidth().align(Alignment.CenterVertically)) {
                            Row(Modifier.align(Alignment.Start)) {
                                Text(
                                    "${episodes[i].entryNumber} - ${episodes[i].nameEN}",
                                    Modifier.padding(5.dp),
                                    softWrap = false,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            Column(Modifier.align(Alignment.End)) {
                                Text(
                                    episodes[i].fileSize.readableSize(),
                                    Modifier.padding(5.dp).align(Alignment.End),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
                if (i < (episodes.size - 1))
                    Divider(Modifier.fillMaxWidth(), thickness = 1.dp)
            }
        }
    }
}

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
    onFail: (String) -> Unit = {}
) {
    AlertDialog(
        { onDismiss() },
        modifier = modifier,
        title = { Text("Choose playback") },
        text = { Text("Do you want to start playing now or append to the current playlist?") },
        dismissButton = {
            Button({
                launchMPV(mediaEntry, false) { onFail(it) }
                onDismiss()
            }, modifier = buttonModifier) { Text("Play now") }
        },
        confirmButton = {
            Button({
                launchMPV(mediaEntry, true) { onFail(it) }
                onDismiss()
            }, modifier = buttonModifier) { Text("Append") }
        }
    )
}