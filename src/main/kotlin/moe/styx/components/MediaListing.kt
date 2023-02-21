package moe.styx.moe.styx.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.styx.components.AnimeCard
import moe.styx.logic.data.Media
import moe.styx.moe.styx.navigation.LocalGlobalNavigator

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaGrid(media: MutableState<List<Media>>) {
    val list = remember { media }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(10.dp, 7.dp),
    ) {
        itemsIndexed(
            items = list.value, key = { _, item -> item.GUID },
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
    val list = remember { media }
    LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(4.dp, 10.dp), content = {
        itemsIndexed(
            items = list.value, key = { _, item -> item.GUID },
        ) { _, item ->
            Row(modifier = Modifier.animateItemPlacement()) {
                AnimeListItem(LocalGlobalNavigator.current, item)
            }
        }
    })
}