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
import kotlinx.serialization.Serializable
import moe.styx.Main
import moe.styx.logic.*
import moe.styx.logic.data.DataManager
import moe.styx.types.eqI
import moe.styx.types.json
import net.lingala.zip4j.ZipFile
import java.io.File

val videoOutputDriverChoices = listOf("gpu-next", "gpu")
val gpuApiChoices = listOf("auto", "vulkan", "d3d11", "opengl")
val profileChoices = listOf("high", "normal", "light", "fast")
val debandIterationsChoices = listOf("4", "3", "2")


object MpvDesc {
    val profileDescription = """
        Sorted from best to worst (and slowest to fastest).
        This mostly affects scaling and if you have a relatively modern machine, it should have no issues with "high".
    """.trimIndent()

    val hwDecoding = """
        Use your GPU to decode the video.
        This is both faster and more efficient.
        Leave on if at all possible and not causing issues.
    """.trimIndent()

    val deband = """
        Removes colorbanding from the video.
        Keep this on if you don't have any performance issues.
        Can also be toggled with h in the player.
    """.trimIndent()

    val oversample = """
        Interpolates by showing every frame 2.5 times (on 60hz).
        This can make the video feel way smoother but cause issues with advanced subtitles.
        G-Sync makes this redundant.
    """.trimIndent()

    val gpuAPI = """
        The rendering backend used.
        Keep at auto if you don't know what you're doing.
        Auto is basically d3d11 on windows and vulkan everywhere else.
    """.trimIndent()

    val outputDriver = """
        Keep at gpu-next if you don't know what you're doing and if not causing issues.
        This is in theory faster and has higher quality.
    """.trimIndent()

    val downmix = """
        This forces a custom downmix for surround sound audio.
        May be useful if you think that surround audio sounds like crap on your headphones.
        Or if you just don't have a 5.1+ setup.
    """.trimIndent()

    val dither10bit = """
        Forces dithering to 10bit because MPV's auto detection is broken.
        Only use if you know that your display is 10bit.
    """.trimIndent()
}

@Serializable
data class MpvPreferences(
    val gpuAPI: String = gpuApiChoices[0],
    val videoOutputDriver: String = videoOutputDriverChoices[0],
    val profile: String = profileChoices[0],
    val deband: Boolean = true,
    val debandIterations: String = debandIterationsChoices[0],
    val hwDecoding: Boolean = true,
    val oversampleInterpol: Boolean = false,
    val dither10bit: Boolean = false,
    val customDownmix: Boolean = false,
    val preferGerman: Boolean = false,
    val preferEnDub: Boolean = false,
    val preferDeDub: Boolean = false,
)

fun generateNewConfig() {
    val pref = MpvUtils.getPreferences()
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

    fun getPreferences() = runCatching { json.decodeFromString<MpvPreferences>(Main.settings["mpv-preferences", ""]) }.getOrNull() ?: MpvPreferences()

    fun getSlangArg(): String {
        val pref = getPreferences()
        return if (pref.preferGerman)
            "de,ger,en,eng"
        else
            "en,eng,de,ger"
    }

    fun getAlangArg(): String {
        val pref = getPreferences()
        return if (pref.preferEnDub)
            "en,eng,jp,jpn,de,ger"
        else if (pref.preferDeDub)
            "de,ger,jp,jpn,en,eng"
        else
            "jp,jpn,en,eng,de,ger"
    }

    fun getProfile(): String {
        val pref = getPreferences()
        return "styx${pref.profile.makeFirstLetterBig()}"
    }

    fun checkVersionAndDownload() {
        if (!hasInternet())
            return
        launchGlobal {
            delay(8000)
            val response = httpClient.get(Endpoints.MPV.url())

            if (!response.status.isSuccess()) {
                Log.w("MpvUtils::checkVersionAndDownload") { "Failed to check for mpv version." }
                return@launchGlobal
            }
            isMpvDownloading = true
            val version = response.bodyAsText().trim()
            if (Main.settings["mpv-version", "None"].trim() eqI version && DataManager.getMpvDir().exists() && DataManager.getMpvConfDir().exists()) {
                Log.i { "mpv version is up-to-date." }
                isMpvDownloading = false
                return@launchGlobal
            }

            val temp = File(DataManager.getAppDir(), "temp.zip")
            if (temp.exists())
                temp.delete()

            launch {
                Log.i { "Downloading latest mpv bundle" }

                val downloadResp = httpClient.get(Endpoints.MPV_DOWNLOAD.url())
                if (DataManager.getMpvDir().exists() && downloadResp.status.isSuccess())
                    DataManager.getMpvDir().deleteRecursively()
                val openChannel = downloadResp.bodyAsChannel()
                openChannel.copyAndClose(temp.writeChannel())
                delay(500)

                runCatching {
                    ZipFile(temp).extractAll(DataManager.getMpvDir().absolutePath)
                    Main.settings["mpv-version"] = version
                    temp.delete()
                    generateNewConfig()
                    isMpvDownloading = false
                }.onFailure { Log.e("MpvUtils Downloader", it) { "Failed to extract downloaded file!" } }
            }
        }
    }
}

