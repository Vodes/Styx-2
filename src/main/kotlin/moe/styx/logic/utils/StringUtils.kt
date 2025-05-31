package moe.styx.logic.utils

fun String.removeSomeHTMLTags(): String {
    return this.replace("<i>", "").replace("</i>", "").replace("<b>", "").replace("</b>", "")
}