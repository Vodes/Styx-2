package moe.styx.logic.utils

import kotlinx.datetime.Clock

fun currentUnixSeconds(): Long {
    return Clock.System.now().epochSeconds
}