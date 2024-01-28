package moe.styx.moe.styx.components.anime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.kamel.core.Resource
import io.kamel.image.KamelImage
import moe.styx.logic.data.DataManager
import moe.styx.moe.styx.components.misc.ExpandIconButton
import moe.styx.moe.styx.navigation.LocalGlobalNavigator
import moe.styx.theme.AppShapes
import moe.styx.types.Media

@Composable
fun MediaNameListing(media: Media, modifier: Modifier = Modifier) {
    SelectionContainer {
        Column(modifier.padding(10.dp, 20.dp)) {
            if (media.nameEN != null) {
                Text(
                    media.nameEN!!,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 3
                )
            }
            if (media.nameJP != null && (media.nameEN == null || !media.nameEN.equals(media.nameJP, true)))
                Text(
                    media.nameJP!!,
                    Modifier.padding(2.dp, 10.dp),
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3
                )
        }
    }
}

@Composable
fun BigScalingCardImage(image: Resource<Painter>, modifier: Modifier = Modifier) {
    Column(modifier) {
        ElevatedCard(
            Modifier.align(Alignment.Start).padding(12.dp).requiredHeightIn(150.dp, 500.dp).aspectRatio(0.71F),
        ) {
            KamelImage(
                image,
                contentDescription = "Anime",
                modifier = Modifier.padding(2.dp).clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.FillBounds
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaGenreListing(media: Media) {
    val isExpanded = remember { mutableStateOf(false) }
    val shape = AppShapes.medium
    if (!media.genres.isNullOrBlank()) {
        FlowRow(Modifier.padding(5.dp), horizontalArrangement = Arrangement.Start, verticalArrangement = Arrangement.Center) {
            for (genre in media.genres!!.split(",")) {
                Surface(
                    Modifier.clip(shape).padding(7.dp).height(34.dp),
                    shape = shape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Row {
                        Text(
                            genre,
                            Modifier.padding(7.dp).align(Alignment.CenterVertically),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
            if (!media.tags.isNullOrBlank()) {
                ExpandIconButton(tooltip = "Show tags", tooltipExpanded = "Hide tags") { isExpanded.value = it }
            }
        }
        if (!media.tags.isNullOrBlank()) {
            AnimatedVisibility(isExpanded.value) {
                FlowRow(Modifier.padding(5.dp)) {
                    media.tags!!.split(",").forEach { tag ->
                        Surface(
                            Modifier.clip(shape).padding(7.dp).height(34.dp),
                            shape = shape,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                            contentColor = MaterialTheme.colorScheme.secondary
                        ) {
                            Row {
                                Text(
                                    tag,
                                    Modifier.padding(7.dp).align(Alignment.CenterVertically),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MediaRelations(media: Media) {
    Text("Relations", Modifier.padding(6.dp, 4.dp), style = MaterialTheme.typography.titleLarge)
    Column(Modifier.padding(5.dp, 2.dp)) {
        val pre = DataManager.media.value.find { a -> a.GUID == media.prequel }
        if (pre != null) {
            Column(Modifier.align(Alignment.Start)) {
                Text("Prequel", Modifier.padding(4.dp, 5.dp, 4.dp, 6.dp), style = MaterialTheme.typography.bodyMedium)
                AnimeListItem(LocalGlobalNavigator.current, pre)//, 5)
            }
        }
        val seq = DataManager.media.value.find { a -> a.GUID == media.sequel }
        if (seq != null) {
            Column(Modifier.align(Alignment.Start)) {
                Text("Sequel", Modifier.padding(4.dp, 5.dp, 4.dp, 6.dp), style = MaterialTheme.typography.bodyMedium)
                AnimeListItem(LocalGlobalNavigator.current, seq)//, 5)
            }
        }
    }
}