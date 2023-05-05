package moe.styx.moe.styx.components.overviews

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.russhwolf.settings.get
import moe.styx.moe.styx.components.misc.ToolTipWrapper
import moe.styx.settings

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilterTypeIcon(onSelect: (String) -> Unit) {
    val typeState = remember { mutableStateOf(settings["filter-type", "both"]) }

    IconButton(onClick = {
        typeState.value = when (typeState.value.lowercase()) {
            "both" -> "shows"
            "shows" -> "movies"
            else -> "both"
        }
        onSelect(typeState.value)
        settings.putString("filter-type", typeState.value)
    }, content = {
        val text = when (typeState.value.lowercase()) {
            "both" -> "Display All"
            "shows" -> "Display Shows"
            else -> "Display Movies"
        }
        ToolTipWrapper(text) {
            when (typeState.value.lowercase()) {
                "both" -> Icon(Icons.Default.Subscriptions, "")
                "shows" -> Icon(Icons.Default.Tv, "")
                else -> Icon(Icons.Default.Movie, "")
            }
        }
    })
}