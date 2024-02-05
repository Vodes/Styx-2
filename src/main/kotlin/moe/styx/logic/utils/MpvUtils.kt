package moe.styx.logic.utils

import com.russhwolf.settings.get
import kotlinx.serialization.Serializable
import moe.styx.Main
import moe.styx.logic.data.DataManager
import moe.styx.types.eqI
import moe.styx.types.json
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
    val preferGerman: Boolean = false,
    val preferEnDub: Boolean = false,
    val preferDeDub: Boolean = false,
)

fun getPreferences() = runCatching { json.decodeFromString<MpvPreferences>(Main.settings["mpv-preferences", ""]) }.getOrNull() ?: MpvPreferences()

fun generateNewConfig() {
    val pref = getPreferences()
    val baseConfig = File(DataManager.getMpvConfDir(), "base.conf")
    val dynamics = File(DataManager.getMpvConfDir(), "dynamic-profiles")
    val profiles = File(dynamics, "quality.conf")
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
        
$customStr
        
${if (pref.oversampleInterpol) oversample.readText() else ""}
        
${profiles.readText()}
    """.trimIndent()

    File(DataManager.getMpvConfDir(), "mpv.conf").writeText(newConfig)
}

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