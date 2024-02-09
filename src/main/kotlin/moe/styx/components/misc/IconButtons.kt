package moe.styx.components.misc

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.datetime.Clock
import moe.styx.common.data.Media
import moe.styx.logic.data.DataManager
import moe.styx.logic.loops.RequestQueue
import moe.styx.logic.utils.isFav
import moe.styx.navigation.LocalGlobalNavigator
import moe.styx.navigation.favsTab

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
    var isFav by remember { mutableStateOf(media.isFav()) }
    IconButton({
        if (!isFav)
            RequestQueue.addFav(media)
        else
            RequestQueue.removeFav(media)
        isFav = media.isFav()
        favsTab.searchState.value = favsTab.mediaSearch.getDefault(updateList = DataManager.media.value.filter { it.isFav() })
    }) {
        if (isFav)
            Icon(Icons.Filled.Star, "Fav")
        else
            Icon(Icons.Outlined.StarOutline, "Not fav")
    }
}