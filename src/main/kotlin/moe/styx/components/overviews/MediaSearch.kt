package moe.styx.components.overviews

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.get
import moe.styx.logic.data.favAdded
import moe.styx.logic.data.find
import moe.styx.logic.utils.equalsAny
import moe.styx.components.misc.TwoStateIconButton
import moe.styx.logic.data.getSelectedCategories
import moe.styx.logic.data.getSelectedGenres
import moe.styx.settings
import moe.styx.theme.AppShapes
import moe.styx.types.Category
import moe.styx.types.Media

class MediaSearch(listIn: List<Media>, private val favs: Boolean = false) {

    private var list = listIn
    val searchState = mutableStateOf("")
    private val sortTypes = listOf("Added", "Name", "English Name", "Romaji Name")
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

    private fun updateList(
        sort: String,
        search: String,
        descending: Boolean,
        selectedCategories: List<Category>,
        selectedGenres: List<String>
    ): List<Media> {
        var processedList = list

        if (selectedCategories.isNotEmpty() && !favs) {
            processedList = processedList.filter { media ->
                selectedCategories.find { it.GUID.equals(media.categoryID, true) } != null
            }
        }

        if (selectedGenres.isNotEmpty() && !favs) {
            processedList = processedList.filter { media ->
                val genresOfMedia = if (media.genres != null) media.genres!!.split(",") else listOf()
                selectedGenres.find { it.equalsAny(genresOfMedia) } != null
            }
        }

        if (search.isNotBlank())
            processedList = processedList.filter { it.find(search) }

        processedList = if (sort.lowercase() == "added" && favs) {
            if (descending)
                processedList.sortedByDescending { it.favAdded() }
            else
                processedList.sortedBy { it.favAdded() }
        } else {
            when (sort.lowercase()) {
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
        var sort by remember { mutableStateOf(settings["sort", "added"]) }
        var showSort by remember { mutableStateOf(false) }
        var showFilters by remember { mutableStateOf(settings["show-filters", false]) }
        var search by remember { searchState }
        var descending by remember { mutableStateOf(settings["sort-descending", true]) }
        var selectedGenres by remember { mutableStateOf(getSelectedGenres()) }
        var selectedCategories by remember { mutableStateOf(getSelectedCategories()) }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().padding(9.dp, 2.dp),
            shape = AppShapes.medium,
            value = search,
            onValueChange = {
                search = it
                result(updateList(sort, search, descending, selectedCategories, selectedGenres))
            },
            label = { Text("Search") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            trailingIcon = {
                Row {
                    if (!favs) {
                        TwoStateIconButton(
                            "show-filters",
                            false,
                            Icons.Filled.FilterAltOff,
                            Icons.Filled.FilterAlt,
                            { showFilters = it })
                    }
                    IconButton(
                        onClick = { showSort = true },
                        content = { Icon(Icons.Filled.MoreVert, null) })
                }
                DropdownMenu(
                    expanded = showSort,
                    onDismissRequest = { showSort = false },
                    content = {
                        Row {
                            Text(
                                "Sort by",
                                modifier = Modifier.padding(15.dp, 10.dp).align(Alignment.CenterVertically)
                            )
                            IconButton(onClick = {
                                descending = !descending
                                settings.putBoolean("sort-descending", descending)
                                result(updateList(sort, search, descending, selectedCategories, selectedGenres))
                            }, content = {
                                Icon(
                                    if (descending) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                    ""
                                )
                            }, modifier = Modifier.align(Alignment.CenterVertically))
                        }
                        sortTypes.forEachIndexed { _, itemValue ->
                            DropdownMenuItem(
                                text = {
                                    Text(text = itemValue)
                                },
                                onClick = {
                                    sort = itemValue
                                    result(
                                        updateList(
                                            sort,
                                            search,
                                            descending,
                                            selectedCategories,
                                            selectedGenres
                                        )
                                    )
                                    settings.putString("sort", itemValue)
                                    showSort = false
                                },
                                enabled = (!itemValue.equals(sort, true))
                            )
                        }
                    })
            }
        )

        AnimatedVisibility(showFilters && !favs) {
            Surface(Modifier.fillMaxWidth().padding(6.dp)) {
                ElevatedCard(Modifier.fillMaxWidth().padding(3.dp)) {
                    Column {
                        Text("Category", Modifier.padding(7.dp, 4.dp, 7.dp, 3.dp))
                        CategoryFilterBar(list, shows) {
                            selectedCategories = it
                            result(updateList(sort, search, descending, selectedCategories, selectedGenres))
                        }

                        Text("Genre", Modifier.padding(7.dp, 4.dp, 7.dp, 3.dp))
                        GenreFilterBar(list, shows) {
                            selectedGenres = it
                            result(updateList(sort, search, descending, selectedCategories, selectedGenres))
                        }
                    }
                }
            }
        }
    }
}