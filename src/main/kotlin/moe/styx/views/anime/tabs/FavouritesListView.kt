package moe.styx.views.anime.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import kotlinx.coroutines.runBlocking
import moe.styx.common.compose.components.search.MediaSearch
import moe.styx.common.compose.extensions.SimpleTab
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.files.getCurrentAndCollectFlow
import moe.styx.common.compose.utils.SearchState
import moe.styx.common.extension.eqI
import moe.styx.views.barWithListComp

class FavouritesListView : SimpleTab("Favourites", Icons.Default.Star) {

    @Composable
    override fun Content() {
        val media by Storage.stores.mediaStore.getCurrentAndCollectFlow()
        Column {
            val favourites by Storage.stores.favouriteStore.getCurrentAndCollectFlow()
            val searchStore = Storage.stores.favSearchState
            val filtered = media.filter { m -> favourites.find { m.GUID eqI it.mediaID } != null }
            val initialState = runBlocking { searchStore.get() ?: SearchState() }
            val mediaSearch = MediaSearch(searchStore, initialState, emptyList(), emptyList(), true)
            barWithListComp(mediaSearch, initialState, filtered, false, true, favourites)
        }
    }
}