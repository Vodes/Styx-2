package moe.styx.moe.styx.components.misc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.TabOptions

@Composable
fun createTabOptions(title: String, icon: ImageVector): TabOptions {
    val iconPainter = rememberVectorPainter(icon)

    return remember {
        TabOptions(
            index = 0u,
            title = title,
            icon = iconPainter
        )
    }
}