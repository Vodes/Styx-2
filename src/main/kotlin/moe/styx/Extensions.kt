package moe.styx

fun Boolean.toInt() = if (this) 1 else 0
fun Int?.toBoolean() = if (this == null) false else (this > 0)
