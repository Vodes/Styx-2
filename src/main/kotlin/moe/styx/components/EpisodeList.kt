package moe.styx.moe.styx.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.styx.logic.data.MediaEntry
import moe.styx.moe.styx.logic.runner.launchMPV
import moe.styx.readableSize

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun EpisodeList(episodes: List<MediaEntry>, showSelection: MutableState<Boolean>) {
    Column(Modifier.fillMaxHeight().fillMaxWidth()) {
        val selected = remember { mutableStateMapOf<String, Boolean>() }

        AnimatedVisibility(showSelection.value) {
            Card(Modifier.padding(4.dp).fillMaxWidth().height(30.dp), elevation = 4.dp) {
                Box {
                    Text(
                        if (selected.containsValue(true)) "Selected: ${selected.filter { it.value }.size}" else "Selection",
                        Modifier.padding(4.dp).align(Alignment.TopStart)
                    )

                    Text(
                        "Imagine a download and/or seen button here",
                        Modifier.padding(4.dp).align(Alignment.TopEnd),
                        style = MaterialTheme.typography.overline
                    )
                }
            }
        }

        LazyColumn {
            items(episodes.size) { i ->
                Column(
                    Modifier.padding(10.dp, 5.dp).fillMaxWidth().defaultMinSize(0.dp, 50.dp)
                        .combinedClickable(onClick = {
                            if (showSelection.value) {
                                selected.put(episodes[i].GUID, !selected.getOrDefault(episodes[i].GUID, false))
                                return@combinedClickable
                            }
                            launchMPV(episodes[i]) {}
                        }, onLongClick = {
                            showSelection.value = !showSelection.value
                        })
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val checked = mutableStateOf(selected.getOrDefault(episodes[i].GUID, false))
                        val isIn = mutableStateOf(false)
                        val timeIn = mutableStateOf(0L)
                        val scope = rememberCoroutineScope()
                        AnimatedVisibility(showSelection.value) {
                            val pointer = Modifier.onPointerEvent(PointerEventType.Enter) {
                                isIn.value = true
                                scope.launch {
                                    while (isIn.value && timeIn.value < 85) {
                                        timeIn.value++
                                        delay(20)
                                    }
                                }

                            }.onPointerEvent(PointerEventType.Exit) {
                                isIn.value = false
                                scope.launch {
                                    while (!isIn.value && timeIn.value > 0) {
                                        timeIn.value--
                                        delay(2)
                                    }
                                }
                            }

                            Column {
                                Column {
                                    AnimatedVisibility(timeIn.value > 35 && i > 0) {
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
                                Checkbox(checked.value, modifier = pointer, onCheckedChange = {
                                    checked.value = !checked.value
                                    selected.put(episodes[i].GUID, checked.value)
                                })
                                Column {
                                    AnimatedVisibility(timeIn.value > 35 && i < (episodes.size - 1)) {
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
                                    style = MaterialTheme.typography.subtitle2
                                )
                            }
                            Column(Modifier.align(Alignment.End)) {
                                Text(
                                    episodes[i].fileSize.readableSize(),
                                    Modifier.padding(5.dp).align(Alignment.End),
                                    style = MaterialTheme.typography.overline
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