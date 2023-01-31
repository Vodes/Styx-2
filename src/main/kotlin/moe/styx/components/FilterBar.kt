package moe.styx.moe.styx.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import moe.styx.logic.data.Category
import moe.styx.logic.data.Media
import moe.styx.logic.data.getCategory
import moe.styx.moe.styx.logic.data.getSelectedCategories
import moe.styx.moe.styx.logic.data.saveSelectedCategories

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryFilterBar(listIn: List<Media>, shows: Boolean = true, onSelected: (List<Category>) -> Unit) {
    val categories = listIn.map { it.getCategory() }.distinctBy { it.GUID }.sortedByDescending { it.sort }
    val selectedCategories = remember { mutableStateOf(getSelectedCategories(shows).toMutableList()) }
    val state = rememberLazyListState()
    Box {
        LazyRow(
            Modifier.padding(2.dp, 2.dp, 2.dp, 10.dp).align(Alignment.TopStart),
            state
        ) {
            itemsIndexed(
                items = categories, key = { _, item -> item.GUID },
            ) { _, item ->
                Row(modifier = Modifier.animateItemPlacement()) {
                    val isSelected = mutableStateOf(selectedCategories.value.find { it.GUID == item.GUID } != null)
                    val textColor =
                        animateColorAsState(if (isSelected.value) MaterialTheme.colors.onSurface else MaterialTheme.colors.primary)
                    val fillColor =
                        animateColorAsState(if (isSelected.value) MaterialTheme.colors.primary else MaterialTheme.colors.surface)
                    val shape = RoundedCornerShape(10.dp)
                    Surface(
                        Modifier.padding(4.dp, 2.dp).sizeIn(0.dp, 36.dp).clickable {
                            if (isSelected.value) {
                                selectedCategories.value.remove(item)
                                isSelected.value = false
                            } else {
                                selectedCategories.value.add(item)
                                isSelected.value = true
                            }
                            selectedCategories.value = selectedCategories.value.filter { cat ->
                                categories.find { it.GUID.equals(cat.GUID, true) } != null
                            }.toMutableList()
                            onSelected(selectedCategories.value.toList())
                            saveSelectedCategories(selectedCategories.value, shows)
                        }.clip(shape),
                        shape = shape,
                        border = BorderStroke(2.dp, MaterialTheme.colors.primary),
                        color = fillColor.value,
                    ) {
                        Row {
                            Text(
                                item.name,
                                Modifier.padding(7.dp).align(Alignment.CenterVertically),
                                color = textColor.value
                            )
                        }
                    }
                }
            }
        }

        HorizontalScrollbar(
            rememberScrollbarAdapter(state),
            Modifier.fillMaxWidth().align(Alignment.BottomStart).height(8.dp)//.padding(0.dp, 3.dp)
        )
    }
}