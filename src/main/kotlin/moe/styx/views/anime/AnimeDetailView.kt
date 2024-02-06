package moe.styx.views.anime

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.russhwolf.settings.get
import io.kamel.image.lazyPainterResource
import moe.styx.Main.settings
import moe.styx.components.MainScaffold
import moe.styx.components.anime.*
import moe.styx.components.misc.FavouriteIconButton
import moe.styx.logic.data.*
import moe.styx.logic.utils.StackType
import moe.styx.logic.utils.getURLFromMap
import moe.styx.logic.utils.removeSomeHTMLTags
import moe.styx.navigation.LocalGlobalNavigator
import moe.styx.theme.AppShapes
import moe.styx.types.Media
import moe.styx.types.MediaEntry
import java.awt.Desktop
import java.net.URI
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
        val nav = LocalGlobalNavigator.current
        val vm = rememberScreenModel { AnimeDetailViewModel(ID) }

        if (vm.anime == null) {
            nav.pop();
            return
        }
        val episodes = vm.getEpisodes()

        val preferGerman = settings["prefer-german-metadata", false]
        val scrollState = rememberScrollState()
        val showSelection = remember { mutableStateOf(false) }

        MainScaffold(title = vm.anime.name, actions = {
            FavouriteIconButton(vm.anime)
        }) {
            ElevatedCard(
                Modifier.padding(8.dp).fillMaxSize(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(Modifier.padding(5.dp).fillMaxSize()) {
                    Column(Modifier.fillMaxHeight().fillMaxWidth(.52F).verticalScroll(scrollState)) {
                        StupidImageNameArea(vm.anime)

                        Spacer(Modifier.height(6.dp))

                        Text("About", Modifier.padding(6.dp, 2.dp), style = MaterialTheme.typography.titleLarge)
                        MediaGenreListing(vm.anime)
                        val synopsis = if (!vm.anime.synopsisDE.isNullOrBlank() && preferGerman) vm.anime.synopsisDE else vm.anime.synopsisEN
                        if (!synopsis.isNullOrBlank())
                            SelectionContainer {
                                Text(synopsis.removeSomeHTMLTags(), Modifier.padding(6.dp), style = MaterialTheme.typography.bodyMedium)
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
                MappingIcons(media)
            }
        }
    }
}

class AnimeDetailViewModel(val ID: String) : ScreenModel {
    val anime = DataManager.media.value.find { a -> a.GUID == ID }
    private var episodes = listOf<MediaEntry>()

    fun getEpisodes(): List<MediaEntry> {
        if (episodes.isNotEmpty())
            return episodes

        episodes = DataManager.entries.value.filter { it.mediaID == anime!!.GUID }.sortedByDescending { it.entryNumber.toDoubleOrNull() ?: 0.0 }
        return episodes
    }
}

@Composable
fun MappingIcons(media: Media) {
    val malURL = media.getURLFromMap(StackType.MAL)
    val anilistURL = media.getURLFromMap(StackType.ANILIST)
    val tmdbURL = media.getURLFromMap(StackType.TMDB)
    Row(Modifier.padding(0.dp, 0.dp, 0.dp, 15.dp), verticalAlignment = Alignment.CenterVertically) {
        val filter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
        if (!anilistURL.isNullOrBlank())
            Image(
                painterResource("icons/al.svg"),
                "AniList",
                Modifier.padding(8.dp, 3.dp).size(25.dp).clip(AppShapes.small).clickable {
                    if (Desktop.isDesktopSupported())
                        Desktop.getDesktop().browse(URI(anilistURL))
                },
                contentScale = ContentScale.FillWidth,
                colorFilter = filter
            )
        if (!malURL.isNullOrBlank())
            Image(
                painterResource("icons/myanimelist.svg"),
                "MyAnimeList",
                Modifier.padding(8.dp, 3.dp).size(25.dp).clip(AppShapes.small).clickable {
                    if (Desktop.isDesktopSupported())
                        Desktop.getDesktop().browse(URI(malURL))
                },
                contentScale = ContentScale.FillWidth,
                colorFilter = filter
            )
        if (!tmdbURL.isNullOrBlank())
            Image(
                painterResource("icons/tmdb.svg"),
                "TheMovieDB",
                Modifier.padding(8.dp, 3.dp).size(25.dp).clip(AppShapes.small).clickable {
                    if (Desktop.isDesktopSupported())
                        Desktop.getDesktop().browse(URI(tmdbURL))
                },
                contentScale = ContentScale.FillWidth,
                colorFilter = filter
            )
    }
}