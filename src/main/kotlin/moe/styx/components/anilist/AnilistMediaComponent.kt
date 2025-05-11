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
import moe.styx.common.extension.capitalize
import moe.styx.common.extension.eqI
import moe.styx.common.extension.toInt
import moe.styx.components.anilist.CommonMediaListStatus.Companion.fromAnilistStatus
import moe.styx.logic.runner.openURI
import pw.vodes.anilistkmp.graphql.fragment.User
import pw.vodes.anilistkmp.graphql.type.MediaListStatus

@Composable
fun AnilistMediaComponent(media: Media, viewer: User?, alMedia: AlMedia, entry: AlUserEntry? = null) {
    Column(Modifier.fillMaxWidth().padding(5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        var status by remember(viewer, entry) {
            mutableStateOf(
                CommonMediaStatus(
                    alMedia.id,
                    entry?.listEntry?.status?.toCommon() ?: CommonMediaListStatus.NONE,
                    entry?.listEntry?.progress ?: -1,
                    alMedia.episodes ?: Int.MAX_VALUE
                )
            )
        }

        RemoteMediaComponent(
            alMedia.title?.english ?: (alMedia.title?.romaji ?: ""),
            alMedia.coverImage?.large ?: "",
            "https://anilist.co/anime/${alMedia.id}",
            viewer != null,
            status
        ) {
            status = it
        }
    }
}

enum class CommonMediaListStatus {
    WATCHING,
    PLANNING,
    COMPLETED,
    DROPPED,
    PAUSED,
    REPEATING,
    NONE;

    fun toAnilistStatus(): MediaListStatus? = when (this) {
        WATCHING -> MediaListStatus.CURRENT
        PLANNING -> MediaListStatus.PLANNING
        COMPLETED -> MediaListStatus.COMPLETED
        DROPPED -> MediaListStatus.DROPPED
        PAUSED -> MediaListStatus.PAUSED
        REPEATING -> MediaListStatus.REPEATING
        else -> null
    }

    companion object {
        fun fromAnilistStatus(status: MediaListStatus): CommonMediaListStatus = when (status) {
            MediaListStatus.CURRENT -> WATCHING
            MediaListStatus.PLANNING -> PLANNING
            MediaListStatus.COMPLETED -> COMPLETED
            MediaListStatus.DROPPED -> DROPPED
            MediaListStatus.PAUSED -> PAUSED
            MediaListStatus.REPEATING -> REPEATING
            else -> NONE
        }
    }
}

fun MediaListStatus.toCommon(): CommonMediaListStatus = fromAnilistStatus(this)

data class CommonMediaStatus(
    val id: Int,
    val status: CommonMediaListStatus,
    val progress: Int = -1,
    val knownMax: Int = Int.MAX_VALUE
) {
    val hasProgress get() = progress > 0
    val hasKnownMax get() = knownMax != Int.MAX_VALUE
}

@Composable
fun RemoteMediaComponent(
    title: String,
    imageURL: String,
    remoteURL: String,
    isLoggedIn: Boolean,
    status: CommonMediaStatus,
    onStatusUpdate: (CommonMediaStatus) -> Unit = {}
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
                            val statusName = status.status.name.capitalize()
                            Text(
                                if (status.status != CommonMediaListStatus.NONE) statusName else "/",
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
                            (if (status.hasProgress) status.progress.toString() else "0") + " / " + (if (status.hasKnownMax) status.knownMax.toString() else "?"),
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.defaultMinSize(100.dp).padding(5.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    ElevatedCard(
                        {
                            onStatusUpdate(status.copy(progress = status.progress + 1))
                        },
                        enabled = if (status.hasProgress && status.hasKnownMax && isLoggedIn)
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
        StatusDialog(status.status.name.capitalize(), CommonMediaListStatus.entries.map { it.name }, { showStatusDialog = false }) {
            onStatusUpdate(status.copy(status = CommonMediaListStatus.entries.find { s -> s.name eqI it }!!))
        }
    }

    if (showEpisodeDialog) {
        EpisodeDialog(status.progress, status.knownMax, { showEpisodeDialog = false }) {
            onStatusUpdate(status.copy(progress = it))
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
        description = "Setting this to None will remove the entry from your list.",
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