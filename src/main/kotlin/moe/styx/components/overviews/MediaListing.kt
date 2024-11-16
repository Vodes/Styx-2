package moe.styx.components.overviews

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.styx.common.compose.components.anime.AnimeCard
import moe.styx.common.compose.components.anime.AnimeListItem
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.viewmodels.ListPosViewModel
import moe.styx.common.compose.viewmodels.MainDataViewModelStorage
import moe.styx.common.data.Media
import moe.styx.common.extension.eqI
import moe.styx.logic.utils.pushMediaView

@Composable
fun MediaGrid(storage: MainDataViewModelStorage, mediaList: List<Media>, listPosViewModel: ListPosViewModel, showUnseen: Boolean = false) {
    val nav = LocalGlobalNavigator.current
    val listState = rememberLazyGridState(listPosViewModel.scrollIndex, listPosViewModel.scrollOffset)
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            listPosViewModel.scrollIndex = listState.firstVisibleItemIndex
            listPosViewModel.scrollOffset = listState.firstVisibleItemScrollOffset
        }
    }
    if (showUnseen) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(10.dp, 7.dp),
            state = listState
        ) {
            items(mediaList, key = { it.GUID }) {
                Row(modifier = Modifier.animateItem()) {
                    AnimeCard(
                        it to storage.imageList.find { img -> img.GUID eqI it.thumbID },
                        true,
                        entryList = storage.entryList,
                        watchedEntries = storage.watchedList
                    ) { nav.pushMediaView(it) }
                }
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(10.dp, 7.dp),
            state = listState
        ) {
            items(mediaList, key = { it.GUID }) {
                Row(modifier = Modifier.animateItem()) {
                    AnimeCard(it to storage.imageList.find { img -> img.GUID eqI it.thumbID }) { nav.pushMediaView(it) }
                }
            }
        }
    }
}

@Composable
fun MediaList(storage: MainDataViewModelStorage, mediaList: List<Media>, listPosViewModel: ListPosViewModel) {
    val nav = LocalGlobalNavigator.current
    val listState = rememberLazyListState(listPosViewModel.scrollIndex, listPosViewModel.scrollOffset)
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            listPosViewModel.scrollIndex = listState.firstVisibleItemIndex
            listPosViewModel.scrollOffset = listState.firstVisibleItemScrollOffset
        }
    }
    LazyColumn(state = listState) {
        items(mediaList, key = { it.GUID }) {
            Row(Modifier.animateItem().padding(3.dp)) {
                AnimeListItem(it, storage.imageList.find { img -> img.GUID eqI it.thumbID }) { nav.pushMediaView(it) }
            }
        }
    }
}