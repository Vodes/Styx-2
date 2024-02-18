package moe.styx.components.misc

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ExpandIconButton(
    modifier: Modifier = Modifier,
    tooltip: String? = null,
    tooltipExpanded: String? = null,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    isExpanded: Boolean,
    onClick: () -> Unit = {}
) {
    if (!tooltip.isNullOrBlank() && !tooltipExpanded.isNullOrBlank()) {
        IconButtonWithTooltip(
            if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            if (isExpanded) tooltipExpanded else tooltip,
            tint = tint,
            modifier = modifier,
            onClick = onClick
        )
    } else {
        IconButton(onClick, modifier = modifier) {
            Icon(if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, "", tint = tint)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToolTipWrapper(text: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    TooltipArea(delayMillis = 450, tooltip = {
        ElevatedCard(elevation = CardDefaults.elevatedCardElevation(5.dp)) {
            Text(text, modifier = modifier.padding(5.dp))
        }
    }, content = content)
}