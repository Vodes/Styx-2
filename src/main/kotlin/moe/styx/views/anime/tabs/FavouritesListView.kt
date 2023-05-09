package moe.styx.moe.styx.views.anime.tabs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import moe.styx.dataManager
import moe.styx.logic.data.isFav
import moe.styx.moe.styx.components.anime.AnimeCard
import moe.styx.moe.styx.components.misc.createTabOptions
import moe.styx.moe.styx.components.overviews.MediaSearch
import moe.styx.moe.styx.navigation.LocalGlobalNavigator

class FavouritesListView() : Tab {
    override val options: TabOptions
        @Composable
        get() {
            return createTabOptions("Favourites", Icons.Default.Star)
        }

    val mediaSearch = MediaSearch(dataManager.media.value.filter { it.isFav() }, true)
    val searchState = mutableStateOf(mediaSearch.getDefault())

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val list = remember { searchState }

        Column {
            mediaSearch.component({ list.value = it })
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
                            item, true
                        )
                    }
                }
            }
        }
    }
}