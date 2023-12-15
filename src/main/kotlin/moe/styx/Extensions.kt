package moe.styx

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.math.floor
import kotlin.math.min

private val small = DecimalFormat("#.#")
private val big = DecimalFormat("#.##")

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

fun Double.round(decimals: Int = 2): Double {
    return BigDecimal(this).setScale(decimals, RoundingMode.HALF_UP).toDouble()
}

fun String.makeFirstLetterBig(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

fun String.equalsAny(vararg strings: String, trim: Boolean = true, ignoreCase: Boolean = true): Boolean {
    for (s in strings) {
        var s2 = this
        val s1 = if (trim) s.trim().also { s2 = s2.trim() } else s
        if (s2.equals(s1, ignoreCase)) {
            return true
        }
    }
    return false
}

fun String.equalsAny(strings: List<String>, trim: Boolean = true, ignoreCase: Boolean = true): Boolean {
    return equalsAny(*strings.toTypedArray(), trim = trim, ignoreCase = ignoreCase)
}

fun String.getLevenshteinScore(other: String): Int {
    val s1 = this.lowercase()
    val s2 = other.lowercase()
    if (s1 == s2) {
        return 0
    }
    if (s1.isEmpty()) {
        return s2.length
    }
    if (s2.isEmpty()) {
        return s1.length
    }
    val s1Length = s1.length + 1
    val s2Length = s2.length + 1

    var cost = Array(s1Length) { it }
    var newCost = Array(s1Length) { 0 }

    for (i in 1 until s2Length) {
        newCost[0] = i

        for (j in 1 until s1Length) {
            val match = if (s1[j - 1] == s2[i - 1]) 0 else 1

            val costReplace = cost[j - 1] + match
            val costInsert = cost[j] + 1
            val costDelete = newCost[j - 1] + 1

            newCost[j] = min(min(costInsert, costDelete), costReplace)
        }

        val swap = cost
        cost = newCost
        newCost = swap
    }
    return cost[s1Length - 1]
}

fun MutableList<String>.containsIgnoreCase(s: String): Boolean {
    for (existing in this) {
        if (existing.trim().equals(s.trim(), true)) {
            return true;
        }
    }
    return false
}

fun MutableList<String>.addIfNotExisting(s: String) {
    if (this.containsIgnoreCase(s))
        return

    this.add(s)
}

