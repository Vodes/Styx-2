package moe.styx.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.navigator.Navigator
import com.russhwolf.settings.get
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource
import moe.styx.dataManager
import moe.styx.logic.data.Media
import moe.styx.moe.styx.logic.data.getFile
import moe.styx.moe.styx.logic.data.getImageFromID
import moe.styx.moe.styx.logic.data.getURL
import moe.styx.moe.styx.logic.data.isCached
import moe.styx.settings
import moe.styx.views.anime.AnimeView

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun AnimeCard(nav: Navigator, media: Media, showUnseenBadge: Boolean = false) {
    val image = media.thumbID.getImageFromID()
    val showNamesAllTheTime = remember { mutableStateOf(settings["display-names", false]) }
    val showName = remember { mutableStateOf(showNamesAllTheTime.value) }
    val shadowAlpha: Float by animateFloatAsState(if (showName.value) 0.8f else 0f)
    val textAlpha: Float by animateFloatAsState(if (showName.value) 1.0f else 0f)
    Card(modifier = Modifier.padding(2.dp).aspectRatio(0.71F), onClick = {
        nav.push(AnimeView(media.name))
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
                        .onPointerEvent(PointerEventType.Exit) { showName.value = showNamesAllTheTime.value }
                )
            }
            if (showUnseenBadge) {
                val entries = dataManager.entries.value.filter { it.mediaID == media.GUID }
                Card(
                    Modifier.clip(RoundedCornerShape(40)).size(33.dp).padding(4.dp).align(Alignment.TopEnd).zIndex(3f),
                    elevation = 2.dp, backgroundColor = MaterialTheme.colors.primaryVariant
                ) {
                    Box(Modifier.fillMaxSize().align(Alignment.Center)) {
                        Text(
                            entries.size.toString(), softWrap = false,
                            overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.caption,
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
                        .onPointerEvent(PointerEventType.Enter) { showName.value = !showNamesAllTheTime.value }
                        .onPointerEvent(PointerEventType.Exit) { showName.value = showNamesAllTheTime.value },
                    color = MaterialTheme.colors.surface.copy(shadowAlpha * 0.85F)
                ) {
                    Surface(
                        modifier = Modifier.zIndex(1f).align(Alignment.Center).padding(1.dp)
                            .defaultMinSize(0.dp, 25.dp)
                            .fillMaxWidth(),
                        color = MaterialTheme.colors.surface.copy(shadowAlpha)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(5.dp)) {
                            Text(
                                media.name, modifier = Modifier.zIndex(2f).align(Alignment.Center),
                                color = MaterialTheme.colors.onSurface.copy(textAlpha), softWrap = false,
                                overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.caption
                            )
                        }
                    }
                }
            }
        }
    }
}