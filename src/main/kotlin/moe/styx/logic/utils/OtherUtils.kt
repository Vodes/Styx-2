package moe.styx.logic.utils

import kotlinx.datetime.*
import moe.styx.types.padString

fun currentUnixSeconds(): Long {
    return Clock.System.now().epochSeconds
}

fun Long.toDateString(): String {
    val instant = Instant.fromEpochSeconds(this)
    val datetime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${datetime.year}-${datetime.monthNumber.padString()}-${datetime.dayOfMonth.padString()} "
}