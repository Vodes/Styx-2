package moe.styx.components.anilist

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.OpenInNewOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.michaelflisar.composedialogs.core.DialogButtonType
import com.michaelflisar.composedialogs.core.DialogEvent
import com.michaelflisar.composedialogs.core.rememberDialogState
import com.michaelflisar.composedialogs.dialogs.list.DialogList
import com.michaelflisar.composedialogs.dialogs.number.DialogNumberPicker
import com.michaelflisar.composedialogs.dialogs.number.NumberPickerSetup
import com.michaelflisar.composedialogs.dialogs.number.rememberDialogNumber
import io.kamel.image.KamelImage
import moe.styx.common.compose.components.AppShapes
import moe.styx.common.compose.components.buttons.IconButtonWithTooltip
import moe.styx.common.compose.extensions.getPainter
import moe.styx.common.data.Image
import moe.styx.common.data.Media
import moe.styx.common.data.tmdb.decodeMapping
import moe.styx.common.extension.capitalize
import moe.styx.common.extension.eqI
import moe.styx.common.extension.toInt
import moe.styx.logic.runner.openURI
import pw.vodes.anilistkmp.graphql.fragment.User
import pw.vodes.anilistkmp.graphql.type.MediaListStatus

@Composable
fun AnilistMediaComponent(media: Media, viewer: User?, alMedia: AlMedia, entry: AlUserEntry? = null) {
    val mapping = media.decodeMapping()
    Column(Modifier.fillMaxWidth().padding(5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        var status by remember(viewer, entry) {
            mutableStateOf(entry?.let {
                RemoteMediaStatus(
                    it.media.id,
                    it.listEntry.status?.name ?: "",
                    it.listEntry.progress ?: 0,
                    alMedia.episodes,
                )
            })
        }

        RemoteMediaComponent(
            alMedia.title?.english ?: (alMedia.title?.romaji ?: ""),
            alMedia.coverImage?.large ?: "",
            "https://anilist.co/anime/${alMedia.id}",
            viewer != null,
            status,
            MediaListStatus.knownEntries.map { it.name }
        ) {
            status = it
        }
    }
}

data class RemoteMediaStatus(val id: Int, val status: String?, val progress: Int, val knownMax: Int? = null)

@Composable
fun RemoteMediaComponent(
    title: String,
    imageURL: String,
    remoteURL: String,
    isLoggedIn: Boolean,
    status: RemoteMediaStatus? = null,
    statusList: List<String> = emptyList(),
    onStatusUpdate: (RemoteMediaStatus) -> Unit = {}
) {
    var showStatusDialog by remember { mutableStateOf(false) }
    var showEpisodeDialog by remember { mutableStateOf(false) }
    ElevatedCard(elevation = CardDefaults.elevatedCardElevation(4.dp)) {
        Row {
            val image = Image(
                imageURL.substringAfterLast("/"),
                hasJPG = false.toInt(),
                hasWEBP = false.toInt(),
                hasPNG = false.toInt(),
                externalURL = imageURL
            )
            val painter = image.getPainter()
            Box(
                Modifier.padding(10.dp).clip(AppShapes.large).width(96.dp).heightIn(0.dp, 145.dp).fillMaxHeight()
            ) {
                KamelImage(
                    { painter },
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    animationSpec = tween(),
                    onLoading = { CircularProgressIndicator(progress = { it }) }
                )
            }

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                    IconButtonWithTooltip(Icons.Filled.OpenInNewOff, "Open Anilist page") {
                        openURI(remoteURL)
                    }
                }

                Row(Modifier.fillMaxWidth()) {
                    ElevatedCard(
                        {
                            showStatusDialog = true
                        },
                        enabled = isLoggedIn,
                        modifier = Modifier.padding(3.dp),
                        colors = CardDefaults.elevatedCardColors(MaterialTheme.colorScheme.tertiaryContainer),
                        elevation = CardDefaults.elevatedCardElevation(1.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                status?.status?.capitalize() ?: "/",
                                style = MaterialTheme.typography.labelLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.defaultMinSize(100.dp).padding(5.dp)
                            )
                        }
                    }

                    ElevatedCard(
                        {
                            showEpisodeDialog = true
                        },
                        enabled = isLoggedIn,
                        modifier = Modifier.padding(3.dp),
                        colors = CardDefaults.elevatedCardColors(MaterialTheme.colorScheme.tertiaryContainer),
                        elevation = CardDefaults.elevatedCardElevation(1.dp)
                    ) {
                        Text(
                            (status?.progress?.toString() ?: "0") + " / " + (status?.knownMax?.toString() ?: "?"),
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.defaultMinSize(100.dp).padding(5.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    ElevatedCard(
                        {
                            onStatusUpdate(status!!.copy(progress = status.progress + 1))
                        },
                        enabled = if (status?.progress != null && status.knownMax != null && isLoggedIn)
                            status.progress < status.knownMax
                        else isLoggedIn,
                        modifier = Modifier.padding(3.dp),
                        colors = CardDefaults.elevatedCardColors(MaterialTheme.colorScheme.secondaryContainer),
                        elevation = CardDefaults.elevatedCardElevation(1.dp)
                    ) {
                        Text(
                            "+1",
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.defaultMinSize(10.dp).padding(5.dp)
                        )
                    }
                }
            }
        }
    }
    if (showStatusDialog) {
        StatusDialog(status?.status?.capitalize() ?: "", statusList, { showStatusDialog = false }) {
            onStatusUpdate(status!!.copy(status = statusList.find { stat -> stat eqI it }))
        }
    }

    if (showEpisodeDialog) {
        EpisodeDialog(status?.progress ?: 0, status?.knownMax, { showEpisodeDialog = false }) {
            onStatusUpdate(status!!.copy(progress = it))
        }
    }
}

@Composable
fun StatusDialog(current: String, available: List<String>, onDismiss: () -> Unit, onUpdate: (String) -> Unit) {
    val state = rememberDialogState(current)
    val selected = remember { mutableStateOf<Int?>(available.indexOfLast { it eqI current }) }
    DialogList(
        title = { Text("Watch status") },
        icon = { Icon(Icons.Filled.Checklist, "Watch status") },
        state = state,
        items = available,
        itemIdProvider = { available.indexOf(it) },
        selectionMode = DialogList.SelectionMode.SingleSelect(
            selected = selected,
            selectOnRadioButtonClickOnly = false
        ),
        itemContents = DialogList.ItemDefaultContent(
            text = { it.capitalize() }
        ),
        onEvent = {
            if (it.dismissed)
                onDismiss()
            if (it is DialogEvent.Button && it.button == DialogButtonType.Positive) {
                onUpdate(selected.value?.let { available[it] } ?: "")
            }
        }
    )
}

@Composable
fun EpisodeDialog(current: Int, max: Int?, onDismiss: () -> Unit, onUpdate: (Int) -> Unit) {
    val state = rememberDialogState()
    val value = rememberDialogNumber(current)
    // number dialog
    DialogNumberPicker(
        state = state,
        title = { Text("Episode") },
        value = value,
        onEvent = {
            if (it.dismissed)
                onDismiss()
            if (it is DialogEvent.Button && it.button == DialogButtonType.Positive) {
                onUpdate(value.value)
            }
        },
        setup = NumberPickerSetup(
            min = 0, max = max ?: Int.MAX_VALUE, stepSize = 1
        )
    )
}