package moe.styx.logic.utils

import cafe.adriel.voyager.navigator.Navigator
import moe.styx.common.data.Media
import moe.styx.common.extension.toBoolean
import moe.styx.views.anime.AnimeDetailView
import moe.styx.views.anime.MovieDetailView

fun Navigator.pushMediaView(media: Media, replace: Boolean = false) {
    val view = if (media.isSeries.toBoolean()) AnimeDetailView(media.GUID) else MovieDetailView(media.GUID)
    if (replace)
        this.replace(view)
    else
        this.push(view)
}