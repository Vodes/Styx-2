package moe.styx.components.misc

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ExpandIconButton(
    modifier: Modifier = Modifier,
    tooltip: String? = null,
    tooltipExpanded: String? = null,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onExpandChange: (Boolean) -> Unit = {}
) {
    val isExpanded = remember { mutableStateOf(false) }
    IconButton(onClick = {
        isExpanded.value = !isExpanded.value
        onExpandChange(isExpanded.value)
    }, content = {
        if (isExpanded.value) {
            val icon = Icons.Filled.ExpandLess
            if (!tooltipExpanded.isNullOrBlank()) {
                ToolTipWrapper(tooltipExpanded) {
                    Icon(icon, tooltipExpanded, modifier, tint)
                }
            } else {
                Icon(icon, tooltipExpanded, modifier, tint)
            }
        } else {
            val icon = Icons.Filled.ExpandMore
            if (!tooltip.isNullOrBlank()) {
                ToolTipWrapper(tooltip) {
                    Icon(icon, tooltip, modifier, tint)
                }
            } else {
                Icon(icon, tooltip, modifier, tint)
            }
        }
    })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolTipWrapper(text: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    TooltipArea(delayMillis = 425, tooltip = {
        ElevatedCard {
            Text(text, modifier = modifier.padding(5.dp))
        }
    }, content = content)
}