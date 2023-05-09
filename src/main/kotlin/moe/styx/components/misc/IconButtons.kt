package moe.styx.moe.styx.components.misc

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.datetime.Clock
import moe.styx.logic.data.Media
import moe.styx.logic.data.isFav
import moe.styx.logic.data.setFav
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

@Composable
fun FavouriteIconButton(media: Media, modifier: Modifier = Modifier) {
    val isFav = remember { mutableStateOf(media.isFav()) }
    IconButton({
        if (setFav(media, !isFav.value))
            isFav.value = media.isFav()
    }) {
        if (isFav.value)
            Icon(Icons.Filled.Star, "Fav")
        else
            Icon(Icons.Outlined.StarOutline, "Not fav")
    }
}