package moe.styx.components.misc

import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableText(text: String, modifier: Modifier = Modifier, maxLines: Int = 1, style: TextStyle = MaterialTheme.typography.labelMedium) {
    var lines by remember { mutableStateOf(maxLines) }
    ElevatedCard(Modifier.clickable {
        lines = if (lines != maxLines) maxLines else Int.MAX_VALUE
    }, elevation = CardDefaults.elevatedCardElevation(2.dp)) {
        Text(text, modifier, maxLines = lines, overflow = TextOverflow.Ellipsis, style = style)
    }
}