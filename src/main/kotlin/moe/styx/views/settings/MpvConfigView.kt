package moe.styx.views.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.russhwolf.settings.set
import kotlinx.serialization.encodeToString
import moe.styx.common.compose.components.misc.MpvCheckbox
import moe.styx.common.compose.components.misc.SettingsCheckbox
import moe.styx.common.compose.components.misc.StringChoices
import moe.styx.common.compose.settings
import moe.styx.common.compose.utils.*
import moe.styx.common.isWindows
import moe.styx.common.json
import moe.styx.components.MainScaffold
import moe.styx.logic.utils.generateNewConfig

class MpvConfigView : Screen {
    @Composable
    override fun Content() {
        var preferences by remember { mutableStateOf(MpvPreferences.getOrDefault()) }
        MainScaffold(title = "Mpv Configuration") {
            Column {
                Column(Modifier.padding(8.dp).fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                    Text("General", Modifier.padding(6.dp, 3.dp), style = MaterialTheme.typography.titleLarge)
                    SettingsCheckbox("Use system MPV", "mpv-system", !isWindows(), paddingValues = PaddingValues(13.dp, 10.dp))
                    SettingsCheckbox("Use styx config with system mpv", "mpv-system-styx-conf", false, paddingValues = PaddingValues(13.dp, 10.dp))
                    SettingsCheckbox("Try to use flatpak (Linux only)", "mpv-flatpak", false, paddingValues = PaddingValues(13.dp, 10.dp))
                    SettingsCheckbox(
                        "Play next automatically",
                        "mpv-play-next",
                        true,
                        description = "Plays next episode (if any) when you reached the end and are paused/stopped.",
                        paddingValues = PaddingValues(13.dp, 10.dp)
                    )
                    HorizontalDivider(Modifier.fillMaxWidth().padding(12.dp, 8.dp), thickness = 2.dp)
                    Text("Performance / Quality", Modifier.padding(6.dp, 3.dp), style = MaterialTheme.typography.titleLarge)
                    Column(Modifier.padding(6.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            MpvCheckbox(
                                "Deband",
                                preferences.deband,
                                MpvDesc.deband
                            ) { preferences = preferences.copy(deband = it) }
                            StringChoices("Deband Iterations", debandIterationsChoices, "Higher = better (& slower)", preferences.debandIterations) {
                                preferences = preferences.copy(debandIterations = it)
                                it
                            }
                        }
                        StringChoices("Profile", profileChoices, MpvDesc.profileDescription, preferences.profile) {
                            preferences = preferences.copy(profile = it)
                            it
                        }
                        Row(verticalAlignment = Alignment.Top) {
                            MpvCheckbox(
                                "Hardware Decoding",
                                preferences.hwDecoding,
                                MpvDesc.hwDecoding
                            ) { preferences = preferences.copy(hwDecoding = it) }
                            MpvCheckbox(
                                "Oversample Interpolate",
                                preferences.oversampleInterpol,
                                MpvDesc.oversample
                            ) { preferences = preferences.copy(oversampleInterpol = it) }
                        }
                        StringChoices("GPU-API", gpuApiChoices, MpvDesc.gpuAPI, preferences.gpuAPI) {
                            preferences = preferences.copy(gpuAPI = it)
                            it
                        }
                        StringChoices("Video Output Driver", videoOutputDriverChoices, MpvDesc.outputDriver, preferences.videoOutputDriver) {
                            preferences = preferences.copy(videoOutputDriver = it)
                            it
                        }
                        MpvCheckbox(
                            "Force Downmix Algorithm",
                            preferences.customDownmix,
                            MpvDesc.downmix
                        ) { preferences = preferences.copy(customDownmix = it) }
                        MpvCheckbox(
                            "Force 10bit Dithering",
                            preferences.dither10bit,
                            MpvDesc.dither10bit
                        ) { preferences = preferences.copy(dither10bit = it) }
                    }
                    HorizontalDivider(Modifier.fillMaxWidth().padding(12.dp, 8.dp), thickness = 2.dp)
                    Text("Language Preferences", style = MaterialTheme.typography.titleLarge)
                    Column(Modifier.padding(6.dp)) {
                        Text(
                            "If nothing here is checked, it will default to English sub",
                            Modifier.padding(10.dp, 4.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        MpvCheckbox(
                            "Prefer German subtitles",
                            preferences.preferGerman
                        ) { preferences = preferences.copy(preferGerman = it) }
                        MpvCheckbox(
                            "Prefer English dub",
                            preferences.preferEnDub
                        ) { preferences = preferences.copy(preferEnDub = it) }
                        MpvCheckbox(
                            "Prefer German dub",
                            preferences.preferDeDub
                        ) { preferences = preferences.copy(preferDeDub = it) }
                    }
                }
                HorizontalDivider(Modifier.fillMaxWidth().padding(12.dp, 8.dp), thickness = 2.dp)
                Button(
                    {
                        settings["mpv-preferences"] = json.encodeToString(preferences)
                        generateNewConfig()
                    },
                    Modifier.padding(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text("Save", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}