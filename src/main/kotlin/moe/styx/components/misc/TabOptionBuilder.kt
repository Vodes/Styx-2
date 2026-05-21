package moe.styx.components.misc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import moe.styx.common.compose.navigation.TabOptions

@Composable
fun createTabOptions(title: String, icon: ImageVector, index: UInt = 0u): TabOptions {
    val iconPainter = rememberVectorPainter(icon)

    return remember(index) {
        TabOptions(
            index = index,
            title = title,
            icon = iconPainter
        )
    }
}
