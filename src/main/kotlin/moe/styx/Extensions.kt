package moe.styx

import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.floor

private val small = DecimalFormat("#.#")
private val big = DecimalFormat("#.##")
fun Boolean.toInt() = if (this) 1 else 0
fun Int?.toBoolean() = if (this == null) false else (this > 0)

fun Long.readableSize(useBinary: Boolean = false): String {
    val units = if (useBinary) listOf("B", "KiB", "MiB", "GiB", "TiB") else listOf("B", "KB", "MB", "GB", "TB")
    val divisor = if (useBinary) 1024 else 1000
    var steps = 0
    var current = this.toDouble()
    while (floor((current / divisor)) > 0) {
        current = (current / divisor)
        steps++;
    }
    small.roundingMode = RoundingMode.CEILING.also { big.roundingMode = it }
    return "${(if (steps > 2) big else small).format(current)} ${units[steps]}"
}