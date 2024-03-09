package moe.styx.components.anime

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.kamel.core.Resource
import io.kamel.image.KamelImage

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
