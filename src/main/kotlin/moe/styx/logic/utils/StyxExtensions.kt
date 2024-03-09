package moe.styx.logic.utils

import cafe.adriel.voyager.navigator.Navigator
import moe.styx.common.data.MappingCollection
import moe.styx.common.data.Media
import moe.styx.common.extension.toBoolean
import moe.styx.common.json
import moe.styx.views.anime.AnimeDetailView
import moe.styx.views.anime.MovieDetailView

fun Navigator.pushMediaView(media: Media, replace: Boolean = false) {
    val view = if (media.isSeries.toBoolean()) AnimeDetailView(media.GUID) else MovieDetailView(media.GUID)
    if (replace)
        this.replace(view)
    else
        this.push(view)
}

enum class StackType {
    ANILIST,
    TMDB,
    MAL
}

fun Media.getURLFromMap(type: StackType): String? {
    val mappings = runCatching {
        json.decodeFromString<MappingCollection>(this.metadataMap!!)
    }.getOrNull() ?: return null
    return when (type) {
        StackType.TMDB -> {
            mappings.tmdbMappings.minByOrNull { it.remoteID }?.remoteID?.let { "https://themoviedb.org/${if (this.isSeries.toBoolean()) "tv" else "movie"}/$it" }
        }

        StackType.MAL -> mappings.malMappings.minByOrNull { it.remoteID }?.remoteID?.let { "https://myanimelist.net/anime/$it" }
        else -> mappings.anilistMappings.minByOrNull { it.remoteID }?.remoteID?.let { "https://anilist.co/anime/$it" }
    }
}