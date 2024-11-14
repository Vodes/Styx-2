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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.styx.common.compose.components.anime.AnimeCard
import moe.styx.common.compose.components.anime.AnimeListItem
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.files.collectWithEmptyInitial
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.data.Media
import moe.styx.common.extension.eqI
import moe.styx.logic.utils.pushMediaView

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaGrid(media: List<Media>, showUnseen: Boolean = false) {
    val nav = LocalGlobalNavigator.current
    val imageList by Storage.stores.imageStore.collectWithEmptyInitial()
    if (showUnseen) {
        val entryList by Storage.stores.entryStore.collectWithEmptyInitial()
        val watchedList by Storage.stores.watchedStore.collectWithEmptyInitial()
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(10.dp, 7.dp),
        ) {
            items(media, key = { it.GUID }) {
                Row(modifier = Modifier.animateItem()) {
                    AnimeCard(
                        it to imageList.find { img -> img.GUID eqI it.thumbID },
                        showUnseen,
                        entryList = entryList,
                        watchedEntries = watchedList
                    ) { nav.pushMediaView(it) }
                }
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(10.dp, 7.dp),
        ) {
            items(media, key = { it.GUID }) {
                Row(modifier = Modifier.animateItem()) {
                    AnimeCard(it to imageList.find { img -> img.GUID eqI it.thumbID }, showUnseen) { nav.pushMediaView(it) }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaList(media: List<Media>) {
    val nav = LocalGlobalNavigator.current
    val imageList by Storage.stores.imageStore.collectWithEmptyInitial()
    LazyColumn {
        items(media, key = { it.GUID }) {
            Row(Modifier.animateItem().padding(3.dp)) {
                AnimeListItem(it, imageList.find { img -> img.GUID eqI it.thumbID }) { nav.pushMediaView(it) }
            }
        }
    }
}