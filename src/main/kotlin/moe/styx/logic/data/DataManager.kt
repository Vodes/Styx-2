package moe.styx.logic.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import moe.styx.logic.*
import moe.styx.logic.login.ServerStatus
import moe.styx.logic.utils.currentUnixSeconds
import moe.styx.logic.utils.uselessEPTitleRegex
import moe.styx.types.*
import java.io.File

object DataManager {

    val media = mutableStateOf(listOf<Media>())
    val entries = mutableStateOf(listOf<MediaEntry>())
    val watched = mutableStateOf(listOf<MediaWatched>())
    val mediainfo = mutableStateOf(listOf<MediaInfo>())
    val categories = mutableStateOf(listOf<Category>())
    val images = mutableStateOf(listOf<Image>())
    val favourites = mutableStateOf(listOf<Favourite>())
    val schedules = mutableStateOf(listOf<MediaSchedule>())

    val isLoaded = mutableStateOf(false)

    suspend fun load(onProgressUpdate: (String) -> Unit) = coroutineScope {
        isLoaded.value = false

        val serverOnline = ServerStatus.lastKnown == ServerStatus.ONLINE

        var lastChanges = if (serverOnline) getObject<Changes>(Endpoints.CHANGES) else Changes(0, 0)
        if (lastChanges == null)
            lastChanges = Changes(0, 0)

        val shouldUpdateMedia = lastChanges.media > lastLocalChange().media
        val shouldUpdateEntries = lastChanges.entry > lastLocalChange().entry

        if (serverOnline) {
            // These can be updated every time because its not a lot of data
            val jobs = mutableListOf(launch {
                saveListEx(getList(Endpoints.SCHEDULES), "schedules.json", schedules)
                saveListEx(getList(Endpoints.CATEGORIES), "categories.json", categories)
                saveListEx(getList(Endpoints.FAVOURITES), "favourites.json", favourites)
                saveListEx(getList(Endpoints.WATCHED), "watched.json", watched)
            })

            if (shouldUpdateEntries || shouldUpdateMedia) {
                // Update Images if entries or media get updated
                jobs.add(launch {
                    saveListEx(getList(Endpoints.IMAGES), "images.json", images)
                })
                jobs.add(launch {
                    saveListEx(getList(Endpoints.MEDIAINFO), "mediainfo.json", mediainfo)
                })

                if (shouldUpdateMedia)
                    jobs.add(launch {
                        saveListEx(getList(Endpoints.MEDIA), "media.json", media)
                        updateLocalChange(true, false)
                    })
                else
                    readListEx("media.json", media)

                if (shouldUpdateEntries)
                    jobs.add(launch {
                        saveListEx(getList(Endpoints.MEDIA_ENTRIES), "entries.json", entries)
                        updateLocalChange(false, true)
                    })
                else
                    readListEx("entries.json", entries)
            } else {
                readListEx("entries.json", entries)
                readListEx("mediainfo.json", mediainfo)
                readListEx("media.json", media)
                readListEx("images.json", images)
            }
            // Wait for all jobs to finish
            awaitAll(*jobs.toTypedArray())
        } else {
            // Read from local files
            entries.value = readList("entries.json")
            media.value = readList("media.json")
            categories.value = readList("categories.json")
            images.value = readList("images.json")
            favourites.value = readList("favourites.json")
            schedules.value = readList("schedules.json")
            watched.value = readList("watched.json")
            mediainfo.value = readList("mediainfo.json")
        }

        entries.value = entries.value.map {
            var changed = false
            val nameDE = if (!it.nameDE.isNullOrBlank()) {
                if (uselessEPTitleRegex.matchEntire(it.nameDE!!) != null) {
                    changed = true
                    null
                } else
                    it.nameEN
            } else it.nameDE

            val nameEN = if (!it.nameEN.isNullOrBlank()) {
                if (uselessEPTitleRegex.matchEntire(it.nameEN!!) != null) {
                    changed = true
                    null
                } else
                    it.nameEN
            } else it.nameEN

            if (changed) it.copy(nameEN = nameEN, nameDE = nameDE)
            else it
        }

        delay(200)

        onProgressUpdate("Updating image cache...")
        updateImageCache()

        isLoaded.value = true
    }

    private fun updateLocalChange(media: Boolean, entry: Boolean) {
        val now = currentUnixSeconds()
        val current = lastLocalChange()
        updateLocalChange(if (media) now else current.media, if (entry) now else current.media)
    }

    fun updateLocalChange(media: Long, entry: Long) {
        val changesFile = File(getAppDir(), "changes.json")
        changesFile.writeText(json.encodeToString(Changes(media, entry)))
    }

    inline fun <reified T> saveList(list: List<T>, file: String): List<T> {
        val open = File(getDataDir(), file)
        open.writeText(json.encodeToString(list))
        return list
    }

    inline fun <reified T> saveListEx(list: List<T>, file: String, target: MutableState<List<T>>) {
        target.value = saveList(list, file)
    }

    private inline fun <reified T> readList(file: String): List<T> {
        val open = File(getDataDir(), file)
        val content = open.readText()
        if (content.isEmpty())
            return listOf()
        return json.decodeFromString(content)
    }

    private inline fun <reified T> readListEx(file: String, target: MutableState<List<T>>) {
        target.value = readList(file)
    }

    private fun lastLocalChange(): Changes {
        val changesFile = File(getAppDir(), "changes.json")
        if (!changesFile.exists() || changesFile.readText().isEmpty())
            return Changes(-1, -1)

        return json.decodeFromString(changesFile.readText())
    }

    fun getDataDir(): File {
        return File(getAppDir(), "Data").also { it.mkdirs() }
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

    fun getMpvDir(): File {
        return File(getAppDir(), "mpv")
    }

    fun getMpvConfDir(): File {
        return File(getMpvDir(), "portable_config")
    }
}