package moe.styx.moe.styx.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.datetime.Clock

var lastPop = 0L

@Composable
fun PopButton(nav: Navigator) {
    IconButton(onClick = {
        val now = Clock.System.now().toEpochMilliseconds()
        val allowPop = (now - lastPop) > 800
        if (nav.canPop && allowPop) {
            nav.pop()
            lastPop = now
        }
    }, content = { Icon(Icons.Filled.Close, null) })
}