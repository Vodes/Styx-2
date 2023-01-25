package moe.styx.moe.styx.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.get
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
        TooltipArea(delayMillis = 250, tooltip = {
            Card(backgroundColor = MaterialTheme.colors.surface, contentColor = MaterialTheme.colors.onSurface) {
                val text = when (typeState.value.lowercase()) {
                    "both" -> "Display All"
                    "shows" -> "Display Shows"
                    else -> "Display Movies"
                }
                Text(text, modifier = Modifier.padding(6.dp))
            }
        }, content = {
            when (typeState.value.lowercase()) {
                "both" -> Icon(Icons.Default.Subscriptions, "")
                "shows" -> Icon(Icons.Default.Tv, "")
                else -> Icon(Icons.Default.Movie, "")
            }
        })
    })
}