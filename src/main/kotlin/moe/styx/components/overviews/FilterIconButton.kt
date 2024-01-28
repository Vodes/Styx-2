package moe.styx.components.overviews

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import com.russhwolf.settings.get
import moe.styx.components.misc.ToolTipWrapper
import moe.styx.settings

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilterTypeIcon(onSelect: (String) -> Unit) {
    var typeState by remember { mutableStateOf(settings["filter-type", "both"]) }

    IconButton(onClick = {
        typeState = when (typeState.lowercase()) {
            "both" -> "shows"
            "shows" -> "movies"
            else -> "both"
        }
        onSelect(typeState)
        settings.putString("filter-type", typeState)
    }, content = {
        val text = when (typeState.lowercase()) {
            "both" -> "Display All"
            "shows" -> "Display Shows"
            else -> "Display Movies"
        }
        ToolTipWrapper(text) {
            when (typeState.lowercase()) {
                "both" -> Icon(Icons.Default.Subscriptions, "")
                "shows" -> Icon(Icons.Default.Tv, "")
                else -> Icon(Icons.Default.Movie, "")
            }
        }
    })
}