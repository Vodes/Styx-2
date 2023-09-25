package moe.styx.logic.data

import com.aallam.similarity.Cosine
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import moe.styx.dataManager
import moe.styx.getLevenshteinScore
import moe.styx.moe.styx.navigation.favsTab
import moe.styx.requestQueue
import moe.styx.types.*
import java.time.*
import java.time.temporal.TemporalAdjusters

@Serializable
data class Changes(val media: Long, val entry: Long)

val cos = Cosine(3)

fun String?.isClose(s: String): Boolean {
    if (!this.isNullOrEmpty()) {
        val score = this.getLevenshteinScore(s).toDouble()
        val maxLen = kotlin.math.max(this.length, s.length).toDouble()
        val compensatedLevScore = (maxLen - score) / maxLen
        val cosineScore = cos.similarity(this, s)
        val avgScore = (compensatedLevScore + cosineScore) / 2

        if (this.startsWith(s, true) ||
            this.equals(s, true) ||
            kotlin.math.max(cosineScore, avgScore) >= 0.3
        ) {
            return true
        }
    }
    return false
}

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

fun Media.isFav(): Boolean {
    val fav = dataManager.favourites.value.find { it.mediaID.equals(GUID, true) }
    return fav != null
}

fun setFav(media: Media, fav: Boolean = true): Boolean = runBlocking {
    var list: MutableList<Favourite> = dataManager.favourites.value.toMutableList()
    if (!fav)
        list.removeIf { it.mediaID.equals(media.GUID, true) }
    else {
        if (!media.isFav()) {
            list.add(Favourite(media.GUID, "", Clock.System.now().epochSeconds))
        }
    }

    dataManager.favourites.value = list.toList()
    favsTab.searchState.value = favsTab.mediaSearch.getDefault(updateList = dataManager.media.value.filter { it.isFav() })
    if (!requestQueue.syncFavs()) {
        requestQueue.status.needsFavSync = true
        requestQueue.save()
        return@runBlocking false
    }
    return@runBlocking true
}

fun Media.getCategory(): Category {
    val categories = dataManager.categories.value.sortedByDescending { it.sort }
    for (cat in categories) {
        if (cat.GUID.equals(categoryID, true))
            return cat
    }
    return categories.last()
}

fun Media.getCategoryName(): String {
    return getCategory().name
}

fun Media.favAdded(): Long {
    val fav = dataManager.favourites.value.find { it.mediaID.equals(GUID, true) }
    return fav?.added ?: 0L
}