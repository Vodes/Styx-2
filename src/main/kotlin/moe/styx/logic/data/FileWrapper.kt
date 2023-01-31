package moe.styx.moe.styx.logic.data

import com.russhwolf.settings.get
import moe.styx.logic.data.Category
import moe.styx.settings

fun getSelectedCategories(): List<Category> {
    var list = mutableListOf<Category>()
    val saved = settings["", ""]
    return list.toList()
}