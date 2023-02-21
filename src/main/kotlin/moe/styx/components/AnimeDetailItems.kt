package moe.styx.moe.styx.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import moe.styx.logic.data.Media

@Composable
fun MediaNameListing(media: Media) {
    Column(Modifier.padding(10.dp, 20.dp).fillMaxWidth()) {
        if (media.nameEN != null) {
            Text(
                media.nameEN,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colors.onBackground,
                style = MaterialTheme.typography.body1,
                maxLines = 4
            )
        }
        if (media.nameJP != null && (media.nameEN == null || !media.nameEN.equals(
                media.nameJP,
                true
            ))
        )
            Text(
                media.nameJP,
                Modifier.padding(2.dp, 10.dp),
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.subtitle2
            )
    }
}

@Composable
fun MediaGenreListing(media: Media) {
    if (!media.genres.isNullOrBlank()) {
        Row(Modifier.padding(5.dp)) {
            val shape = RoundedCornerShape(16.dp)
            for (genre in media.genres.split(",")) {
                Surface(
                    Modifier.clip(shape).padding(7.dp).height(29.dp),
                    shape = shape,
                    color = MaterialTheme.colors.surface,
                    border = BorderStroke(2.dp, MaterialTheme.colors.primary),
                    contentColor = MaterialTheme.colors.primary
                ) {
                    Row {
                        Text(
                            genre,
                            Modifier.padding(7.dp).align(Alignment.CenterVertically),
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
        }
    }
}