package moe.styx.logic.data

import androidx.compose.runtime.mutableStateOf
import moe.styx.Endpoints
import moe.styx.getList
import moe.styx.getObject
import moe.styx.logic.login.isLoggedIn
import java.io.File

class DataManager() {

    val media = mutableStateOf(listOf<Media>())
    val entries = mutableStateOf(listOf<MediaEntry>())

    val categories = mutableStateOf(listOf<Category>())
    val images = mutableStateOf(listOf<Image>())

    val loadprogress = mutableStateOf("")

    fun load() {
        if (!isLoggedIn())
            return

        val lastChanges = getObject<Changes>(Endpoints.CHANGES)
        if (lastChanges != null) {
            println(lastChanges.media)
            println(lastChanges.entry)
        }
        media.value = getList(Endpoints.MEDIA)
        entries.value = getList(Endpoints.MEDIA_ENTRIES)
        categories.value = getList(Endpoints.CATEGORIES)
        images.value = getList(Endpoints.IMAGES)
    }

    fun getDir(): File {
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