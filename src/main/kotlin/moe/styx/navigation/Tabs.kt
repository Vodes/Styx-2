package moe.styx.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import moe.styx.common.compose.components.search.MediaSearch
import moe.styx.common.compose.utils.SearchState
import moe.styx.common.data.Media
import moe.styx.components.overviews.MediaGrid
import moe.styx.components.overviews.MediaList
import moe.styx.views.anime.tabs.*

val defaultTab = AnimeListView()
val movieTab = MovieListView()
var favsTab = FavouritesListView()
val scheduleTab = ScheduleView()

@OptIn(FlowPreview::class)
@Composable
internal fun Tab.barWithListComp(
    mediaSearch: MediaSearch,
    initialState: SearchState,
    filtered: List<Media>,
    useList: Boolean = false,
    showUnseen: Boolean = false
) {
    Column(Modifier.fillMaxSize()) {
        mediaSearch.Component(Modifier.fillMaxWidth().padding(10.dp))
        Column(Modifier.fillMaxSize()) {
            val flow by mediaSearch.stateEmitter.debounce(150L).collectAsState(initialState)
            val processedMedia = flow.filterMedia(filtered)
            if (!useList)
                MediaGrid(processedMedia, showUnseen)
            else
                MediaList(processedMedia)
        }
    }
}