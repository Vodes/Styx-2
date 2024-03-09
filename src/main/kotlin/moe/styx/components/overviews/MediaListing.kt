package moe.styx.components.overviews

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.styx.common.compose.components.anime.AnimeCard
import moe.styx.common.compose.components.anime.AnimeListItem
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.data.Media
import moe.styx.common.extension.toBoolean
import moe.styx.views.anime.AnimeDetailView
import moe.styx.views.anime.MovieDetailView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaGrid(media: List<Media>, showUnseen: Boolean = false) {
    val nav = LocalGlobalNavigator.current
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(10.dp, 7.dp),
    ) {
        items(media, key = { it.GUID }) {
            Row(modifier = Modifier.animateItemPlacement()) {
                AnimeCard(it, showUnseen) {
                    if (it.isSeries.toBoolean()) {
                        nav.push(AnimeDetailView(it.GUID))
                    } else
                        nav.push(MovieDetailView(it.GUID))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaList(media: List<Media>) {
    val nav = LocalGlobalNavigator.current
    LazyColumn {
        items(media, key = { it.GUID }) {
            Row(Modifier.animateItemPlacement().padding(3.dp)) {
                AnimeListItem(it) {
                    if (it.isSeries.toBoolean()) {
                        nav.push(AnimeDetailView(it.GUID))
                    } else
                        nav.push(MovieDetailView(it.GUID))
                }
            }
        }
    }
}