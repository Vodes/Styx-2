package moe.styx.components.anime

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import com.russhwolf.settings.get
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource
import moe.styx.Main.settings
import moe.styx.common.data.Media
import moe.styx.common.extension.toBoolean
import moe.styx.logic.data.*
import moe.styx.theme.AppShapes
import moe.styx.views.anime.AnimeDetailView
import moe.styx.views.anime.MovieDetailView

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AnimeCard(nav: Navigator, media: Media, showUnseenBadge: Boolean = false) {
    val image = media.thumbID.getImageFromID()
    val showNamesAllTheTime by remember { mutableStateOf(settings["display-names", false]) }
    var showName by remember { mutableStateOf(showNamesAllTheTime) }
    val entries = if (showUnseenBadge) {
        DataManager.entries.value.filter { it.mediaID == media.GUID }
            .associateWith { m -> DataManager.watched.value.find { it.entryID == m.GUID } }.filter { (it.value?.maxProgress ?: 0F) < 85F }
    } else emptyMap()
    val shadowAlpha: Float by animateFloatAsState(if (showName) 0.8f else 0f)
    val textAlpha: Float by animateFloatAsState(if (showName) 1.0f else 0f)
    Card(modifier = Modifier.padding(2.dp).aspectRatio(0.71F), onClick = {
        val view = if (media.isSeries.toBoolean()) AnimeDetailView(media.GUID) else MovieDetailView(media.GUID)
        if (nav.lastItem is AnimeDetailView) {
            nav.replace(view)
        } else {
            nav.push(view)
        }
    }) {
        Box(contentAlignment = Alignment.Center) {
            if (image != null) {
                KamelImage(
                    lazyPainterResource(
                        if (image.isCached()) image.getFile() else image.getURL(),
                        filterQuality = FilterQuality.Low
                    ),
                    contentDescription = media.name,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.padding(2.dp).align(Alignment.Center)
                        .onPointerEvent(PointerEventType.Enter) { showName = !showNamesAllTheTime }
                        .onPointerEvent(PointerEventType.Exit) { showName = showNamesAllTheTime }.clip(AppShapes.medium)
                )
            }
            if (showUnseenBadge) {
                if (entries.isNotEmpty()) {
                    ElevatedCard(
                        Modifier.clip(RoundedCornerShape(40)).size(33.dp).padding(4.dp).align(Alignment.TopEnd).zIndex(3f),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            val text = entries.size.toString()
                            Text(
                                text, softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                style = if (text.length > 2) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
            if (showName || textAlpha > 0) {
                Surface(
                    modifier = Modifier.zIndex(1f).align(Alignment.BottomCenter).padding(0.dp, 0.dp, 0.dp, 5.dp)
                        .defaultMinSize(0.dp, 25.dp)
                        .fillMaxWidth()
                        .clip(AppShapes.small)
                        .onPointerEvent(PointerEventType.Enter) { showName = !showNamesAllTheTime }
                        .onPointerEvent(PointerEventType.Exit) { showName = showNamesAllTheTime },
                    color = MaterialTheme.colorScheme.surface.copy(shadowAlpha * 0.85F)
                ) {
                    Surface(
                        modifier = Modifier.zIndex(1f).align(Alignment.Center).padding(1.dp)
                            .defaultMinSize(0.dp, 25.dp)
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface.copy(shadowAlpha)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(5.dp)) {
                            Text(
                                media.name, modifier = Modifier.zIndex(2f).align(Alignment.Center),
                                color = MaterialTheme.colorScheme.onSurface.copy(textAlpha), softWrap = false,
                                overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}