package moe.styx.logic.loops

import com.russhwolf.settings.get
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import moe.styx.logic.*
import moe.styx.logic.data.DataManager
import moe.styx.logic.login.isLoggedIn
import moe.styx.logic.login.login
import moe.styx.settings
import moe.styx.types.*

object RequestQueue {
    private var queuedFavChanges = QueuedFavChanges()

    fun start() {
        val savedFavs = settings["queued-favs", ""]
        if (savedFavs.isNotBlank())
            queuedFavChanges = json.decodeFromString(savedFavs)

        launchGlobal {
            while (true) {
                if (!hasInternet() || !isLoggedIn()) {
                    delay(30000L)
                    continue
                }
                if (queuedFavChanges.toAdd.isNotEmpty() || queuedFavChanges.toRemove.isNotEmpty())
                    syncFavs()
                delay(15000L)
            }
        }
    }

    fun save() {
        settings.putString("queued-favs", json.encodeToString(queuedFavChanges))
    }

    fun addFav(media: Media) {
        val favs = DataManager.favourites.value.toMutableList()
        val existing = favs.find { it.mediaID eqI media.GUID }
        if (existing != null)
            return
        val fav = Favourite(media.GUID, login?.userID ?: "", Clock.System.now().epochSeconds)
        favs.add(fav)
        DataManager.saveListEx(favs.toList(), "favourites.json", DataManager.favourites)
        queuedFavChanges.toAdd.removeIf { it.mediaID eqI media.GUID }
        queuedFavChanges.toRemove.removeIf { it.mediaID eqI media.GUID }

        if (!hasInternet() || !isLoggedIn() || !sendObject(Endpoints.FAVOURITES_ADD, fav)) {
            queuedFavChanges.toAdd.add(fav)
            save()
        }
        println(queuedFavChanges)
    }

    fun removeFav(media: Media) {
        val favs = DataManager.favourites.value.toMutableList()
        val fav = favs.find { it.mediaID eqI media.GUID } ?: return
        favs.remove(fav)
        DataManager.saveListEx(favs.toList(), "favourites.json", DataManager.favourites)
        queuedFavChanges.toAdd.removeIf { it.mediaID eqI media.GUID }
        queuedFavChanges.toRemove.removeIf { it.mediaID eqI media.GUID }

        if (!hasInternet() || !isLoggedIn() || !sendObject(Endpoints.FAVOURITES_DELETE, fav)) {
            queuedFavChanges.toRemove.add(fav)
            save()
        }
        println(queuedFavChanges)
    }

    private fun syncFavs() {
        if (hasInternet() && isLoggedIn() && sendObject(Endpoints.FAVOURITES_SYNC, queuedFavChanges)) {
            queuedFavChanges.toAdd.clear()
            queuedFavChanges.toRemove.clear()
            save()
            println("Synced queued favourites!")
        }
    }
}

