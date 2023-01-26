package moe.styx.moe.styx.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource
import moe.styx.logic.data.Media
import moe.styx.logic.data.getImageFromID
import moe.styx.logic.data.getURL
import moe.styx.views.anime.AnimeView

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun AnimeListItem(nav: Navigator, media: Media) {
    val image = media.thumbID.getImageFromID()
    Card(modifier = Modifier.padding(2.dp).fillMaxWidth(), onClick = {
        nav.push(AnimeView(media.name))
    }) {
        Row(Modifier.height(80.dp)) {
            Card(
                Modifier.clip(RoundedCornerShape(40)).width(67.dp).padding(3.dp),
                elevation = 5.dp
            ) {
                if (image != null) {
                    KamelImage(
                        lazyPainterResource(image.getURL(), filterQuality = FilterQuality.Low),
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
                        media.nameEN,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false,
                        modifier = Modifier.padding(10.dp, 3.dp, 0.dp, 0.dp).align(Alignment.Start),
                        style = MaterialTheme.typography.caption
                    )
                }
                if (media.nameJP != null && !media.name.equals(media.nameJP, true) && !media.nameJP.equals(
                        media.nameEN,
                        true
                    )
                ) {
                    Text(
                        media.nameJP,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false,
                        modifier = Modifier.padding(10.dp, 3.dp, 0.dp, 0.dp).align(Alignment.Start),
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        }
    }
}