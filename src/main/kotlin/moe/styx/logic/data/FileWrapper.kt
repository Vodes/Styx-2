package moe.styx.moe.styx.logic.data

import com.russhwolf.settings.get
import moe.styx.dataManager
import moe.styx.logic.data.Category
import moe.styx.settings

fun getSelectedCategories(shows: Boolean = true): List<Category> {
    val list = mutableListOf<Category>()
    val saved = settings[if (shows) "selected-categories-shows" else "selected-categories-movies", ""]
    if (saved.isNotBlank() && saved.contains(";")) {
        for (GUID in saved.split(";")) {
            val category = dataManager.categories.value.find { it.GUID.equals(GUID, true) }
            if (category != null)
                list.add(category)
        }
    }
    return list.sortedByDescending { it.sort }.toList()
}

fun saveSelectedCategories(categories: List<Category>, shows: Boolean = true) {
    var s = ""
    for (cat in categories) {
        s += "${cat.GUID};"
    }
    settings.putString(if (shows) "selected-categories-shows" else "selected-categories-movies", s)
}

fun getSelectedGenres(shows: Boolean = true): List<String> {
    val list = mutableListOf<String>()
    val saved = settings[if (shows) "selected-genres-shows" else "selected-genres-movies", ""]
    if (saved.isNotBlank() && saved.contains(";")) {
        for (s in saved.split(";")) {
            list.add(s);
        }
    }
    return list.sorted().toList()
}

fun saveSelectedGenres(genres: List<String>, shows: Boolean = true) {
    var s = ""
    for (g in genres) {
        s += "${g};"
    }
    settings.putString(if (shows) "selected-genres-shows" else "selected-genres-movies", s)
}