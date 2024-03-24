package moe.styx.logic

import com.russhwolf.settings.get
import dev.cbyrne.kdiscordipc.KDiscordIPC
import dev.cbyrne.kdiscordipc.core.event.impl.DisconnectedEvent
import dev.cbyrne.kdiscordipc.core.event.impl.ErrorEvent
import dev.cbyrne.kdiscordipc.core.event.impl.ReadyEvent
import dev.cbyrne.kdiscordipc.data.activity.*
import io.github.xxfast.kstore.extensions.getOrEmpty
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import moe.styx.Styx__.BuildConfig
import moe.styx.common.compose.extensions.getThumb
import moe.styx.common.compose.extensions.getURL
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.settings
import moe.styx.common.compose.utils.Log
import moe.styx.common.data.MediaActivity
import moe.styx.common.extension.currentUnixSeconds
import moe.styx.common.extension.eqI
import moe.styx.common.extension.toBoolean
import moe.styx.common.util.launchGlobal
import moe.styx.logic.runner.MpvStatus
import moe.styx.logic.runner.currentPlayer

object DiscordRPC {
    private var ipc: KDiscordIPC? = null
    private var errored = false

    fun isStarted() = ipc != null && ipc!!.connected

    fun start() {
        launchGlobal {
            ipc = KDiscordIPC(BuildConfig.DISCORD_CLIENT_ID)
            runCatching {
                ipc!!.on<ErrorEvent> {
                    errored = true
                    Log.e("Discord IPC Initialization") { "Failed to initialize DiscordIPC: ${this.data.message}" }
                }.start()
                ipc!!.on<DisconnectedEvent> {
                    Log.w { "Discord-RPC disconnected." }
                }.start()
                ipc!!.on<ReadyEvent> {
                    errored = false
                    Log.i { "Discord-RPC initialized." }
                    updateActivity()
                }.start()
                ipc!!.connect()
            }.onFailure { Log.e("Discord IPC Initialization") { "Failed to initialize DiscordIPC: ${it.localizedMessage}" } }
        }
    }

    fun clearActivity() {
        if (!isStarted())
            return
        ipc?.scope?.launch {
            runCatching {
                ipc!!.activityManager.clearActivity()
            }
            ipc = null
        }
    }

    fun updateActivity() {
        if (!isStarted())
            return
        val mediaActivity = if (currentPlayer != null && MpvStatus.current.file.isNotEmpty() && MpvStatus.current.percentage > -1)
            MediaActivity(MpvStatus.current.file, MpvStatus.current.seconds.toLong(), !MpvStatus.current.paused)
        else null
        val entry = mediaActivity?.let { act -> runBlocking { Storage.stores.entryStore.getOrEmpty() }.find { it.GUID eqI act.mediaEntry } }
        val media = entry?.let { ent -> runBlocking { Storage.stores.mediaStore.getOrEmpty() }.find { it.GUID eqI ent.mediaID } }
//        val media = entry?.let { ent -> Storage.mediaList.find { it.GUID eqI ent.mediaID } }
        ipc!!.scope.launch {
            if (errored)
                return@launch
            runCatching {
                ipc?.let {
                    if (mediaActivity == null || media == null) {
                        if (settings["discord-rpc-idle", true]) {
                            it.activityManager.setActivity("Not watching anything") {
                                button("View on GitHub", "https://github.com/Vodes?tab=repositories&q=Styx&language=kotlin")
                                largeImage("styx", "v${BuildConfig.APP_VERSION}")
                            }
                        } else {
                            it.activityManager.clearActivity()
                        }
                        return@let
                    }
                    it.activityManager.setActivity(
                        if (mediaActivity.playing) "Watching" else "Paused",
                        media.name + if (media.isSeries.toBoolean()) " - ${entry.entryNumber}" else ""
                    ) {
                        button("View on GitHub", "https://github.com/Vodes?tab=repositories&q=Styx&language=kotlin")
                        val image = media.getThumb()
                        if (mediaActivity.playing)
                            timestamps(currentUnixSeconds(), currentUnixSeconds() + MpvStatus.current.timeRemaining)
                        if (image == null) {
                            largeImage("styx", "v${BuildConfig.APP_VERSION}")
                        } else {
                            smallImage("styx", "v${BuildConfig.APP_VERSION}")
                            largeImage(image.getURL())
                        }
                    }
                }
            }.onFailure { Log.e { "Failed to update discord activity. Exception: ${it.localizedMessage}" } }
        }
    }
}