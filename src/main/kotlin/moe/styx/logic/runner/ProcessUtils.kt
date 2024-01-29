package moe.styx.logic.runner

import moe.styx.types.eqI
import java.io.File

fun getExecutableFromPath(name: String): File? {
    val isWin = System.getProperty("os.name").contains("win", true)
    var name = name
    if (isWin && !name.contains(".exe"))
        name = "$name.exe"
    val pathDirs = System.getenv("PATH").split(File.pathSeparator)
        .map { File(it) }.filter { it.exists() && it.isDirectory }

    return pathDirs.flatMap { it.listFiles()?.asList() ?: listOf() }.find { (if (isWin) it.name else it.nameWithoutExtension) eqI name }
}