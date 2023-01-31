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