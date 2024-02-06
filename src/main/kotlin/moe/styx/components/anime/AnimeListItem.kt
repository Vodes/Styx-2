package moe.styx.components.anime

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource
import moe.styx.logic.data.*
import moe.styx.theme.AppShapes
import moe.styx.types.Media
import moe.styx.types.toBoolean
import moe.styx.views.anime.AnimeDetailView
import moe.styx.views.anime.MovieDetailView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeListItem(nav: Navigator, media: Media) {
    val image = media.thumbID.getImageFromID()
    ElevatedCard(modifier = Modifier.padding(5.dp, 2.dp).fillMaxWidth(), onClick = {
        val view = if (media.isSeries.toBoolean()) AnimeDetailView(media.GUID) else MovieDetailView(media.GUID)
        if (nav.lastItem is AnimeDetailView) {
            nav.replace(view)
        } else {
            nav.push(view)
        }
    }) {
        Row(Modifier.height(80.dp)) {
            ElevatedCard(
                Modifier.clip(AppShapes.large).width(67.dp).padding(3.dp),
            ) {
                if (image != null) {
                    KamelImage(
                        lazyPainterResource(
                            if (image.isCached()) image.getFile() else image.getURL(),
                            filterQuality = FilterQuality.Low
                        ),
                        contentDescription = media.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.padding(1.dp)
                    )
                }
            }
            Column {
                Text(
                    media.name,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    modifier = Modifier.padding(10.dp, 3.dp, 0.dp, 0.dp)
                )
                if (media.nameEN != null && !media.name.equals(media.nameEN, true)) {
                    Text(
                        media.nameEN!!,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false,
                        modifier = Modifier.padding(10.dp, 3.dp, 0.dp, 0.dp).align(Alignment.Start),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                if (media.nameJP != null && !media.name.equals(media.nameJP, true) && !media.nameJP.equals(
                        media.nameEN,
                        true
                    )
                ) {
                    Text(
                        media.nameJP!!,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false,
                        modifier = Modifier.padding(10.dp, 3.dp, 0.dp, 0.dp).align(Alignment.Start),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}