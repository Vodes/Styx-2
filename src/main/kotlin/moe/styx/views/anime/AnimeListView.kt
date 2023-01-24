package moe.styx.views.anime

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.russhwolf.settings.get
import moe.styx.components.AnimeCard
import moe.styx.dataManager
import moe.styx.logic.data.find
import moe.styx.settings
import moe.styx.views.settings.SettingsView

@OptIn(ExperimentalMaterialApi::class)
class AnimeListView : Screen {

    var animeList = mutableStateOf(dataManager.media.value)
    val sortTypes = listOf("Added", "Name", "English Name", "Romaji Name")
    val searchState = mutableStateOf("")

    init {
        updateList(settings["sort", "added"], "", settings["sort-descending", true])
    }

    fun updateList(sort: String, search: String, descending: Boolean) {
        var list = dataManager.media.value
        if (search.isNotBlank())
            list = list.filter { it.find(search) }

        list = when (sort.lowercase()) {
            "name" -> if (descending) list.sortedByDescending { it.name.lowercase() } else list.sortedBy { it.name.lowercase() }
            "added" -> if (descending) list.sortedByDescending { it.added } else list.sortedBy { it.added }
            "english name" -> if (descending) list.sortedByDescending { it.nameEN?.lowercase() } else list.sortedBy { it.nameEN?.lowercase() }
            "romaji name" -> if (descending) list.sortedByDescending { it.nameJP?.lowercase() } else list.sortedBy { it.nameJP?.lowercase() }
            else -> list
        }
        animeList.value = list
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val scaffoldState = rememberScaffoldState()
        val nav = LocalNavigator.currentOrThrow

        val sort = remember { mutableStateOf(settings["sort", "added"]) }
        val showSort = remember { mutableStateOf(false) }
        val search = remember { searchState }
        val descending = remember { mutableStateOf(settings["sort-descending", true]) }

        Scaffold(scaffoldState = scaffoldState, topBar = {
            TopAppBar(
                title = { Text("Styx Test") },
                backgroundColor = MaterialTheme.colors.primaryVariant,
                actions = {
                    IconButton(onClick = { nav.push(SettingsView()) }, content = { Icon(Icons.Filled.Settings, null) })
                }
            )
        }) {
            Column {
                TextField(
                    value = search.value,
                    onValueChange = {
                        search.value = it
                        updateList(sort.value, search.value, descending.value)
                    },
                    placeholder = { Text("Search") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    trailingIcon = {
                        IconButton(
                            onClick = { showSort.value = true },
                            content = { Icon(Icons.Filled.List, null) })
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
                                        updateList(sort.value, search.value, descending.value)
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
                                            if (sort.value != itemValue) {
                                                updateList(itemValue, search.value, descending.value)
                                            }
                                            sort.value = itemValue
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
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(10.dp, 7.dp),
                    content = {
                        items(animeList.value.size) { i ->
                            Row(modifier = Modifier.animateItemPlacement()) { AnimeCard(nav, animeList.value[i]) }
                        }
                    }
                )
            }
        }
    }
}