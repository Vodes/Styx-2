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
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import com.russhwolf.settings.get
import moe.styx.Main
import moe.styx.components.MainScaffold
import moe.styx.components.anime.*
import moe.styx.components.misc.FavouriteIconButton
import moe.styx.components.user.OnlineUsersIcon
import moe.styx.logic.data.DataManager
import moe.styx.logic.login.login
import moe.styx.logic.loops.RequestQueue
import moe.styx.logic.runner.currentPlayer
import moe.styx.logic.runner.launchMPV
import moe.styx.logic.utils.currentUnixSeconds
import moe.styx.logic.utils.readableSize
import moe.styx.logic.utils.removeSomeHTMLTags
import moe.styx.navigation.LocalGlobalNavigator
import moe.styx.types.MediaWatched
import moe.styx.types.eqI
import moe.styx.views.settings.SettingsView
import java.util.*

class MovieDetailView(val ID: String) : Screen {

    private var sKey: String? = null

    private fun generateKey(): String {
        if (sKey == null)
            sKey = UUID.randomUUID().toString()
        return sKey as String
    }

    override val key: ScreenKey
        get() = generateKey()


    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current
        val vm = rememberScreenModel { AnimeDetailViewModel(ID) }

        if (vm.anime == null) {
            nav.pop();
            return
        }
        val entry = vm.getEpisodes().firstOrNull()
        var needsRepaint by remember { mutableStateOf(0) }
        val watched = entry?.let { DataManager.watched.value.find { it.entryID eqI entry.GUID } }

        val preferGerman = Main.settings["prefer-german-metadata", false]
        val scrollState = rememberScrollState()
        val showSelection = remember { mutableStateOf(false) }

        MainScaffold(title = vm.anime.name, actions = {
            OnlineUsersIcon()
            FavouriteIconButton(vm.anime)
        }) {
            ElevatedCard(
                Modifier.padding(8.dp).fillMaxSize(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                var showFailedDialog by remember { mutableStateOf(false) }
                var failedToPlayMessage by remember { mutableStateOf("") }
                if (showFailedDialog) {
                    FailedDialog(failedToPlayMessage, Modifier.fillMaxWidth(0.6F), Modifier.align(Alignment.CenterHorizontally)) {
                        showFailedDialog = false
                        if (it) nav.push(SettingsView())
                    }
                }
                var showMediaInfoDialog by remember { mutableStateOf(false) }
                var showAppendDialog by remember { mutableStateOf(false) }
                if (showAppendDialog && entry != null) {
                    AppendDialog(entry, Modifier.fillMaxWidth(0.6F), Modifier.align(Alignment.CenterHorizontally), {
                        showAppendDialog = false
                    }, execUpdate = {
                        needsRepaint++
                    }) {
                        failedToPlayMessage = it
                        showFailedDialog = true
                    }
                }

                if (showMediaInfoDialog && entry != null) {
                    MediaInfoDialog(entry) { showMediaInfoDialog = false }
                }

                Column(Modifier.fillMaxSize().verticalScroll(scrollState)) {
                    StupidImageNameArea(vm.anime) {
                        Column(Modifier.padding(6.dp).widthIn(0.dp, 560.dp).fillMaxWidth()) {
                            Row(Modifier.padding(3.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                IconButton({
                                    if (entry == null)
                                        return@IconButton
                                    if (currentPlayer == null) {
                                        launchMPV(entry, false, {
                                            failedToPlayMessage = it
                                            showFailedDialog = true
                                        }) { needsRepaint++ }
                                    } else {
                                        showAppendDialog = true
                                    }
                                }) { Icon(Icons.Filled.PlayArrow, "Play this movie") }
                                IconButton({
                                    showMediaInfoDialog = !showMediaInfoDialog
                                }) { Icon(Icons.Filled.Info, "Media information") }

                                IconButton(onClick = {
                                    entry?.let {
                                        RequestQueue.updateWatched(
                                            MediaWatched(entry.GUID, login?.userID ?: "", currentUnixSeconds(), 0, 0F, 100F)
                                        )
                                    }
                                }) { Icon(Icons.Default.Visibility, "Set Watched") }

                                IconButton(onClick = {
                                    entry?.let { RequestQueue.removeWatched(entry) }
                                }) { Icon(Icons.Default.VisibilityOff, "Set Unwatched") }

                                Spacer(Modifier.weight(1f))
                                Text(entry?.fileSize?.readableSize() ?: "", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (watched != null) {
                                WatchedIndicator(watched, Modifier.fillMaxWidth().padding(0.dp, 2.dp, 0.dp, 5.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))

                    Text("About", Modifier.padding(6.dp, 2.dp), style = MaterialTheme.typography.titleLarge)
                    MediaGenreListing(vm.anime)
                    val synopsis = if (!vm.anime.synopsisDE.isNullOrBlank() && preferGerman) vm.anime.synopsisDE else vm.anime.synopsisEN
                    if (!synopsis.isNullOrBlank())
                        SelectionContainer {
                            Text(synopsis.removeSomeHTMLTags(), Modifier.padding(6.dp), style = MaterialTheme.typography.bodyMedium)
                        }

                    if (vm.anime.sequel != null || vm.anime.prequel != null) {
                        Divider(Modifier.fillMaxWidth().padding(8.dp, 6.dp), thickness = 2.dp)
                        MediaRelations(vm.anime)
                    }
                }
            }
        }
    }
}