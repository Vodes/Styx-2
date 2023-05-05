package moe.styx.moe.styx.views.anime

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import io.kamel.core.Resource
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource
import moe.styx.dataManager
import moe.styx.logic.data.Media
import moe.styx.logic.data.MediaEntry
import moe.styx.moe.styx.components.MainScaffold
import moe.styx.moe.styx.components.anime.EpisodeList
import moe.styx.moe.styx.components.anime.MediaGenreListing
import moe.styx.moe.styx.components.anime.MediaNameListing
import moe.styx.moe.styx.components.anime.MediaRelations
import moe.styx.moe.styx.logic.data.getFile
import moe.styx.moe.styx.logic.data.getImageFromID
import moe.styx.moe.styx.logic.data.getURL
import moe.styx.moe.styx.logic.data.isCached
import moe.styx.moe.styx.navigation.LocalGlobalNavigator
import java.util.*

class AnimeDetailView(val ID: String) : Screen {

    private var sKey: String? = null

    private fun generateKey(): String {
        if (sKey == null)
            sKey = UUID.randomUUID().toString()
        return sKey as String
    }

    override val key: ScreenKey
        get() = generateKey()

    @Preview
    @Composable
    override fun Content() {
        val scaffoldState = rememberScaffoldState()
        val nav = LocalGlobalNavigator.current
        val vm = rememberScreenModel { AnimeDetailViewModel(ID) }

        if (vm.anime == null) {
            nav.pop();
            return
        }
        val episodes = vm.getEpisodes()

        val scrollState = rememberScrollState()
        val showSelection = remember { mutableStateOf(false) }

        MainScaffold(scaffoldState, title = vm.anime.name) {
            Card(Modifier.padding(8.dp).fillMaxSize(), elevation = 9.dp) {
                Row(Modifier.padding(5.dp).fillMaxSize()) {
                    Column(Modifier.fillMaxHeight().fillMaxWidth(.55F).verticalScroll(scrollState)) {
                        StupidImageNameArea(vm.anime)

                        Spacer(Modifier.height(6.dp))

                        Text("About", Modifier.padding(6.dp, 2.dp), style = MaterialTheme.typography.h6)
                        MediaGenreListing(vm.anime)
                        if (!vm.anime.synopsisEN.isNullOrBlank())
                            SelectionContainer {
                                Text(vm.anime.synopsisEN, Modifier.padding(6.dp), style = MaterialTheme.typography.caption)
                            }

                        if (vm.anime.sequel != null || vm.anime.prequel != null) {
                            Divider(Modifier.fillMaxWidth().padding(0.dp, 4.dp, 0.dp, 2.dp), thickness = 3.dp)
                            MediaRelations(vm.anime)
                        }
                    }
                    Column(Modifier.padding(6.dp).width(2.dp).fillMaxHeight()) {
                        Divider(Modifier.fillMaxHeight(), thickness = 3.dp)
                    }

                    EpisodeList(episodes, showSelection)
                }
            }
        }
    }
}

@Composable
fun StupidImageNameArea(media: Media) {
    val img = media.thumbID.getImageFromID()!!
    val imageResource = lazyPainterResource(
        if (img.isCached()) img.getFile() else img.getURL(),
        filterQuality = FilterQuality.High
    )
    BoxWithConstraints {
        val width = this.maxWidth
        Row(Modifier.align(Alignment.TopStart).height(IntrinsicSize.Max).fillMaxWidth()) {
            if (width <= 760.dp)
                BigScalingCardImage(imageResource, Modifier.fillMaxWidth().weight(1f, false))
            else {
                // Theoretical max size that should be reached at this window width
                // Just force to not have layout spacing issues lmao
                BigScalingCardImage(imageResource, Modifier.requiredSize(385.dp, 535.dp))
            }
            Column(Modifier.fillMaxWidth().weight(1f, true)) {
                MediaNameListing(media, Modifier.align(Alignment.Start))//, Modifier.weight(0.5F))
                Spacer(Modifier.weight(1f, true))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val filter = ColorFilter.tint(MaterialTheme.colors.onSurface)
                    IconButton({ println("Test") }, Modifier.padding(7.dp, 15.dp).size(25.dp)) {
                        Image(painterResource("icons/al.svg"), "Anilist", colorFilter = filter)
                    }
                    IconButton({ println("Test") }, Modifier.padding(7.dp, 15.dp).size(35.dp)) {
                        Image(painterResource("icons/myanimelist.svg"), "MyAnimeList", colorFilter = filter)
                    }
                    IconButton({ println("Test") }, Modifier.padding(7.dp, 15.dp).size(25.dp)) {
                        Image(painterResource("icons/tmdb.svg"), "TheMovieDB", colorFilter = filter)
                    }
                }
            }
        }
    }
}

@Composable
fun BigScalingCardImage(image: Resource<Painter>, modifier: Modifier = Modifier) {
    Column(modifier) {
        Card(
            Modifier.align(Alignment.Start).padding(12.dp).requiredHeightIn(150.dp, 500.dp).aspectRatio(0.71F),
            elevation = 2.dp
        ) {
            KamelImage(
                image,
                contentDescription = "Anime",
                modifier = Modifier.padding(2.dp).clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.FillBounds
            )
        }
    }
}

class AnimeDetailViewModel(val ID: String) : ScreenModel {
    val anime = dataManager.media.value.find { a -> a.GUID == ID }
    private var episodes = listOf<MediaEntry>()

    fun getEpisodes(): List<MediaEntry> {
        if (episodes.isNotEmpty())
            return episodes

        episodes = dataManager.entries.value.filter { it.mediaID == anime!!.GUID }.sortedByDescending { it.entryNumber }
        return episodes
    }
}