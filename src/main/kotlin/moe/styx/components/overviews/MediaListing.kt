package moe.styx.moe.styx.components.overviews

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.styx.moe.styx.components.anime.AnimeCard
import moe.styx.moe.styx.components.anime.AnimeListItem
import moe.styx.moe.styx.navigation.LocalGlobalNavigator
import moe.styx.types.Media

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaGrid(media: MutableState<List<Media>>) {
    val list by remember { media }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(10.dp, 7.dp),
    ) {
        itemsIndexed(
            items = list, key = { _, item -> item.GUID },
        ) { _, item ->
            Row(modifier = Modifier.animateItemPlacement()) {
                AnimeCard(
                    LocalGlobalNavigator.current,
                    item
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaList(media: MutableState<List<Media>>) {
    val list by remember { media }
    LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(4.dp, 10.dp), content = {
        itemsIndexed(
            items = list, key = { _, item -> item.GUID },
        ) { _, item ->
            Row(modifier = Modifier.animateItemPlacement()) {
                AnimeListItem(LocalGlobalNavigator.current, item)
            }
        }
    })
}