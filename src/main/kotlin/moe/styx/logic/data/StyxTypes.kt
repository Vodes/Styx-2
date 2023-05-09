package moe.styx.logic.data

import com.aallam.similarity.Cosine
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import moe.styx.dataManager
import moe.styx.getLevenshteinScore
import moe.styx.moe.styx.navigation.favsTab
import moe.styx.requestQueue
import java.time.*
import java.time.temporal.TemporalAdjusters

@Serializable
data class Media(
    val GUID: String, val name: String, val nameJP: String?, val nameEN: String?, val synopsisEN: String?,
    val synopsisDE: String?, val thumbID: String?, val bannerID: String? = null, val categoryID: String? = null,
    val prequel: String? = null, val sequel: String? = null, val genres: String? = null, val tags: String? = null,
    val metadataMap: String? = null, val isSeries: Int = 1, val added: Long = 0
)

@Serializable
data class Category(val GUID: String, val sort: Int, val isSeries: Int, val isVisible: Int, val name: String)

@Serializable
data class Image(
    val GUID: String, val hasWEBP: Int? = 0, val hasPNG: Int? = 0,
    val hasJPG: Int? = 0, val externalURL: String? = null, val type: Int = 0
)

@Serializable
data class MediaEntry(
    val GUID: String, val mediaID: String, val timestamp: Long, val entryNumber: String,
    val nameEN: String?, val nameDE: String?, val synopsisEN: String?, val synopsisDE: String?,
    val thumbID: String?, val filePath: String, val fileSize: Long, val originalName: String?
)

@Serializable
data class MediaInfo(
    val entryID: String, var videoCodec: String, var videoBitdepth: Int, var videoRes: String,
    var hasEnglishDub: Int, var hasGermanDub: Int, var hasGermanSub: Int
)

@Serializable
data class MediaWatched(
    val entryID: String,
    val userID: String,
    var lastWatched: Long,
    var progress: Long,
    var progressPercent: Float,
    var maxProgress: Float
)

@Serializable
data class User(
    val GUID: String, var name: String, var discordID: String, val added: Long, var lastLogin: Long,
    var permissions: Int
)

@Serializable
data class Favourite(val mediaID: String, var userID: String, var added: Long)


@Serializable
data class LoginResponse(
    val name: String, val permissions: Int, val accessToken: String, val watchToken: String,
    val tokenExpiry: Long, val refreshToken: String? = null
)

@Serializable
data class CreationResponse(val GUID: String, val code: Int, val expiry: Long)

@Serializable
data class DeviceInfo(
    val type: String, val name: String?, val model: String?, val cpu: String?, val gpu: String?,
    val os: String, val osVersion: String?, var jvm: String?, var jvmVersion: String?
)

@Serializable
data class UnregisteredDevice(val GUID: String, val deviceInfo: DeviceInfo, val codeExpiry: Long, val code: Int)

@Serializable
data class Device(
    var GUID: String, var userID: String, var name: String, var deviceInfo: DeviceInfo,
    var lastUsed: Long, var accessToken: String, var watchToken: String, var refreshToken: String, var tokenExpiry: Long
)

@Serializable
data class ApiResponse(var code: Int, var message: String?, var silent: Boolean = false)

@Serializable
data class MediaSchedule(
    var mediaID: String,
    var day: ScheduleWeekday,
    var hour: Int,
    var minute: Int,
    var isEstimated: Int = 0,
    var finalEpisodeCount: Int = 0
)

enum class ScheduleWeekday {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

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