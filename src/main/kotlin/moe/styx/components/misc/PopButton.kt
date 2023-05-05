package moe.styx.moe.styx.components.misc

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.datetime.Clock
import moe.styx.moe.styx.navigation.LocalGlobalNavigator

var lastPop = 0L

@Composable
fun PopButton(nav: Navigator? = null) {
    var navigator = nav
    if (nav == null)
        navigator = LocalGlobalNavigator.current
    IconButton(onClick = {
        val now = Clock.System.now().toEpochMilliseconds()
        val allowPop = (now - lastPop) > 800
        if (navigator!!.canPop && allowPop) {
            navigator.pop()
            lastPop = now
        }
    }, content = { Icon(Icons.Filled.Close, null) })
}