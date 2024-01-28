package moe.styx.logic.runner

import moe.styx.types.eqI
import java.io.File

fun getExecutableFromPath(name: String): File? {
    val pathDirs = System.getenv("PATH").split(File.pathSeparator)
        .map { File(it) }.filter { it.exists() && it.isDirectory }

    return pathDirs.flatMap { it.listFiles()?.asList() ?: listOf() }.find { it.nameWithoutExtension eqI name }
}