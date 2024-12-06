package moe.styx.views.anime

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.dokar.sonner.TextToastAction
import com.dokar.sonner.Toast
import com.dokar.sonner.ToastType
import com.russhwolf.settings.get
import moe.styx.common.compose.components.anime.*
import moe.styx.common.compose.components.buttons.FavouriteIconButton
import moe.styx.common.compose.components.buttons.IconButtonWithTooltip
import moe.styx.common.compose.components.layout.MainScaffold
import moe.styx.common.compose.components.misc.OnlineUsersIcon
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.files.collectWithEmptyInitial
import moe.styx.common.compose.files.updateList
import moe.styx.common.compose.http.login
import moe.styx.common.compose.settings
import moe.styx.common.compose.threads.DownloadQueue
import moe.styx.common.compose.threads.RequestQueue
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.utils.LocalToaster
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.data.MediaEntry
import moe.styx.common.data.MediaWatched
import moe.styx.common.extension.currentUnixSeconds
import moe.styx.common.extension.eqI
import moe.styx.common.extension.toBoolean
import moe.styx.common.util.SYSTEMFILES
import moe.styx.common.util.launchThreaded
import moe.styx.components.anime.AppendDialog
import moe.styx.logic.runner.currentPlayer
import moe.styx.logic.runner.launchMPV
import moe.styx.logic.utils.pushMediaView
import moe.styx.logic.utils.readableSize
import moe.styx.logic.utils.removeSomeHTMLTags
import moe.styx.views.settings.SettingsView

class MovieDetailView(private val mediaID: String) : Screen {

    override val key: ScreenKey
        get() = mediaID


    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current
        val toaster = LocalToaster.current
        val sm = nav.rememberNavigatorScreenModel("main-vm") { MainDataViewModel() }
        val storage by sm.storageFlow.collectAsState()
        val mediaStorage = remember(storage) { sm.getMediaStorageForID(mediaID, storage) }
        val movieEntry = mediaStorage.entries.getOrNull(0)

        if (mediaStorage.image == null) {
            nav.pop()
            return
        }

        val watchedList by Storage.stores.watchedStore.collectWithEmptyInitial()
        val watched = movieEntry?.let { watchedList.find { it.entryID eqI movieEntry.GUID } }
        var showMediaInfoDialog by remember { mutableStateOf(false) }
        if (showMediaInfoDialog && movieEntry != null) {
            MediaInfoDialog(movieEntry) { showMediaInfoDialog = false }
        }

        MainScaffold(title = mediaStorage.media.name, actions = {
            OnlineUsersIcon { nav.replace(if (it.isSeries.toBoolean()) AnimeDetailView(it.GUID) else MovieDetailView(it.GUID)) }
            FavouriteIconButton(mediaStorage.media, sm, storage)
        }) {
            val scrollState = rememberScrollState()
            ElevatedCard(
                Modifier.padding(8.dp).fillMaxSize(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                var failedToPlayMessage by remember { mutableStateOf("") }
                if (failedToPlayMessage.isNotBlank()) {
                    toaster.show(Toast(failedToPlayMessage, type = ToastType.Error, action = TextToastAction("Open Settings") {
                        nav.push(SettingsView())
                    }))
                    failedToPlayMessage = ""
                }
                var showAppendDialog by remember { mutableStateOf(false) }
                if (showAppendDialog && movieEntry != null) {
                    AppendDialog(movieEntry, Modifier.fillMaxWidth(0.6F), Modifier.align(Alignment.CenterHorizontally), {
                        showAppendDialog = false
                    }) {
                        if (!it.isOK)
                            failedToPlayMessage = it.message
                        else
                            sm.updateData(true)
                    }
                }

                Column(Modifier.fillMaxSize().verticalScroll(scrollState)) {
                    StupidImageNameArea(mediaStorage) {
                        Column(Modifier.padding(6.dp).widthIn(0.dp, 560.dp).fillMaxWidth()) {
                            Row(Modifier.padding(3.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                IconButton({
                                    if (movieEntry == null)
                                        return@IconButton
                                    if (currentPlayer == null) {
                                        launchMPV(movieEntry, false) {
                                            if (!it.isOK)
                                                failedToPlayMessage = it.message
                                            else
                                                sm.updateData(true)
                                        }
                                    } else {
                                        showAppendDialog = true
                                    }
                                }) { Icon(Icons.Filled.PlayArrow, "Play this movie") }
                                IconButton({
                                    showMediaInfoDialog = !showMediaInfoDialog
                                }) { Icon(Icons.Filled.Info, "Media information") }

                                IconButton(onClick = {
                                    movieEntry?.let {
                                        launchThreaded {
                                            RequestQueue.updateWatched(
                                                MediaWatched(movieEntry.GUID, login?.userID ?: "", currentUnixSeconds(), 0, 0F, 100F)
                                            ).first.join()
                                            sm.updateData(true)
                                        }
                                    }
                                }) { Icon(Icons.Default.Visibility, "Set Watched") }

                                IconButton(onClick = {
                                    movieEntry?.let {
                                        launchThreaded {
                                            RequestQueue.removeWatched(movieEntry).first.join()
                                            sm.updateData(true)
                                        }
                                    }
                                }) { Icon(Icons.Default.VisibilityOff, "Set Unwatched") }

                                Spacer(Modifier.weight(1f))
                                Text(movieEntry?.fileSize?.readableSize() ?: "", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (movieEntry != null)
                                DownloadRow(movieEntry)
                            if (watched != null) {
                                WatchedIndicator(watched, Modifier.fillMaxWidth().padding(0.dp, 2.dp, 0.dp, 5.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))

                    Text("About", Modifier.padding(6.dp, 2.dp), style = MaterialTheme.typography.titleLarge)
                    MediaGenreListing(mediaStorage.media)
                    val preferGerman = settings["prefer-german-metadata", false]
                    val synopsis =
                        if (!mediaStorage.media.synopsisDE.isNullOrBlank() && preferGerman) mediaStorage.media.synopsisDE else mediaStorage.media.synopsisEN
                    if (!synopsis.isNullOrBlank())
                        SelectionContainer {
                            Text(synopsis.removeSomeHTMLTags(), Modifier.padding(6.dp), style = MaterialTheme.typography.bodyMedium)
                        }

                    if (mediaStorage.sequel != null || mediaStorage.prequel != null) {
                        HorizontalDivider(Modifier.fillMaxWidth().padding(8.dp, 6.dp), thickness = 2.dp)
                        MediaRelations(mediaStorage) {
                            nav.pushMediaView(
                                it,
                                true
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun DownloadRow(entry: MediaEntry) {
        val downloaded by Storage.stores.downloadedStore.collectWithEmptyInitial()
        val currentlyDownloading by DownloadQueue.currentDownload.collectAsState()
        val queued by DownloadQueue.queuedEntries.collectAsState()
        Row(Modifier.padding(8.dp, 5.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val isDownloaded = downloaded.find { it.entryID eqI entry.GUID } != null
            val isQueued = queued.contains(entry.GUID)
            val progress = currentlyDownloading?.let { if (it.entryID eqI entry.GUID) it else null }
            if (isQueued || progress != null) {
                if (isQueued) {
                    Icon(Icons.Default.Downloading, "Queued", modifier = Modifier.size(20.dp))
                } else if (progress != null) {
                    Box {
                        Icon(Icons.Default.Download, "Downloading", modifier = Modifier.size(14.dp).zIndex(1F).align(Alignment.Center))
                        CircularProgressIndicator(
                            { progress.progressPercent.toFloat() / 100 },
                            modifier = Modifier.size(23.dp).zIndex(2F).align(Alignment.Center),
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(0.4F),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                }
            }
            if (progress == null) {
                if (isDownloaded || isQueued) {
                    IconButtonWithTooltip(Icons.Default.Delete, "Delete") {
                        if (queued.contains(entry.GUID)) {
                            launchThreaded { DownloadQueue.queuedEntries.emit(queued.toMutableList().filterNot { it eqI entry.GUID }.toList()) }
                        }
                        val downloadedEntry = downloaded.find { it.entryID eqI entry.GUID }
                        if (downloadedEntry != null) {
                            SYSTEMFILES.delete(downloadedEntry.okioPath)
                        }
                        launchThreaded {
                            Storage.stores.downloadedStore.updateList { list ->
                                list.removeAll { it.entryID eqI entry.GUID }
                            }
                        }
                    }
                } else {
                    IconButtonWithTooltip(Icons.Default.DownloadForOffline, "Download") {
                        DownloadQueue.addToQueue(entry)
                    }
                }
            }
        }
    }
}