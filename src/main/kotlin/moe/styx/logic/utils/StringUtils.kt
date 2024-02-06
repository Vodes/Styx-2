package moe.styx.logic.utils

import com.aallam.similarity.Cosine
import java.util.*

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

fun String.removeSomeHTMLTags(): String {
    return this.replace("<i>", "").replace("</i>", "").replace("<b>", "").replace("</b>", "")
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

            newCost[j] = kotlin.math.min(kotlin.math.min(costInsert, costDelete), costReplace)
        }

        val swap = cost
        cost = newCost
        newCost = swap
    }
    return cost[s1Length - 1]
}

private val cos = Cosine(3)

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
