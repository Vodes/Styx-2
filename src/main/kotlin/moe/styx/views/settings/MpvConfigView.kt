package moe.styx.views.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.serialization.encodeToString
import moe.styx.common.compose.components.layout.MainScaffold
import moe.styx.common.compose.components.misc.ExpandableSettings
import moe.styx.common.compose.components.misc.Toggles
import moe.styx.common.compose.components.misc.Toggles.settingsContainer
import moe.styx.common.compose.settings
import moe.styx.common.compose.utils.*
import moe.styx.common.isWindows
import moe.styx.common.json
import moe.styx.logic.Files
import moe.styx.logic.utils.generateNewConfig
import java.io.File

class MpvConfigView : Screen {
    @Composable
    override fun Content() {
        var preferences by remember { mutableStateOf(MpvPreferences.getOrDefault()) }
        var tipsExpanded by remember { mutableStateOf(false) }
        MainScaffold(title = "Mpv Configuration") {
            Column {
                Column(Modifier.padding(8.dp).fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                    ExpandableSettings("MPV Tips and tricks", tipsExpanded, { tipsExpanded = !tipsExpanded }) {
                        SelectionContainer {
                            Text(
                                """
                            Here are some possibly useful keybinds:
                            
                            CTRL+R      Attempts to reload the video, may be useful if the API is having issues and stuff starts buffering.
                            
                            SHIFT+C     Tries to Auto-Crop the video. Useful if the actual content is 21:9 but has black bars in the file itself.
                            
                            SHIFT+W     Opens the Recording/Clip-Maker Menu. These clips are just dumped in your user folder.
                            
                            H           Toggle debanding on the fly.
                            
                            You can also step frame by frame with SHIFT + Arrow Keys.
                            
                            You can also create a custom config that will be persisted through mpv updates by creating a file at:
                            ${File(Files.getAppDir(), "custom-mpv.conf").absolutePath}
                            """.trimIndent(),
                                modifier = Modifier.padding(8.dp, 4.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Column(Modifier.settingsContainer()) {
                        Text("General", Modifier.padding(10.dp, 7.dp), style = MaterialTheme.typography.titleLarge)
                        Toggles.ContainerSwitch(
                            "Use system mpv",
                            "Use your own installed mpv instead of the bundled binaries.\n(Styx only ships binaries for Windows)",
                            value = settings["mpv-system", !isWindows]
                        ) { settings["mpv-system"] = it }
                        Toggles.ContainerSwitch(
                            "Use styx config with system mpv",
                            "Use the bundled custom ui and config with your own mpv.",
                            value = settings["mpv-system-styx-conf", true]
                        ) {
                            settings["mpv-system-styx-conf"] = it
                        }
                        Toggles.ContainerSwitch(
                            "Use flatpak",
                            description = "Not relevant unless you're on linux.",
                            value = settings["mpv-flatpak", false],
                            paddingValues = Toggles.colEndPadding
                        ) { settings["mpv-flatpak"] = it }
                    }

                    Column(Modifier.settingsContainer()) {
                        Text("Language Preferences", Modifier.padding(10.dp, 7.dp), style = MaterialTheme.typography.titleLarge)
                        Text(
                            "If nothing is selected here, it defaults to english subtitles.",
                            Modifier.padding(10.dp, 5.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Toggles.ContainerSwitch(
                            "Prefer German subtitles", value = preferences.preferGerman,
                        ) { preferences = preferences.copy(preferGerman = it) }
                        Toggles.ContainerSwitch(
                            "Prefer German dub", value = preferences.preferDeDub,
                        ) { preferences = preferences.copy(preferDeDub = it) }
                        Toggles.ContainerSwitch(
                            "Prefer English dub", value = preferences.preferEnDub,
                        ) { preferences = preferences.copy(preferEnDub = it) }
                        Spacer(Modifier.height(3.dp))
                    }

                    Column(Modifier.settingsContainer()) {
                        Text("Performance / Quality", Modifier.padding(10.dp, 7.dp), style = MaterialTheme.typography.titleLarge)
                        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
                            Toggles.ContainerSwitch(
                                "Deband",
                                MpvDesc.deband,
                                value = preferences.deband,
                                paddingValues = Toggles.rowStartPadding,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            ) { preferences = preferences.copy(deband = it) }
                            Toggles.ContainerRadioSelect(
                                "Deband Iterations",
                                "Higher = better (& slower)",
                                value = preferences.debandIterations,
                                choices = debandIterationsChoices,
                                paddingValues = Toggles.rowEndPadding,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            ) { preferences = preferences.copy(debandIterations = it) }
                        }
                        Toggles.ContainerRadioSelect(
                            "Profile", MpvDesc.profileDescription, value = preferences.profile, choices = profileChoices,
                        ) { preferences = preferences.copy(profile = it) }

                        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
                            Toggles.ContainerSwitch(
                                "Hardware Decoding",
                                MpvDesc.hwDecoding,
                                value = preferences.hwDecoding,
                                paddingValues = Toggles.rowStartPadding,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            ) { preferences = preferences.copy(hwDecoding = it) }
                            Toggles.ContainerSwitch(
                                "Oversample Interpolate",
                                MpvDesc.oversample,
                                value = preferences.oversampleInterpol,
                                paddingValues = Toggles.rowEndPadding,
                                modifier = Modifier.weight(1f).fillMaxHeight()
                            ) { preferences = preferences.copy(oversampleInterpol = it) }
                        }

                        Toggles.ContainerRadioSelect(
                            "GPU-API", MpvDesc.gpuAPI, value = preferences.gpuAPI, choices = gpuApiChoices,
                        ) { preferences = preferences.copy(gpuAPI = it) }
                        Toggles.ContainerRadioSelect(
                            "Video Output Driver", MpvDesc.outputDriver, value = preferences.videoOutputDriver, choices = videoOutputDriverChoices,
                        ) { preferences = preferences.copy(videoOutputDriver = it) }
                        Toggles.ContainerSwitch(
                            "Force Downmix Algorithm", MpvDesc.downmix, value = preferences.customDownmix,
                        ) { preferences = preferences.copy(customDownmix = it) }
                        Toggles.ContainerSwitch(
                            "Force 10bit Dithering", MpvDesc.dither10bit, value = preferences.dither10bit,
                        ) { preferences = preferences.copy(dither10bit = it) }
                        Spacer(Modifier.height(3.dp))
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