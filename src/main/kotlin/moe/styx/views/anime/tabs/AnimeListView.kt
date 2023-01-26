package moe.styx.moe.styx.views.anime.tabs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.russhwolf.settings.get
import moe.styx.components.AnimeCard
import moe.styx.dataManager
import moe.styx.moe.styx.components.AnimeListItem
import moe.styx.moe.styx.components.MediaSearch
import moe.styx.moe.styx.components.createTabOptions
import moe.styx.moe.styx.navigation.LocalGlobalNavigator
import moe.styx.settings
import moe.styx.toBoolean

class AnimeListView() : Tab {
    init {
        println("Creating AnimeLIst")
    }

    override val options: TabOptions
        @Composable
        get() {
            return createTabOptions("Shows", Icons.Default.Tv)
        }

    val mediaSearch = MediaSearch(dataManager.media.value.filter { it.isSeries.toBoolean() })

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val list = remember { mutableStateOf(mediaSearch.getDefault()) }
        val useListView = remember { mutableStateOf(settings["shows-list", false]) }

        Column {
            mediaSearch.component({ list.value = it })
            if (useListView.value) {
                LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(4.dp, 10.dp), content = {
                    itemsIndexed(
                        items = list.value, key = { _, item -> item.GUID },
                    ) { _, item ->
                        Row(modifier = Modifier.animateItemPlacement()) {
                            AnimeListItem(LocalGlobalNavigator.current, item)
                        }
                    }
                })
            } else {
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
        }
    }
}