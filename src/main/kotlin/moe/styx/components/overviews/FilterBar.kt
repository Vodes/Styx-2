package moe.styx.components.overviews

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import moe.styx.common.data.Category
import moe.styx.common.data.Media
import moe.styx.common.extension.addIfNotExisting
import moe.styx.logic.data.*
import moe.styx.logic.utils.getCategory
import moe.styx.theme.AppShapes

@Composable
fun PrimarySelectableObject(name: String, isSelected: MutableState<Boolean>, onSelection: (Boolean) -> Unit) =
    SelectableObject(
        name,
        isSelected,
        MaterialTheme.colorScheme.onPrimary,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.surface,
        onSelection
    )

@Composable
fun SecondarySelectableObject(name: String, isSelected: MutableState<Boolean>, onSelection: (Boolean) -> Unit) =
    SelectableObject(
        name,
        isSelected,
        MaterialTheme.colorScheme.onSecondary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.surface,
        onSelection
    )

@Composable
private fun SelectableObject(
    name: String,
    isSelected: MutableState<Boolean>,
    selectedTextColor: Color,
    selectedFillColor: Color,
    defaultTextColor: Color,
    defaultFillColor: Color,
    onSelection: (Boolean) -> Unit
) {
    val textColor by animateColorAsState(if (isSelected.value) selectedTextColor else defaultTextColor)
    val fillColor by animateColorAsState(if (isSelected.value) selectedFillColor else defaultFillColor)

    Surface(
        Modifier.padding(2.dp, 2.dp).sizeIn(0.dp, 36.dp).clip(AppShapes.medium)
            .clickable { isSelected.value = !isSelected.value; onSelection(isSelected.value) },
        shape = AppShapes.medium,
        border = BorderStroke(2.dp, selectedFillColor),
        color = fillColor,
    ) {
        Row {
            Text(
                name,
                Modifier.padding(7.dp).align(Alignment.CenterVertically),
                color = textColor
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryFilterBar(listIn: List<Media>, shows: Boolean = true, onSelected: (List<Category>) -> Unit) {
    val categories = listIn.map { it.getCategory() }.distinctBy { it.GUID }.sortedByDescending { it.sort }
    var selectedCategories by remember { mutableStateOf(getSelectedCategories(shows).toMutableList()) }

    Box {
        FlowRow(Modifier.padding(7.dp, 2.dp, 7.dp, 10.dp).heightIn(0.dp, 150.dp).align(Alignment.TopStart)) {
            for (c in categories) {
                PrimarySelectableObject(c.name, mutableStateOf(selectedCategories.find { it.GUID == c.GUID } != null)) {
                    if (!it) {
                        selectedCategories.remove(c)
                    } else {
                        selectedCategories.add(c)
                    }
                    selectedCategories = selectedCategories.filter { cat ->
                        categories.find { c -> c.GUID.equals(cat.GUID, true) } != null
                    }.toMutableList()
                    onSelected(selectedCategories.toList())
                    saveSelectedCategories(selectedCategories, shows)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GenreFilterBar(listIn: List<Media>, shows: Boolean = true, onSelected: (List<String>) -> Unit) {
    val genres = mutableListOf<String>()
    listIn.forEach { m ->
        if (m.genres == null)
            return@forEach

        for (s in m.genres!!.split(",")) {
            genres.addIfNotExisting(s.trim())
        }
    }
    genres.sort()
    var selectedGenres by remember { mutableStateOf(getSelectedGenres(shows).toMutableList()) }
    val state = rememberLazyListState()

    Box {
        FlowRow(Modifier.padding(7.dp, 2.dp, 7.dp, 10.dp).heightIn(0.dp, 150.dp).align(Alignment.TopStart)) {
            for (g in genres) {
                SecondarySelectableObject(g, mutableStateOf(selectedGenres.find { it.equals(g, true) } != null)) {
                    if (!it) {
                        selectedGenres.remove(g)
                    } else {
                        selectedGenres.add(g)
                    }
                    selectedGenres = selectedGenres.filter { gen ->
                        genres.find { g -> g.equals(gen, true) } != null
                    }.toMutableList()
                    onSelected(selectedGenres.toList())
                    saveSelectedGenres(selectedGenres, shows)
                }
            }
        }
    }
}