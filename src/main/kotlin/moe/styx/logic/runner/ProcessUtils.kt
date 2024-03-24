package moe.styx.logic.runner

import moe.styx.common.extension.eqI
import moe.styx.common.isWindows
import java.io.File

fun getExecutableFromPath(name: String): File? {
    var name = name
    if (isWindows && !name.contains(".exe"))
        name = "$name.exe"
    val pathDirs = System.getenv("PATH").split(File.pathSeparator)
        .map { File(it) }.filter { it.exists() && it.isDirectory }

    return pathDirs.flatMap { it.listFiles()?.asList() ?: listOf() }.find { (if (isWindows) it.name else it.nameWithoutExtension) eqI name }
}