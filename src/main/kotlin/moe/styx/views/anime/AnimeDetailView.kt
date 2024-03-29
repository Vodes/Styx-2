package moe.styx.views.anime

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.russhwolf.settings.get
import moe.styx.common.compose.components.anime.*
import moe.styx.common.compose.components.buttons.FavouriteIconButton
import moe.styx.common.compose.components.layout.MainScaffold
import moe.styx.common.compose.components.misc.OnlineUsersIcon
import moe.styx.common.compose.extensions.getPainter
import moe.styx.common.compose.extensions.getThumb
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.files.getCurrentAndCollectFlow
import moe.styx.common.compose.settings
import moe.styx.common.compose.threads.Heartbeats
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.data.Media
import moe.styx.common.data.MediaEntry
import moe.styx.common.extension.eqI
import moe.styx.components.anime.AppendDialog
import moe.styx.components.anime.BigScalingCardImage
import moe.styx.components.anime.FailedDialog
import moe.styx.logic.runner.currentPlayer
import moe.styx.logic.runner.launchMPV
import moe.styx.logic.utils.*
import moe.styx.theme.AppShapes
import moe.styx.views.settings.SettingsView
import java.awt.Desktop
import java.net.URI

class AnimeDetailView(private val mediaID: String) : Screen {

    override val key: ScreenKey
        get() = mediaID

    @Preview
    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current

        val mediaList by Storage.stores.mediaStore.getCurrentAndCollectFlow()
        val media = remember { mediaList.find { it.GUID eqI mediaID } }
        if (media == null) {
            nav.pop()
            return
        }
        val entries = fetchEntries(mediaID)

        val preferGerman = settings["prefer-german-metadata", false]
        val scrollState = rememberScrollState()
        val showSelection = remember { mutableStateOf(false) }

        MainScaffold(title = media.name, actions = {
            OnlineUsersIcon { nav.pushMediaView(it, true) }
            FavouriteIconButton(media)
        }) {
            var failedToPlayMessage by remember { mutableStateOf("") }
            if (failedToPlayMessage.isNotBlank()) {
                FailedDialog(failedToPlayMessage, Modifier.fillMaxWidth(0.6F)) {
                    failedToPlayMessage = ""
                    if (it) nav.push(SettingsView())
                }
            }
            var appendEntry by remember { mutableStateOf<MediaEntry?>(null) }
            if (appendEntry != null) {
                AppendDialog(appendEntry!!, Modifier.fillMaxWidth(0.6F), onDismiss = {
                    appendEntry = null
                }) {
                    failedToPlayMessage = it
                    appendEntry = null
                }
            }
            ElevatedCard(
                Modifier.padding(8.dp).fillMaxSize(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(8.dp)
            ) {
                Row(Modifier.padding(5.dp).fillMaxSize()) {
                    Column(Modifier.fillMaxHeight().fillMaxWidth(.52F).verticalScroll(scrollState)) {
                        StupidImageNameArea(media)

                        Spacer(Modifier.height(6.dp))

                        Text("About", Modifier.padding(6.dp, 2.dp), style = MaterialTheme.typography.titleLarge)
                        MediaGenreListing(media)
                        val synopsis = if (!media.synopsisDE.isNullOrBlank() && preferGerman) media.synopsisDE else media.synopsisEN
                        if (!synopsis.isNullOrBlank())
                            SelectionContainer {
                                Text(synopsis.removeSomeHTMLTags(), Modifier.padding(6.dp), style = MaterialTheme.typography.bodyMedium)
                            }

                        if (media.sequel != null || media.prequel != null) {
                            HorizontalDivider(Modifier.fillMaxWidth().padding(0.dp, 4.dp, 0.dp, 2.dp), thickness = 2.dp)
                            MediaRelations(media, mediaList) { nav.pushMediaView(it, true) }
                        }
                    }
                    VerticalDivider(Modifier.fillMaxHeight().padding(6.dp), thickness = 3.dp)

                    EpisodeList(entries, showSelection, SettingsView(), onPlay = { entry ->
                        if (currentPlayer == null) {
                            launchMPV(entry, false, {
                                failedToPlayMessage = it
                            })
                        } else {
                            appendEntry = entry
                        }
                        ""
                    })
                }
            }
        }
    }
}

@Composable
fun StupidImageNameArea(
    media: Media,
    dynamicMaxWidth: Dp = 760.dp,
    requiredWidth: Dp = 385.dp,
    requiredHeight: Dp = 535.dp,
    otherContent: @Composable () -> Unit = {}
) {
    val img = media.getThumb()!!
    val painter = img.getPainter()
    BoxWithConstraints {
        val width = this.maxWidth
        Row(Modifier.align(Alignment.TopStart).height(IntrinsicSize.Max).fillMaxWidth()) {
            if (width <= dynamicMaxWidth)
                BigScalingCardImage(painter, Modifier.fillMaxWidth().weight(1f, false))
            else {
                // Theoretical max size that should be reached at this window width
                // Just force to not have layout spacing issues lmao
                BigScalingCardImage(painter, Modifier.requiredSize(requiredWidth, requiredHeight))
            }
            Column(Modifier.fillMaxWidth().weight(1f, true)) {
                MediaNameListing(media, Modifier.align(Alignment.Start))//, Modifier.weight(0.5F))
                otherContent()
                Spacer(Modifier.weight(1f, true))
                MappingIcons(media)
            }
        }
    }
}

@Composable
fun fetchEntries(mediaID: String): List<MediaEntry> {
    Heartbeats.mediaActivity = null
    val flow by Storage.stores.entryStore.getCurrentAndCollectFlow()
    val filtered = flow.filter { it.mediaID eqI mediaID }
    return if (settings["episode-asc", false]) filtered.sortedBy {
        it.entryNumber.toDoubleOrNull() ?: 0.0
    } else filtered.sortedByDescending { it.entryNumber.toDoubleOrNull() ?: 0.0 }
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