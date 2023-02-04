package moe.styx.moe.styx.logic.runner

import java.io.File

fun getExecutableFromPath(name: String): File? {
    for (s in System.getenv("PATH").split(File.pathSeparator)) {
        val dir = File(s)
        if (!dir.exists() || !dir.isDirectory)
            continue
        val files = dir.listFiles() ?: continue
        for (file in files) {
            if (file.name.equals(name, true))
                return file
        }
    }
    return null
}