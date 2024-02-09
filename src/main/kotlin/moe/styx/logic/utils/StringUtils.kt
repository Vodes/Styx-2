package moe.styx.logic.utils

val uselessEPTitleRegex = "^(?:Folge|Episode) \\d+\$".toRegex(RegexOption.IGNORE_CASE)
fun String.removeSomeHTMLTags(): String {
    return this.replace("<i>", "").replace("</i>", "").replace("<b>", "").replace("</b>", "")
}