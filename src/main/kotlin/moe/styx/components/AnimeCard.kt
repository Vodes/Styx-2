package moe.styx.components

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource
import moe.styx.logic.data.Media
import moe.styx.logic.data.getImageFromID
import moe.styx.logic.data.getURL
import moe.styx.views.anime.AnimeView

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AnimeCard(nav: Navigator, media: Media) {
    val image = media.thumbID.getImageFromID()
    Card(modifier = Modifier.padding(2.dp).aspectRatio(0.71F), onClick = {
        nav.push(AnimeView(media.name))
    }) {
        if (image != null) {
            KamelImage(
                lazyPainterResource(image.getURL(), filterQuality = FilterQuality.Low),
                contentDescription = "Anime",
                modifier = Modifier.padding(2.dp),
                contentScale = ContentScale.FillBounds
            )
        }
    }
}