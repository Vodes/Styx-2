package moe.styx.logic.runner

import moe.styx.common.extension.eqI
import moe.styx.common.isWindows
import moe.styx.common.util.Log
import java.awt.Desktop
import java.io.File
import java.net.URI

fun getExecutableFromPath(name: String): File? {
    var name = name
    if (isWindows && !name.contains(".exe"))
        name = "$name.exe"
    val pathDirs = System.getenv("PATH").split(File.pathSeparator)
        .map { File(it) }.filter { it.exists() && it.isDirectory }

    return pathDirs.flatMap { it.listFiles()?.asList() ?: listOf() }.find { (if (isWindows) it.name else it.nameWithoutExtension) eqI name }
}

fun openURI(uri: String) = openURI(URI(uri))

fun openURI(uri: URI) {
    val xdgOpen = getExecutableFromPath("xdg-open")
    if (xdgOpen != null) {
        val result = ProcessBuilder(listOf(xdgOpen.absolutePath, uri.toString())).start().waitFor()
        if (result == 0)
            return
    }
    if (Desktop.isDesktopSupported()) {
        runCatching { Desktop.getDesktop().browse(uri) }.onFailure { Log.e(exception = it) { "Failed to open URI: $uri" } }
    }
}