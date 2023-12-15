package moe.styx.moe.styx.logic.requests

import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import moe.styx.hasInternet
import moe.styx.settings
import moe.styx.types.json

// This is kinda scuffed tbh but I need Favs and Watched progress to be saved locally first and then synced
class RequestQueue {
    var status = QueuedSyncs(false, false)
    private val key = "request-queue-status"

    fun start() {
        val queueJob = Job()
        val scope = CoroutineScope(queueJob)
        scope.launch {
            while (true) {
                if (hasInternet()) {
                    if (status.needsFavSync) {
                        if (syncFavs()) {
                            status.needsFavSync = false
                            save()
                        }
                    }
                }
                delay(15000L)
            }
        }
    }

    fun save() {
        settings.putString(key, json.encodeToString(status))
    }

    fun syncFavs(): Boolean {
        return true
    }
}

data class QueuedSyncs(var needsFavSync: Boolean, var needsWatchedSync: Boolean)