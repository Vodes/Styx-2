package moe.styx.views.anime.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import com.russhwolf.settings.get
import kotlinx.coroutines.runBlocking
import moe.styx.common.compose.components.search.MediaSearch
import moe.styx.common.compose.extensions.SimpleTab
import moe.styx.common.compose.extensions.getDistinctCategories
import moe.styx.common.compose.extensions.getDistinctGenres
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.utils.SearchState
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.extension.toBoolean
import moe.styx.views.barWithListComp

class AnimeListView : SimpleTab("Shows", Icons.Default.Tv) {

    @Composable
    override fun Content() {
        val searchStore = Storage.stores.showSearchState
        val nav = LocalGlobalNavigator.current
        val sm = nav.rememberNavigatorScreenModel("main-vm") { MainDataViewModel() }
        val storage by sm.storageFlow.collectAsState()

        val filtered = storage.mediaList.filter { it.isSeries.toBoolean() }
        val availableGenres = filtered.getDistinctGenres()
        val availableCategories = filtered.getDistinctCategories(storage.categoryList)
        val initialState = runBlocking { searchStore.get() ?: SearchState() }
        val mediaSearch = MediaSearch(searchStore, initialState, availableGenres, availableCategories)
        barWithListComp(mediaSearch, initialState, filtered, moe.styx.common.compose.settings["shows-list", false])
    }
}