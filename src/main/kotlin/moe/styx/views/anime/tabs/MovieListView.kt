package moe.styx.moe.styx.views.anime.tabs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.russhwolf.settings.get
import moe.styx.dataManager
import moe.styx.moe.styx.components.*
import moe.styx.settings
import moe.styx.toBoolean

@OptIn(ExperimentalMaterialApi::class)
class MovieListView() : Tab {
    override val options: TabOptions
        @Composable
        get() {
            return createTabOptions("Movies", Icons.Default.Movie)
        }

    val mediaSearch = MediaSearch(dataManager.media.value.filter { !it.isSeries.toBoolean() })

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val list = remember { mutableStateOf(mediaSearch.getDefault(false)) }
        val useListView = remember { mutableStateOf(settings["movies-list", false]) }

        Column {
            mediaSearch.component({ list.value = it }, false)

            if (useListView.value) {
                MediaList(list)
            } else {
                MediaGrid(list)
            }
        }
    }
}