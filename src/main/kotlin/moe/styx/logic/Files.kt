package moe.styx.logic

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

    fun cleanUpLegacyData() {
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
    }
}