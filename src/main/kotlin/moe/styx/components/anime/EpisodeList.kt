package moe.styx.components.anime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.get
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.styx.Main.settings
import moe.styx.common.data.MediaEntry
import moe.styx.common.data.MediaWatched
import moe.styx.common.extension.currentUnixSeconds
import moe.styx.common.extension.eqI
import moe.styx.common.extension.toDateString
import moe.styx.components.misc.ExpandableText
import moe.styx.logic.data.DataManager
import moe.styx.logic.login.login
import moe.styx.logic.loops.RequestQueue
import moe.styx.logic.runner.currentPlayer
import moe.styx.logic.runner.launchMPV
import moe.styx.logic.utils.readableSize
import moe.styx.navigation.LocalGlobalNavigator
import moe.styx.views.settings.SettingsView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EpisodeList(episodes: List<MediaEntry>, showSelection: MutableState<Boolean>) {
    val nav = LocalGlobalNavigator.current
    Column(Modifier.fillMaxHeight().fillMaxWidth()) {
        val selected = remember { mutableStateMapOf<String, Boolean>() }
        var needsRepaint by remember { mutableStateOf(0) }
        val preferGerman = settings["prefer-german-metadata", false]
        val showSummaries = settings["display-ep-synopsis", false]
        if (!showSelection.value)
            selected.clear()

        val watched = episodes.associateWith { ep -> DataManager.watched.value.find { ep.GUID eqI it.entryID } }

        AnimatedVisibility(showSelection.value) { SelectedCard(selected, onUpdate = { needsRepaint++ }) }

        var showFailedDialog by remember { mutableStateOf(false) }
        var failedToPlayMessage by remember { mutableStateOf("") }
        if (showFailedDialog) {
            FailedDialog(failedToPlayMessage, Modifier.fillMaxWidth(0.6F), Modifier.align(Alignment.CenterHorizontally)) {
                showFailedDialog = false
                if (it) nav.push(SettingsView())
            }
        }
        var selectedMedia by remember { mutableStateOf<MediaEntry?>(null) }
        var showMediaInfoDialog by remember { mutableStateOf(false) }
        var showAppendDialog by remember { mutableStateOf(false) }
        if (showAppendDialog && selectedMedia != null) {
            AppendDialog(selectedMedia!!, Modifier.fillMaxWidth(0.6F), Modifier.align(Alignment.CenterHorizontally), {
                showAppendDialog = false
            }, execUpdate = {
                needsRepaint++
            }) {
                failedToPlayMessage = it
                showFailedDialog = true
            }
        }

        if (showMediaInfoDialog && selectedMedia != null) {
            MediaInfoDialog(selectedMedia!!) { showMediaInfoDialog = false }
        }

        LazyColumn {
            items(episodes.size) { i ->
                val ep = episodes[i]
                Column(
                    Modifier.padding(10.dp, 5.dp).fillMaxWidth().defaultMinSize(0.dp, 75.dp)
                        .onClick(true, matcher = PointerMatcher.mouse(PointerButton.Secondary)) {
                            showSelection.value = !showSelection.value
                            if (showSelection.value)
                                selected[ep.GUID] = !selected.getOrDefault(ep.GUID, false)
                        }
                        .combinedClickable(onClick = {
                            if (showSelection.value) {
                                selected[ep.GUID] = !selected.getOrDefault(ep.GUID, false)
                                return@combinedClickable
                            }
                            if (currentPlayer == null) {
                                launchMPV(ep, false, {
                                    failedToPlayMessage = it
                                    showFailedDialog = true
                                }) { needsRepaint++ }
                            } else {
                                selectedMedia = ep
                                showAppendDialog = true
                            }
                        }, onLongClick = {
                            showSelection.value = !showSelection.value
                        })
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        val watchProgress = watched[ep]
                        SelectionCheckboxes(showSelection, selected, episodes, i)
                        Column(Modifier.fillMaxWidth()) {
                            Column {
                                val title = if (!ep.nameDE.isNullOrBlank() && preferGerman) ep.nameDE else ep.nameEN
                                Text(
                                    "${ep.entryNumber}${if (!title.isNullOrBlank()) " - $title" else ""}",
                                    Modifier.padding(5.dp).basicMarquee(animationMode = MarqueeAnimationMode.WhileFocused),
                                    softWrap = false,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        ep.timestamp.toDateString(),
                                        Modifier.padding(5.dp, 0.dp, 0.dp, 4.dp).weight(1f),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Text(
                                        ep.fileSize.readableSize(),
                                        Modifier.padding(5.dp).clickable {
                                            selectedMedia = ep
                                            showMediaInfoDialog = true
                                        },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                val summary = if (!ep.synopsisDE.isNullOrBlank() && preferGerman) ep.synopsisDE else ep.synopsisEN
                                if (!summary.isNullOrBlank() && showSummaries)
                                    ExpandableText(summary, Modifier.padding(8.dp, 2.dp, 5.dp, 2.dp))
                            }
                            if (watchProgress != null)
                                WatchedIndicator(watchProgress, Modifier.fillMaxWidth().padding(0.dp, 2.dp, 0.dp, 5.dp))
                        }
                    }
                }
                if (i < (episodes.size - 1))
                    Divider(Modifier.fillMaxWidth(), thickness = 1.dp)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun SelectionCheckboxes(showSelection: MutableState<Boolean>, selected: SnapshotStateMap<String, Boolean>, episodes: List<MediaEntry>, index: Int) {
    var checked by mutableStateOf(selected.getOrDefault(episodes[index].GUID, false))
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
                AnimatedVisibility(timeIn > 35 && index > 0) {
                    IconButton({
                        episodes.filter { episodes.indexOf(it) < index }.forEach {
                            selected[it.GUID] = !selected.getOrDefault(it.GUID, false)
                        }
                    }, modifier = pointer) {
                        Icon(Icons.Default.KeyboardArrowUp, "")
                    }
                    Spacer(Modifier.height(2.dp))
                }
            }
            Checkbox(checked, modifier = pointer.onClick(true, matcher = PointerMatcher.mouse(PointerButton.Secondary)) {
                timeIn = 85
            }, onCheckedChange = {
                checked = !checked
                selected[episodes[index].GUID] = checked
            })
            Column {
                AnimatedVisibility(timeIn > 35 && index < (episodes.size - 1)) {
                    Spacer(Modifier.height(2.dp))
                    IconButton({
                        episodes.filter { episodes.indexOf(it) > index }.forEach {
                            selected[it.GUID] = !selected.getOrDefault(it.GUID, false)
                        }
                    }, modifier = pointer) {
                        Icon(Icons.Default.KeyboardArrowDown, "")
                    }
                }
            }
        }
    }
}

@Composable
fun SelectedCard(selected: SnapshotStateMap<String, Boolean>, onUpdate: () -> Unit) {
    ElevatedCard(Modifier.padding(4.dp).fillMaxWidth().height(30.dp)) {
        Row {
            Text(
                if (selected.containsValue(true)) "Selected: ${selected.filter { it.value }.size}" else "Selection",
                modifier = Modifier.padding(6.dp, 5.dp).weight(1f), style = MaterialTheme.typography.labelMedium
            )
            IconButton(onClick = {
                val current = selected.filter { it.value }
                if (current.isEmpty())
                    return@IconButton
                if (current.size == 1) {
                    val entry = DataManager.entries.value.find { selected.entries.first().key eqI it.GUID }
                    if (entry == null)
                        return@IconButton
                    RequestQueue.updateWatched(
                        MediaWatched(entry.GUID, login?.userID ?: "", currentUnixSeconds(), 0, 0F, 100F)
                    )
                } else {
                    RequestQueue.addMultipleWatched(current
                        .map { pair -> DataManager.entries.value.find { pair.key eqI it.GUID } }
                        .filterNotNull())
                }
                onUpdate()
            }) { Icon(Icons.Default.Visibility, "Set Watched") }

            IconButton(onClick = {
                val current = selected.filter { it.value }
                if (current.isEmpty())
                    return@IconButton
                if (current.size == 1) {
                    val entry = DataManager.entries.value.find { selected.entries.first().key eqI it.GUID }
                    if (entry == null)
                        return@IconButton
                    RequestQueue.removeWatched(entry)
                } else {
                    RequestQueue.removeMultipleWatched(current
                        .map { pair -> DataManager.entries.value.find { pair.key eqI it.GUID } }
                        .filterNotNull())
                }
                onUpdate()
            }) { Icon(Icons.Default.VisibilityOff, "Set Unwatched") }

            IconButton(onClick = {
                println("Not implemented yet.")
            }) { Icon(Icons.Default.DownloadForOffline, "Download") }

            IconButton(onClick = {
                println("Not implemented yet.")
            }) { Icon(Icons.Default.Delete, "Delete downloaded") }
        }
    }
}

@Composable
fun WatchedIndicator(mediaWatched: MediaWatched, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        LinearProgressIndicator(mediaWatched.progressPercent / 100, Modifier.fillMaxWidth().weight(1f).padding(7.dp, 2.dp))
        if (mediaWatched.maxProgress > 85)
            Icon(
                Icons.Default.CheckCircle,
                "Has been watched",
                Modifier.size(20.dp).padding(0.dp, 0.dp, 6.dp, 0.dp),
                tint = MaterialTheme.colorScheme.primary
            )
    }
}