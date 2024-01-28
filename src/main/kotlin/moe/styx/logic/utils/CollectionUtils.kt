package moe.styx.logic.utils

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

