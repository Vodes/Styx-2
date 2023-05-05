package moe.styx.moe.styx.components.misc

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
    tint: Color = MaterialTheme.colors.onSurface,
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
    TooltipArea(delayMillis = 325, tooltip = {
        Card(
            backgroundColor = MaterialTheme.colors.surface,
            contentColor = MaterialTheme.colors.onSurface,
            elevation = 6.dp
        ) {
            Text(text, modifier = modifier.padding(6.dp))
        }
    }, content = content)
}