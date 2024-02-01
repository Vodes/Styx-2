package moe.styx.components.misc

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import moe.styx.theme.AppShapes

@Composable
fun OutlinedText(text: String, lineColor: Color = MaterialTheme.colorScheme.primary, fillColor: Color = MaterialTheme.colorScheme.surface) {
    Surface(
        Modifier.padding(2.dp, 2.dp).sizeIn(0.dp, 36.dp).clip(AppShapes.medium),
        shape = AppShapes.medium,
        border = BorderStroke(2.dp, lineColor),
        color = fillColor,
    ) {
        Row {
            Text(text, Modifier.padding(7.dp).align(Alignment.CenterVertically))
        }
    }
}