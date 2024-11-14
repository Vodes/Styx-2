package moe.styx.views.anime.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import kotlinx.coroutines.runBlocking
import moe.styx.common.compose.components.search.MediaSearch
import moe.styx.common.compose.extensions.SimpleTab
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.utils.SearchState
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.extension.eqI
import moe.styx.views.barWithListComp

class FavouritesListView : SimpleTab("Favourites", Icons.Default.Star) {

    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current
        val sm = nav.rememberNavigatorScreenModel("main-vm") { MainDataViewModel() }
        val storage by sm.storageFlow.collectAsState()
        Column {
            val searchStore = Storage.stores.favSearchState
            val filtered = storage.mediaList.filter { m -> storage.favouritesList.find { m.GUID eqI it.mediaID } != null }
            val initialState = runBlocking { searchStore.get() ?: SearchState() }
            val mediaSearch = MediaSearch(searchStore, initialState, emptyList(), emptyList(), true)
            barWithListComp(mediaSearch, initialState, filtered, false, true, storage.favouritesList)
        }
    }
}