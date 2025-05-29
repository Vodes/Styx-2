package moe.styx.views.anime

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.dokar.sonner.TextToastAction
import com.dokar.sonner.Toast
import com.dokar.sonner.ToastType
import com.russhwolf.settings.get
import moe.styx.common.compose.components.anime.*
import moe.styx.common.compose.components.buttons.FavouriteIconButton
import moe.styx.common.compose.components.layout.MainScaffold
import moe.styx.common.compose.components.misc.OnlineUsersIcon
import moe.styx.common.compose.extensions.joinAndSyncProgress
import moe.styx.common.compose.settings
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.utils.LocalToaster
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.data.MediaEntry
import moe.styx.components.anime.AppendDialog
import moe.styx.logic.runner.currentPlayer
import moe.styx.logic.runner.launchMPV
import moe.styx.logic.utils.pushMediaView
import moe.styx.logic.utils.removeSomeHTMLTags
import moe.styx.views.settings.SettingsTab
import moe.styx.views.settings.SettingsView

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
                        StupidImageNameArea(
                            mediaStorage,
                            requiredMaxHeight = 535.dp,
                            enforceConstraints = true,
                            mappingIconModifier = Modifier.padding(8.dp, 5.dp, 8.dp, 12.dp).size(30.dp)
                        )

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