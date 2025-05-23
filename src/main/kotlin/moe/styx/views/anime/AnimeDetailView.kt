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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.dokar.sonner.TextToastAction
import com.dokar.sonner.Toast
import com.dokar.sonner.ToastType
import com.russhwolf.settings.get
import moe.styx.common.compose.components.AppShapes
import moe.styx.common.compose.components.anime.*
import moe.styx.common.compose.components.buttons.FavouriteIconButton
import moe.styx.common.compose.components.layout.MainScaffold
import moe.styx.common.compose.components.misc.OnlineUsersIcon
import moe.styx.common.compose.components.tracking.anilist.AnilistBottomSheetModel
import moe.styx.common.compose.components.tracking.anilist.AnilistButtomSheet
import moe.styx.common.compose.extensions.getPainter
import moe.styx.common.compose.extensions.joinAndSyncProgress
import moe.styx.common.compose.settings
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.utils.LocalToaster
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.compose.viewmodels.MediaStorage
import moe.styx.common.data.MediaEntry
import moe.styx.common.data.tmdb.StackType
import moe.styx.components.anime.AppendDialog
import moe.styx.components.anime.BigScalingCardImage
import moe.styx.logic.runner.currentPlayer
import moe.styx.logic.runner.launchMPV
import moe.styx.logic.runner.openURI
import moe.styx.logic.utils.getURLFromMap
import moe.styx.logic.utils.pushMediaView
import moe.styx.logic.utils.removeSomeHTMLTags
import moe.styx.styx_common_compose.generated.resources.*
import moe.styx.views.settings.SettingsTab
import moe.styx.views.settings.SettingsView
import org.jetbrains.compose.resources.painterResource

class AnimeDetailView(private val mediaID: String) : Screen {

    override val key: ScreenKey
        get() = mediaID

    @Preview
    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current
        val toaster = LocalToaster.current
        val sm = nav.rememberNavigatorScreenModel("main-vm") { MainDataViewModel() }
        val storage by sm.storageFlow.collectAsState()
        val mediaStorage = remember(storage) { sm.getMediaStorageForID(mediaID, storage) }

        val preferGerman = remember { settings["prefer-german-metadata", false] }
        val scrollState = rememberScrollState()
        val showSelection = remember { mutableStateOf(false) }

        MainScaffold(title = mediaStorage.media.name, actions = {
            OnlineUsersIcon { nav.pushMediaView(it, true) }
            MediaPreferencesIconButton(mediaStorage.preferences, mediaStorage.media, sm)
            FavouriteIconButton(mediaStorage.media, sm, storage)
        }) {
            var failedToPlayMessage by remember { mutableStateOf("") }
            if (failedToPlayMessage.isNotBlank()) {
                toaster.show(Toast(failedToPlayMessage, type = ToastType.Error, action = TextToastAction("Open Settings") {
                    nav.push(SettingsView())
                }))
                failedToPlayMessage = ""
            }
            var appendEntry by remember { mutableStateOf<MediaEntry?>(null) }
            if (appendEntry != null) {
                AppendDialog(appendEntry!!, sm, Modifier.fillMaxWidth(0.6F), onDismiss = {
                    appendEntry = null
                }) { result ->
                    if (!result.isOK)
                        failedToPlayMessage = result.message
                    else
                        sm.updateData(true).also { job -> result.lastEntry?.let { job.joinAndSyncProgress(it, sm) } }
                }
            }
            ElevatedCard(
                Modifier.padding(8.dp).fillMaxSize()
            ) {
                Row(Modifier.padding(5.dp).fillMaxSize()) {
                    Column(Modifier.fillMaxHeight().fillMaxWidth(.52F).verticalScroll(scrollState)) {
                        StupidImageNameArea(mediaStorage)

                        Spacer(Modifier.height(6.dp))

                        Text("About", Modifier.padding(6.dp, 2.dp), style = MaterialTheme.typography.titleLarge)
                        MediaGenreListing(mediaStorage.media)
                        val synopsis =
                            if (!mediaStorage.media.synopsisDE.isNullOrBlank() && preferGerman) mediaStorage.media.synopsisDE else mediaStorage.media.synopsisEN
                        if (!synopsis.isNullOrBlank())
                            SelectionContainer {
                                Text(synopsis.removeSomeHTMLTags(), Modifier.padding(6.dp), style = MaterialTheme.typography.bodyMedium)
                            }

                        if (mediaStorage.sequel != null || mediaStorage.prequel != null) {
                            HorizontalDivider(Modifier.fillMaxWidth().padding(0.dp, 4.dp, 0.dp, 2.dp), thickness = 2.dp)
                            MediaRelations(mediaStorage) { nav.pushMediaView(it, true) }
                        }
                    }
                    VerticalDivider(Modifier.fillMaxHeight().padding(6.dp), thickness = 3.dp)

                    EpisodeList(storage, mediaStorage, showSelection, SettingsTab(), onPlay = { entry ->
                        if (currentPlayer == null) {
                            launchMPV(entry, sm, false) { result ->
                                if (!result.isOK)
                                    failedToPlayMessage = result.message
                                else
                                    sm.updateData(true).also { job -> result.lastEntry?.let { job.joinAndSyncProgress(it, sm) } }
                            }
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
    mediaStorage: MediaStorage,
    dynamicMaxWidth: Dp = 760.dp,
    requiredWidth: Dp = 385.dp,
    requiredHeight: Dp = 535.dp,
    otherContent: @Composable () -> Unit = {}
) {
    val (media, img) = mediaStorage.media to mediaStorage.image
    val painter = img?.getPainter()
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
                MappingIcons(mediaStorage)
            }
        }
    }
}

@Composable
fun MappingIcons(mediaStorage: MediaStorage) {
    val malURL = mediaStorage.media.getURLFromMap(StackType.MAL)
    val anilistURL = mediaStorage.media.getURLFromMap(StackType.ANILIST)
    val tmdbURL = mediaStorage.media.getURLFromMap(StackType.TMDB)
    val nav = LocalGlobalNavigator.current
    var showAnilistSheet by remember { mutableStateOf(false) }
    Row(Modifier.padding(0.dp, 0.dp, 0.dp, 15.dp), verticalAlignment = Alignment.CenterVertically) {
        val filter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
        if (!anilistURL.isNullOrBlank())
            Image(
                painterResource(Res.drawable.al),
                "AniList",
                Modifier.padding(8.dp, 3.dp).size(25.dp).clip(AppShapes.small).clickable {
//                    val test = mediaStorage.media.decodeMapping()?.mapLocalToRemote(StackType.ANILIST, mediaStorage.entries)
//                    println(test)
                    showAnilistSheet = true
                },
                contentScale = ContentScale.FillWidth,
                colorFilter = filter
            )
        if (!malURL.isNullOrBlank())
            Image(
                painterResource(Res.drawable.myanimelist),
                "MyAnimeList",
                Modifier.padding(8.dp, 3.dp).size(25.dp).clip(AppShapes.small).clickable {
                    openURI(malURL)
                },
                contentScale = ContentScale.FillWidth,
                colorFilter = filter
            )
        if (!tmdbURL.isNullOrBlank())
            Image(
                painterResource(Res.drawable.tmdb),
                "TheMovieDB",
                Modifier.padding(8.dp, 3.dp).size(25.dp).clip(AppShapes.small).clickable {
                    openURI(tmdbURL)
                },
                contentScale = ContentScale.FillWidth,
                colorFilter = filter
            )
    }
    if (showAnilistSheet) {
        val state = nav.rememberNavigatorScreenModel("al-sheet-${mediaStorage.media.GUID}") { AnilistBottomSheetModel() }
        val sm = nav.rememberNavigatorScreenModel("main-vm") { MainDataViewModel() }
        AnilistButtomSheet(mediaStorage, sm, state, { openURI(it) }) {
            showAnilistSheet = false
        }
    }
}