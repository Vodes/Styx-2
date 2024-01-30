package moe.styx.logic.loops

import com.russhwolf.settings.get
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import moe.styx.logic.*
import moe.styx.logic.data.DataManager
import moe.styx.logic.login.isLoggedIn
import moe.styx.logic.login.login
import moe.styx.logic.utils.replaceIfNotNull
import moe.styx.settings
import moe.styx.types.*

object RequestQueue {
    private var queuedFavChanges = QueuedFavChanges()
    private var queuedWatchedChanges = QueuedWatchedChanges()

    fun start() {
        val savedFavs = settings["queued-favs", ""]
        if (savedFavs.isNotBlank())
            queuedFavChanges = json.decodeFromString(savedFavs)

        val savedWatched = settings["queued-watched", ""]
        if (savedWatched.isNotBlank())
            queuedWatchedChanges = json.decodeFromString(savedWatched)

        launchGlobal {
            while (true) {
                if (!hasInternet() || !isLoggedIn()) {
                    delay(30000L)
                    continue
                }
                if (queuedFavChanges.toAdd.isNotEmpty() || queuedFavChanges.toRemove.isNotEmpty())
                    syncFavs()
                if (queuedWatchedChanges.toUpdate.isNotEmpty() || queuedWatchedChanges.toRemove.isNotEmpty())
                    syncWatched()
                delay(15000L)
            }
        }
    }

    fun save() {
        settings.putString("queued-favs", json.encodeToString(queuedFavChanges))
        settings.putString("queued-watched", json.encodeToString(queuedWatchedChanges))
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

    fun updateWatched(mediaWatched: MediaWatched) {
        val existingList = DataManager.watched.value.toMutableList()
        val existing = existingList.find { it.entryID eqI mediaWatched.entryID }
        val existingMax = existing?.maxProgress ?: -1F
        val new = if (existingMax > mediaWatched.maxProgress) mediaWatched.copy(maxProgress = existingMax) else mediaWatched
        DataManager.saveListEx(existingList.replaceIfNotNull(existing, new).toList(), "watched.json", DataManager.watched)
        queuedWatchedChanges.toUpdate.removeIf { it.entryID eqI mediaWatched.entryID }
        queuedWatchedChanges.toRemove.removeIf { it.entryID eqI mediaWatched.entryID }
        if (!hasInternet() || !isLoggedIn() || !sendObject(Endpoints.WATCHED_ADD, new)) {
            queuedWatchedChanges.toUpdate.add(new)
            save()
        }
    }

    fun addMultipleWatched(entries: List<MediaEntry>) {
        val existingList = DataManager.watched.value.toMutableList()
        val now = Clock.System.now().epochSeconds
        entries.forEach { entry ->
            val existing = existingList.find { it.entryID eqI entry.GUID }
            val new = MediaWatched(entry.GUID, login?.userID ?: "", now, 0, 0F, 100F)
            existingList.replaceIfNotNull(existing, new)
            queuedWatchedChanges.toUpdate.removeIf { it.entryID eqI entry.GUID }
            queuedWatchedChanges.toRemove.removeIf { it.entryID eqI entry.GUID }
            queuedWatchedChanges.toUpdate.add(new)
        }
        DataManager.saveListEx(existingList.toList(), "watched.json", DataManager.watched)
    }

    fun removeMultipleWatched(entries: List<MediaEntry>) {
        val existingList = DataManager.watched.value.toMutableList()
        entries.forEach { entry ->
            val existing = existingList.find { it.entryID eqI entry.GUID }
            if (existing == null)
                return@forEach
            existingList.remove(existing)
            queuedWatchedChanges.toUpdate.removeIf { it.entryID eqI entry.GUID }
            queuedWatchedChanges.toRemove.removeIf { it.entryID eqI entry.GUID }
            queuedWatchedChanges.toRemove.add(existing)
        }
        DataManager.saveListEx(existingList.toList(), "watched.json", DataManager.watched)
    }

    fun removeWatched(entry: MediaEntry) {
        val existingList = DataManager.watched.value.toMutableList()
        val existing = existingList.find { it.entryID eqI entry.GUID }
        if (existing == null)
            return
        existingList.remove(existing)
        DataManager.saveListEx(existingList.toList(), "watched.json", DataManager.watched)
        queuedWatchedChanges.toUpdate.removeIf { it.entryID eqI entry.GUID }
        queuedWatchedChanges.toRemove.removeIf { it.entryID eqI entry.GUID }
        if (!hasInternet() || !isLoggedIn() || !sendObject(Endpoints.WATCHED_DELETE, existing)) {
            queuedWatchedChanges.toRemove.add(existing)
            save()
        }
    }

    private fun syncWatched() {
        if (hasInternet() && isLoggedIn() && sendObject(Endpoints.WATCHED_SYNC, queuedWatchedChanges)) {
            queuedWatchedChanges.toUpdate.clear()
            queuedWatchedChanges.toRemove.clear()
            save()
            println("Synced queued playback tracking!")
        }
    }
}

