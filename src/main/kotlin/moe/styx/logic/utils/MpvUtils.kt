package moe.styx.logic.utils

import com.russhwolf.settings.get
import com.russhwolf.settings.set
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.styx.common.compose.http.Endpoints
import moe.styx.common.compose.settings
import moe.styx.common.compose.utils.Log
import moe.styx.common.compose.utils.MpvPreferences
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.extension.eqI
import moe.styx.common.http.httpClient
import moe.styx.common.util.launchGlobal
import moe.styx.logic.data.DataManager
import net.lingala.zip4j.ZipFile
import java.io.File

fun generateNewConfig() {
    val pref = MpvPreferences.getOrDefault()
    val baseConfig = File(DataManager.getMpvConfDir(), "base.conf")
    if (!baseConfig.exists())
        return

    val dynamics = File(DataManager.getMpvConfDir(), "dynamic-profiles")
    val profiles = File(dynamics, "quality.conf")
    val downmix = File(dynamics, "downmix.conf")
    val oversample = File(dynamics, "oversample-interpolate.conf")
    val customConf = File(DataManager.getAppDir(), "custom-mpv.conf")
    val customStr = if (customConf.exists()) "## Custom conf file\n\n${customConf.readText().trim()}\n\n## End" else "\n\n##End"
    val gpuAPI = if (pref.gpuAPI eqI "auto") {
        if (System.getProperty("os.name").contains("win", true))
            "d3d11"
        else
            "vulkan"
    } else pref.gpuAPI

    val newConfig = """
## This file is AUTO GENERATED - Changing anything here won't last.
${baseConfig.readText().trim()}
        
## Styx User Settings

${if (pref.deband) "deband=yes" else "deband=no"}
deband-iterations=${pref.debandIterations}
vo=${pref.videoOutputDriver}
gpu-api=$gpuAPI
hwdec=${if (pref.hwDecoding) "auto-safe" else "no"}
dither-depth=${if (pref.dither10bit) "10" else "8"}
        
$customStr
        
${if (pref.oversampleInterpol) "${oversample.readText()}\n" else ""}
${if (pref.customDownmix) "${downmix.readText()}\n" else ""}
${profiles.readText()}
    """.trimIndent()

    File(DataManager.getMpvConfDir(), "mpv.conf").writeText(newConfig)
}

object MpvUtils {
    var isMpvDownloading = false

    fun checkVersionAndDownload() {
        if (ServerStatus.lastKnown == ServerStatus.UNKNOWN)
            return
        launchGlobal {
            delay(8000)
            val response = httpClient.get(Endpoints.MPV.url)

            if (!response.status.isSuccess()) {
                Log.w("MpvUtils::checkVersionAndDownload") { "Failed to check for mpv version." }
                return@launchGlobal
            }
            isMpvDownloading = true
            val version = response.bodyAsText().trim()
            if (settings["mpv-version", "None"].trim() eqI version && DataManager.getMpvDir().exists() && DataManager.getMpvConfDir().exists()) {
                Log.i { "mpv version is up-to-date." }
                isMpvDownloading = false
                return@launchGlobal
            }

            val temp = File(DataManager.getAppDir(), "temp.zip")
            if (temp.exists())
                temp.delete()

            launch {
                Log.i { "Downloading latest mpv bundle" }

                val downloadResp = httpClient.get(Endpoints.MPV_DOWNLOAD.url)
                if (DataManager.getMpvDir().exists() && downloadResp.status.isSuccess())
                    DataManager.getMpvDir().deleteRecursively()
                val openChannel = downloadResp.bodyAsChannel()
                openChannel.copyAndClose(temp.writeChannel())
                delay(500)

                runCatching {
                    ZipFile(temp).extractAll(DataManager.getMpvDir().absolutePath)
                    settings["mpv-version"] = version
                    temp.delete()
                    generateNewConfig()
                    isMpvDownloading = false
                }.onFailure { Log.e("MpvUtils Downloader", it) { "Failed to extract downloaded file!" } }
            }
        }
    }
}

