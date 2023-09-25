package moe.styx.moe.styx.components.overviews

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import moe.styx.addIfNotExisting
import moe.styx.logic.data.getCategory
import moe.styx.moe.styx.logic.data.*
import moe.styx.types.Category
import moe.styx.types.Media

@Composable
fun <T> PrimarySelectableObject(objectIn: T, name: String, isSelected: MutableState<Boolean>, onSelection: (Boolean) -> Unit) {
    val textColor = animateColorAsState(if (isSelected.value) MaterialTheme.colors.onSurface else MaterialTheme.colors.primary)
    val fillColor = animateColorAsState(if (isSelected.value) MaterialTheme.colors.primary else MaterialTheme.colors.surface)
    val shape = RoundedCornerShape(10.dp)

    Surface(
        Modifier.padding(2.dp, 2.dp).sizeIn(0.dp, 36.dp).clip(shape)
            .clickable { isSelected.value = !isSelected.value; onSelection(isSelected.value) },
        shape = shape,
        border = BorderStroke(2.dp, MaterialTheme.colors.primary),
        color = fillColor.value,
    ) {
        Row {
            Text(
                name,
                Modifier.padding(7.dp).align(Alignment.CenterVertically),
                color = textColor.value
            )
        }
    }
}

@Composable
fun CategoryFilterBar(listIn: List<Media>, shows: Boolean = true, onSelected: (List<Category>) -> Unit) {
    val categories = listIn.map { it.getCategory() }.distinctBy { it.GUID }.sortedByDescending { it.sort }
    val selectedCategories = remember { mutableStateOf(getSelectedCategories(shows).toMutableList()) }
    val state = rememberLazyListState()

    Box {
        FlowRow(Modifier.padding(2.dp, 2.dp, 2.dp, 10.dp).heightIn(0.dp, 150.dp).align(Alignment.TopStart)) {
            for (c in categories) {
                PrimarySelectableObject(c, c.name, mutableStateOf(selectedCategories.value.find { it.GUID == c.GUID } != null)) {
                    if (!it) {
                        selectedCategories.value.remove(c)
                    } else {
                        selectedCategories.value.add(c)
                    }
                    selectedCategories.value = selectedCategories.value.filter { cat ->
                        categories.find { c -> c.GUID.equals(cat.GUID, true) } != null
                    }.toMutableList()
                    onSelected(selectedCategories.value.toList())
                    saveSelectedCategories(selectedCategories.value, shows)
                }
            }
        }
    }
}

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
    val selectedGenres = remember { mutableStateOf(getSelectedGenres(shows).toMutableList()) }
    val state = rememberLazyListState()

    Box {
        FlowRow(Modifier.padding(2.dp, 2.dp, 2.dp, 10.dp).heightIn(0.dp, 150.dp).align(Alignment.TopStart)) {
            for (g in genres) {
                PrimarySelectableObject(g, g, mutableStateOf(selectedGenres.value.find { it.equals(g, true) } != null)) {
                    if (!it) {
                        selectedGenres.value.remove(g)
                    } else {
                        selectedGenres.value.add(g)
                    }
                    selectedGenres.value = selectedGenres.value.filter { gen ->
                        genres.find { g -> g.equals(gen, true) } != null
                    }.toMutableList()
                    onSelected(selectedGenres.value.toList())
                    saveSelectedGenres(selectedGenres.value, shows)
                }
            }
        }
    }
}