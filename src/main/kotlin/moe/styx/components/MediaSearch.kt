package moe.styx.moe.styx.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.get
import moe.styx.logic.data.Media
import moe.styx.logic.data.favAdded
import moe.styx.logic.data.find
import moe.styx.settings

class MediaSearch(listIn: List<Media>, favs: Boolean = false) {

    private val list = listIn
    val searchState = mutableStateOf("")
    val sortTypes = listOf("Added", "Name", "English Name", "Romaji Name")
    val favs = favs
    private var lastSearch = ""

    fun getDefault(): List<Media> {
        return updateList(settings["sort", "added"], lastSearch, settings["sort-descending", true])
    }

    fun updateList(sort: String, search: String, descending: Boolean): List<Media> {
        var processedList = list
        if (search.isNotBlank())
            processedList = processedList.filter { it.find(search) }

        if (sort.lowercase() == "added" && favs) {
            processedList = if (descending)
                processedList.sortedByDescending { it.favAdded() }
            else
                processedList.sortedBy { it.favAdded() }
        } else {
            processedList = when (sort.lowercase()) {
                "name" -> if (descending) processedList.sortedByDescending { it.name.lowercase() } else processedList.sortedBy { it.name.lowercase() }
                "added" -> if (descending) processedList.sortedByDescending { it.added } else processedList.sortedBy { it.added }
                "english name" -> if (descending) processedList.sortedByDescending { it.nameEN?.lowercase() } else processedList.sortedBy { it.nameEN?.lowercase() }
                "romaji name" -> if (descending) processedList.sortedByDescending { it.nameJP?.lowercase() } else processedList.sortedBy { it.nameJP?.lowercase() }
                else -> processedList
            }
        }

        lastSearch = search
        return processedList
    }

    @Composable
    fun component(result: (List<Media>) -> Unit) {
        val sort = remember { mutableStateOf(settings["sort", "added"]) }
        val showSort = remember { mutableStateOf(false) }
        val search = remember { searchState }
        val descending = remember { mutableStateOf(settings["sort-descending", true]) }

        TextField(
            value = search.value,
            onValueChange = {
                search.value = it
                result(updateList(sort.value, search.value, descending.value))
            },
            placeholder = { Text("Search") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            trailingIcon = {
                Row {
                    IconButton(
                        onClick = { showSort.value = true },
                        content = { Icon(Icons.Filled.MoreVert, null) })
                }
                DropdownMenu(
                    expanded = showSort.value,
                    onDismissRequest = { showSort.value = false },
                    content = {
                        Row {
                            Text(
                                "Sort by",
                                modifier = Modifier.padding(15.dp, 10.dp).align(Alignment.CenterVertically)
                            )
                            IconButton(onClick = {
                                descending.value = !descending.value
                                settings.putBoolean("sort-descending", descending.value)
                                result(updateList(sort.value, search.value, descending.value))
                            }, content = {
                                Icon(
                                    if (descending.value) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                    ""
                                )
                            }, modifier = Modifier.align(Alignment.CenterVertically))
                        }
                        sortTypes.forEachIndexed { itemIndex, itemValue ->
                            DropdownMenuItem(
                                onClick = {
                                    sort.value = itemValue
                                    result(updateList(sort.value, search.value, descending.value))
                                    settings.putString("sort", itemValue)
                                    showSort.value = false
                                },
                                enabled = (!itemValue.equals(sort.value, true))
                            ) {
                                Text(text = itemValue)
                            }
                        }
                    })
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

}