package moe.styx.logic

import moe.styx.common.extension.currentUnixSeconds
import java.io.File

object Files {

    fun getDataDir(): File {
        cleanUpLegacyData()
        return File(getAppDir(), "Data").also { it.mkdirs() }
    }

    fun getCacheDir(): File {
        return File(getAppDir(), "Cache").also { it.mkdirs() }
    }

    fun getAppDir(): File {
        return if (System.getProperty("os.name").lowercase().contains("win")) {
            val styxDir = File(System.getenv("APPDATA"), "Styx")
            val dir = File(styxDir, "App")
            dir.mkdirs()
            dir
        } else {
            var configDir = File(System.getProperty("user.home"), ".config")
            val xdgDirEnv = System.getenv("XDG_CONFIG_HOME")
            if (!xdgDirEnv.isNullOrBlank() && File(xdgDirEnv).exists())
                configDir = File(xdgDirEnv)
            val styxDir = File(configDir, "Styx")
            val dir = File(styxDir, "App")
            dir.mkdirs()
            dir
        }
    }

    fun getMpvDir(): File {
        return File(getAppDir(), "mpv")
    }

    fun getMpvConfDir(): File {
        return File(getMpvDir(), "portable_config")
    }

    private fun cleanUpLegacyData() {
        val imageDir = File(getAppDir(), "Images")
        if (imageDir.exists() && imageDir.isDirectory) {
            runCatching { imageDir.deleteRecursively() }
        }
        val oldChangesJson = File(getAppDir(), "changes.json")
        if (oldChangesJson.exists()) {
            runCatching {
                oldChangesJson.delete()
                File(getAppDir(), "Data").deleteRecursively()
            }
        }
        val logsDir = File(getAppDir(), "Logs")
        if (logsDir.exists()) {
            val files = logsDir.walkTopDown().sorted().filter { it.isFile }.toList()
            if (files.size > 25) {
                files.take(files.size - 10).forEach {
                    runCatching { it.delete() }
                }
            }
        }
        val currentMillis = currentUnixSeconds() * 1000
        val installerFiles = getAppDir().listFiles()?.toList() ?: emptyList()
        installerFiles
            .filter { it.name.contains(".msi", true) }
            .filter { (it.lastModified() - 30000) > currentMillis }
            .forEach {
                runCatching { it.delete() }
            }
    }
}