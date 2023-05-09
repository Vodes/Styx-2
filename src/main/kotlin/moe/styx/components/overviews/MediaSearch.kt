package moe.styx.moe.styx.components.overviews

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.get
import moe.styx.equalsAny
import moe.styx.logic.data.*
import moe.styx.moe.styx.components.misc.TwoStateIconButton
import moe.styx.moe.styx.logic.data.getSelectedCategories
import moe.styx.moe.styx.logic.data.getSelectedGenres
import moe.styx.settings

class MediaSearch(listIn: List<Media>, favs: Boolean = false) {

    private var list = listIn
    val searchState = mutableStateOf("")
    val sortTypes = listOf("Added", "Name", "English Name", "Romaji Name")
    val favs = favs
    private var lastSearch = ""

    fun getDefault(shows: Boolean = true, updateList: List<Media>? = null): List<Media> {
        if (updateList != null)
            list = updateList
        return updateList(
            settings["sort", "added"],
            lastSearch,
            settings["sort-descending", true],
            getSelectedCategories(shows),
            getSelectedGenres(shows)
        )
    }

    fun updateList(sort: String, search: String, descending: Boolean, selectedCategories: List<Category>, selectedGenres: List<String>): List<Media> {
        var processedList = list

        if (selectedCategories.isNotEmpty() && !favs) {
            processedList = processedList.filter { media ->
                selectedCategories.find { it.GUID.equals(media.categoryID, true) } != null
            }
        }

        if (selectedGenres.isNotEmpty() && !favs) {
            processedList = processedList.filter { media ->
                var genresOfMedia = if (media.genres != null) media.genres.split(",") else listOf()
                selectedGenres.find { it.equalsAny(genresOfMedia) } != null
            }
        }

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
    fun component(result: (List<Media>) -> Unit, shows: Boolean = true) {
        val sort = remember { mutableStateOf(settings["sort", "added"]) }
        val showSort = remember { mutableStateOf(false) }
        val showFilters = remember { mutableStateOf(settings["show-filters", false]) }
        val search = remember { searchState }
        val descending = remember { mutableStateOf(settings["sort-descending", true]) }
        val selectedGenres = remember { mutableStateOf(getSelectedGenres()) }
        val selectedCategories = remember { mutableStateOf(getSelectedCategories()) }

        TextField(
            value = search.value,
            onValueChange = {
                search.value = it
                result(updateList(sort.value, search.value, descending.value, selectedCategories.value, selectedGenres.value))
            },
            placeholder = { Text("Search") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            trailingIcon = {
                Row {
                    if (!favs) {
                        TwoStateIconButton(
                            "show-filters",
                            false,
                            Icons.Filled.FilterAltOff,
                            Icons.Filled.FilterAlt,
                            { showFilters.value = it })
                    }
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
                                result(updateList(sort.value, search.value, descending.value, selectedCategories.value, selectedGenres.value))
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
                                    result(
                                        updateList(
                                            sort.value,
                                            search.value,
                                            descending.value,
                                            selectedCategories.value,
                                            selectedGenres.value
                                        )
                                    )
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

        AnimatedVisibility(showFilters.value && !favs) {
            Surface(Modifier.fillMaxWidth().padding(6.dp)) {
                Card(Modifier.fillMaxWidth().padding(3.dp), elevation = 8.dp) {
                    Column {
                        Text("Category", Modifier.padding(7.dp, 4.dp, 7.dp, 3.dp))
                        CategoryFilterBar(list, shows) {
                            selectedCategories.value = it
                            result(updateList(sort.value, search.value, descending.value, selectedCategories.value, selectedGenres.value))
                        }

                        Text("Genre", Modifier.padding(7.dp, 4.dp, 7.dp, 3.dp))
                        GenreFilterBar(list, shows) {
                            selectedGenres.value = it
                            result(updateList(sort.value, search.value, descending.value, selectedCategories.value, selectedGenres.value))
                        }
                    }
                }
            }
        }
    }
}