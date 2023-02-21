package moe.styx.logic.data

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import moe.styx.*
import moe.styx.moe.styx.logic.data.updateImageCache
import java.io.File

class DataManager() {

    val media = mutableStateOf(listOf<Media>())
    val entries = mutableStateOf(listOf<MediaEntry>())
    val categories = mutableStateOf(listOf<Category>())
    val images = mutableStateOf(listOf<Image>())
    val favourites = mutableStateOf(listOf<Favourite>())
    val schedules = mutableStateOf(listOf<MediaSchedule>())

    val isLoaded = mutableStateOf(false)

    suspend fun load(onProgressUpdate: (String) -> Unit) = coroutineScope {
        isLoaded.value = false

        val hasConnection = hasInternet()

        var lastChanges = if (hasConnection) getObject<Changes>(Endpoints.CHANGES) else Changes(0, 0)
        if (lastChanges != null) {
            println(lastChanges.media)
            println(lastChanges.entry)
        } else
            lastChanges = Changes(0, 0)

        val shouldUpdateMedia = lastChanges.media > lastLocalChange().media
        val shouldUpdateEntries = lastChanges.entry > lastLocalChange().entry

        if (shouldUpdateMedia && shouldUpdateEntries) {
            onProgressUpdate("Updating media & entries...")
            val mediaJob = launch {
                media.value = saveList(getList(Endpoints.MEDIA), "media.json")
            }
            val entryJob = launch {
                entries.value = saveList(getList(Endpoints.MEDIA_ENTRIES), "entries.json")
            }
            awaitAll(mediaJob, entryJob)
            updateLocalChange(true, true)
            delay(300)
        } else {
            if (shouldUpdateMedia && hasConnection) {
                onProgressUpdate("Updating media...")
                media.value = saveList(getList(Endpoints.MEDIA), "media.json")
                updateLocalChange(true, false)
                delay(300)
            } else {
                media.value = readList("media.json")
            }

            if (shouldUpdateEntries && hasConnection) {
                onProgressUpdate("Updating entries...")
                entries.value = saveList(getList(Endpoints.MEDIA_ENTRIES), "entries.json")
                updateLocalChange(false, true)
                delay(300)
            } else {
                entries.value = readList("entries.json")
            }
        }

        if (hasConnection)
            favourites.value = saveList(getList(Endpoints.FAVOURITES), "favourites.json")

        if ((shouldUpdateMedia || shouldUpdateEntries) && hasConnection) {
            onProgressUpdate("Updating categories & images...")
            categories.value = saveList(getList(Endpoints.CATEGORIES), "categories.json")
            images.value = saveList(getList(Endpoints.IMAGES), "images.json")
            schedules.value = saveList(getList(Endpoints.SCHEDULES), "schedules.json")
            delay(300)

            onProgressUpdate("Updating image cache...")
            updateImageCache()
        } else {
            categories.value = readList("categories.json")
            images.value = readList("images.json")
            favourites.value = readList("favourites.json")
            schedules.value = readList("schedules.json")
        }
        isLoaded.value = true
    }

    private fun updateLocalChange(media: Boolean, entry: Boolean) {
        val now = Clock.System.now().epochSeconds
        val changesFile = File(getAppDir(), "changes.json")
        val current = lastLocalChange()
        changesFile.writeText(
            json.encodeToString(
                Changes(
                    if (media) now else current.media,
                    if (entry) now else current.entry
                )
            )
        )
    }

    private inline fun <reified T> saveList(list: List<T>, file: String): List<T> {
        val open = File(getDataDir(), file)
        open.writeText(json.encodeToString(list))
        return list
    }

    private inline fun <reified T> readList(file: String): List<T> {
        val open = File(getDataDir(), file)
        val content = open.readText()
        if (content.isEmpty())
            return listOf()
        return json.decodeFromString(content)
    }

    private fun lastLocalChange(): Changes {
        val changesFile = File(getAppDir(), "changes.json")
        if (!changesFile.exists() || changesFile.readText().isEmpty())
            return Changes(-1, -1)

        return json.decodeFromString(changesFile.readText())
    }

    private fun getDataDir(): File {
        val dir = File(getAppDir(), "Data")
        dir.mkdirs()
        return dir
    }

    fun getAppDir(): File {
        return if (System.getProperty("os.name").lowercase().contains("win")) {
            val styxDir = File(System.getenv("APPDATA"), "Styx")
            val dir = File(styxDir, "App")
            dir.mkdirs()
            dir
        } else {
            val configDir = File(System.getProperty("user.dir"), ".config")
            val styxDir = File(configDir, "Styx")
            val dir = File(styxDir, "App")
            dir.mkdirs()
            dir
        }
    }
}