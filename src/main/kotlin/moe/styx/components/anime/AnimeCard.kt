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
import moe.styx.logic.data.DataManager
import moe.styx.logic.data.*
import moe.styx.views.anime.AnimeDetailView
import moe.styx.settings
import moe.styx.theme.AppShapes
import moe.styx.types.Media

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun AnimeCard(nav: Navigator, media: Media, showUnseenBadge: Boolean = false) {
    val image = media.thumbID.getImageFromID()
    val showNamesAllTheTime = remember { mutableStateOf(settings["display-names", false]) }
    val showName = remember { mutableStateOf(showNamesAllTheTime.value) }
    val shadowAlpha: Float by animateFloatAsState(if (showName.value) 0.8f else 0f)
    val textAlpha: Float by animateFloatAsState(if (showName.value) 1.0f else 0f)
    Card(modifier = Modifier.padding(2.dp).aspectRatio(0.71F), onClick = {
        if (nav.lastItem is AnimeDetailView) {
            nav.replace(AnimeDetailView(media.GUID))
        } else {
            nav.push(AnimeDetailView(media.GUID))
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
                        .onPointerEvent(PointerEventType.Enter) { showName.value = !showNamesAllTheTime.value }
                        .onPointerEvent(PointerEventType.Exit) { showName.value = showNamesAllTheTime.value }.clip(AppShapes.medium)
                )
            }
            if (showUnseenBadge) {
                val entries = DataManager.entries.value.filter { it.mediaID == media.GUID }
                ElevatedCard(
                    Modifier.clip(RoundedCornerShape(40)).size(33.dp).padding(4.dp).align(Alignment.TopEnd).zIndex(3f),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            entries.size.toString(), softWrap = false,
                            overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
            if (showName.value || textAlpha > 0) {
                Surface(
                    modifier = Modifier.zIndex(1f).align(Alignment.BottomCenter).padding(0.dp, 0.dp, 0.dp, 5.dp)
                        .defaultMinSize(0.dp, 25.dp)
                        .fillMaxWidth()
                        .clip(AppShapes.small)
                        .onPointerEvent(PointerEventType.Enter) { showName.value = !showNamesAllTheTime.value }
                        .onPointerEvent(PointerEventType.Exit) { showName.value = showNamesAllTheTime.value },
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