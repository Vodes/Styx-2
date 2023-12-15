package moe.styx.moe.styx.views.anime.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.russhwolf.settings.get
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource
import moe.styx.dataManager
import moe.styx.moe.styx.components.misc.createTabOptions
import moe.styx.moe.styx.components.overviews.MediaGrid
import moe.styx.moe.styx.components.overviews.MediaList
import moe.styx.moe.styx.components.overviews.MediaSearch
import moe.styx.moe.styx.navigation.LocalGlobalNavigator
import moe.styx.moe.styx.views.other.SubwaySurfers
import moe.styx.settings
import moe.styx.types.toBoolean

class AnimeListView() : Tab {
    override val options: TabOptions
        @Composable
        get() {
            return createTabOptions("Shows", Icons.Default.Tv)
        }

    val mediaSearch = MediaSearch(dataManager.media.value.filter { it.isSeries.toBoolean() })
    
    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current
        val vm = rememberScreenModel { AnimeListViewModel(mediaSearch) }
        var list by remember { vm.listState }
        val useListView by remember { vm.useListViewState }
        var showSubwaySurfers by remember { mutableStateOf(false) }
        Column {
            mediaSearch.component({
                list = it
                showSubwaySurfers = mediaSearch.searchState.value.equals("SubwaySurfers", true)
            })

            if (showSubwaySurfers) {
                Card(Modifier.clip(RoundedCornerShape(40)).width(67.dp).height(65.dp).padding(3.dp).clickable {
                    nav.push(SubwaySurfers())
                }, elevation = 5.dp) {
                    KamelImage(
                        lazyPainterResource("https://static.wikia.nocookie.net/subwaysurf/images/4/4b/FirstAvatar.jpg"),
                        contentDescription = "Subway Surfers",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.padding(1.dp)
                    )
                }
            } else {
                if (useListView) {
                    MediaList(list)
                } else {
                    MediaGrid(list)
                }
            }
        }
    }
}

class AnimeListViewModel(search: MediaSearch) : ScreenModel {
    val listState = mutableStateOf(search.getDefault())
    val useListViewState = mutableStateOf(settings["shows-list", false])
}