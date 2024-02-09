package moe.styx.logic.utils

import moe.styx.common.data.*
import moe.styx.common.extension.eqI
import moe.styx.common.extension.toBoolean
import moe.styx.common.json
import moe.styx.common.util.isClose
import moe.styx.logic.data.DataManager
import java.time.*
import java.time.temporal.TemporalAdjusters

fun ScheduleWeekday.dayOfWeek(): DayOfWeek {
    return when (this) {
        ScheduleWeekday.MONDAY -> DayOfWeek.MONDAY
        ScheduleWeekday.TUESDAY -> DayOfWeek.TUESDAY
        ScheduleWeekday.WEDNESDAY -> DayOfWeek.WEDNESDAY
        ScheduleWeekday.THURSDAY -> DayOfWeek.THURSDAY
        ScheduleWeekday.FRIDAY -> DayOfWeek.FRIDAY
        ScheduleWeekday.SATURDAY -> DayOfWeek.SATURDAY
        else -> DayOfWeek.SUNDAY
    }
}

fun MediaSchedule.getTargetTime(): LocalDateTime {
    val now = LocalDate.now(ZoneId.of("Europe/Berlin"))
    val adjusted = now.atTime(this.hour, this.minute)
    val target = adjusted.with(TemporalAdjusters.next(this.day.dayOfWeek()))
    return target.atZone(ZoneId.of("Europe/Berlin")).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
}

fun Media.find(search: String): Boolean {
    return name.isClose(search.trim()) || nameEN.isClose(search.trim()) || nameJP.isClose(search.trim())
}

fun Media.isFav() = DataManager.favourites.value.find { it.mediaID.equals(GUID, true) } != null

fun Media.getCategory(): Category {
    val categories = DataManager.categories.value.sortedByDescending { it.sort }
    return categories.find { it.GUID eqI categoryID } ?: categories.last()
}

fun Media.favAdded(): Long {
    val fav = DataManager.favourites.value.find { it.mediaID.equals(GUID, true) }
    return fav?.added ?: 0L
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