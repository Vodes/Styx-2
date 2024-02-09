package moe.styx.views.anime.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.*
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.russhwolf.settings.get
import moe.styx.Main.settings
import moe.styx.common.extension.toBoolean
import moe.styx.components.misc.createTabOptions
import moe.styx.components.overviews.MediaGrid
import moe.styx.components.overviews.MediaList
import moe.styx.components.overviews.MediaSearch
import moe.styx.logic.data.DataManager
import moe.styx.navigation.LocalGlobalNavigator

class AnimeListView() : Tab {
    override val options: TabOptions
        @Composable
        get() {
            return createTabOptions("Shows", Icons.Default.Tv)
        }

    private val mediaSearch = MediaSearch(DataManager.media.value.filter { it.isSeries.toBoolean() })

    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current
        val vm = rememberScreenModel { AnimeListViewModel(mediaSearch) }
        var list by remember { vm.listState }
        val useListView by remember { vm.useListViewState }
        Column {
            mediaSearch.component({ list = it })
            if (useListView) {
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