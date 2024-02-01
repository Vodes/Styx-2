package moe.styx.logic.data

import androidx.compose.runtime.getValue
import com.russhwolf.settings.get
import moe.styx.Main.settings
import moe.styx.types.Category
import moe.styx.types.eqI

fun getSelectedCategories(shows: Boolean = true): List<Category> {
    val saved = settings[if (shows) "selected-categories-shows" else "selected-categories-movies", ""]
    if (saved.isBlank())
        return emptyList()
    val categories by DataManager.categories
    return saved.split(";")
        .mapNotNull { s -> categories.find { it.GUID eqI s } }
        .sortedByDescending { it.sort }
        .toList()
}

fun saveSelectedCategories(categories: List<Category>, shows: Boolean = true) {
    val s = categories.joinToString(separator = ";") { it.GUID }
    settings.putString(if (shows) "selected-categories-shows" else "selected-categories-movies", s)
}

fun getSelectedGenres(shows: Boolean = true): List<String> {
    val saved = settings[if (shows) "selected-genres-shows" else "selected-genres-movies", ""]
    if (saved.isBlank())
        return emptyList()

    return saved.split(";").sorted().toList()
}

fun saveSelectedGenres(genres: List<String>, shows: Boolean = true) {
    val s = genres.joinToString(separator = ";")
    settings.putString(if (shows) "selected-genres-shows" else "selected-genres-movies", s)
}