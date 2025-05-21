package moe.styx.logic.runner

import io.github.xxfast.kstore.extensions.getOrEmpty
import kotlinx.coroutines.runBlocking
import moe.styx.Main
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.http.login
import moe.styx.common.compose.threads.Heartbeats
import moe.styx.common.compose.threads.RequestQueue
import moe.styx.common.data.MediaActivity
import moe.styx.common.data.MediaWatched
import moe.styx.common.extension.currentUnixSeconds
import moe.styx.common.extension.eqI
import moe.styx.common.util.launchThreaded
import kotlin.math.roundToInt

data class MpvStatus(
    val pos: Float,
    val percentage: Int,
    val file: String,
    val paused: Boolean,
    val playlistCurrent: Int,
    val playlistSize: Int,
    val eof: Boolean,
    val timeRemaining: Float
) {
    companion object {
        var current = MpvStatus(-1F, 0, "", true, 0, 0, false, 0F)
        private var was100 = false
        var lastPrint = currentUnixSeconds()

        fun updateCurrent(dataMap: Map<String, String?>) {
            var path = dataMap["path"]
            path = if (path.isNullOrBlank()) "" else path.substringAfterLast("/").substringBefore("?").substringBefore(".")

            val percent = dataMap["percent-pos"]?.toDoubleOrNull()?.roundToInt() ?: -1
            val eof = dataMap["eof-reached"]?.equals("yes") ?: false
            val playlistCurrent = dataMap["playlist-current-pos"]?.toIntOrNull() ?: 0
            val playlistSize = dataMap["playlist-count"]?.toIntOrNull() ?: 1

            var shouldAutoplay = percent == 100 && eof && playlistCurrent + 1 == playlistSize

            if (!was100 && shouldAutoplay) {
                was100 = true
                shouldAutoplay = true
            } else {
                shouldAutoplay = false
            }

            was100 = current.percentage == 100 && eof

            val new = MpvStatus(
                dataMap["playback-time"]?.toFloat() ?: -1F,
                percent,
                path,
                dataMap["pause"]?.equals("yes") ?: true,
                playlistCurrent,
                playlistSize,
                eof,
                dataMap["time-remaining"]?.toFloat() ?: 0F
            )
            if (new.isAvailable()) {
                if (current.isAvailable() && !current.file.trim().equals(new.file.trim(), true)) {
                    val previousEntry = runBlocking { Storage.stores.entryStore.getOrEmpty() }.find { current.file eqI it.GUID }
                    if (previousEntry != null && current.seconds > 5) {
                        val watched = MediaWatched(
                            previousEntry.GUID,
                            login?.userID ?: "",
                            currentUnixSeconds(),
                            current.seconds.toLong(),
                            current.percentage.toFloat(),
                            current.percentage.toFloat()
                        )
                        launchThreaded {
                            RequestQueue.updateWatched(watched).first.join()
                            currentPlayer?.onClose(MpvFinishStatus(0, "", previousEntry.GUID))
                        }
                    }
                }
                current = new
            }
            Heartbeats.mediaActivity = if (currentPlayer != null && current.file.isNotEmpty() && current.percentage > -1)
                MediaActivity(current.file, current.seconds.toLong(), !current.paused)
            else null

            if (Main.wasLaunchedInDebug && lastPrint < (currentUnixSeconds() - 4))
                println(current).also { lastPrint = currentUnixSeconds() }
            if (shouldAutoplay && currentPlayer != null)
                currentPlayer!!.attemptPlayNext()
        }
    }

    val seconds: Int
        get() = pos.roundToInt()

    fun isAvailable(): Boolean {
        return file.isNotBlank() && (pos != -1F || file.contains("unavailable", true))
    }
}