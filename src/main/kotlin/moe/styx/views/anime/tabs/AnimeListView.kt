package moe.styx.moe.styx.views.anime.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.russhwolf.settings.get
import moe.styx.dataManager
import moe.styx.moe.styx.components.misc.createTabOptions
import moe.styx.moe.styx.components.overviews.MediaGrid
import moe.styx.moe.styx.components.overviews.MediaList
import moe.styx.moe.styx.components.overviews.MediaSearch
import moe.styx.settings
import moe.styx.toBoolean

class AnimeListView() : Tab {
    override val options: TabOptions
        @Composable
        get() {
            return createTabOptions("Shows", Icons.Default.Tv)
        }

    val mediaSearch = MediaSearch(dataManager.media.value.filter { it.isSeries.toBoolean() })

    @Composable
    override fun Content() {
        val vm = rememberScreenModel { AnimeListViewModel(mediaSearch) }
        val list = remember { vm.listState }
        val useListView = remember { vm.useListViewState }
        Column {
            mediaSearch.component({ list.value = it })

            if (useListView.value) {
                MediaList(list)
            } else {
                MediaGrid(list)
            }
        }
    }
}

class AnimeListViewModel(search: MediaSearch) : ScreenModel {
    val listState = mutableStateOf(search.getDefault())
    val useListViewState = mutableStateOf(settings["shows-list", false])
}