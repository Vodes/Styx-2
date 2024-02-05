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
import moe.styx.Main.settings
import moe.styx.components.MainScaffold
import moe.styx.components.SettingsCheckbox
import moe.styx.logic.utils.*
import moe.styx.types.eqI
import moe.styx.types.json

class MpvConfigView : Screen {
    @Composable
    override fun Content() {
        var preferences by remember { mutableStateOf(MpvUtils.getPreferences()) }
        MainScaffold(title = "Mpv Configuration") {
            Column {
                Column(Modifier.padding(8.dp).fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                    Text("General", Modifier.padding(6.dp, 3.dp), style = MaterialTheme.typography.titleLarge)
                    SettingsCheckbox("Use system MPV", "mpv-system", !isWin(), paddingValues = PaddingValues(13.dp, 10.dp))
                    SettingsCheckbox("Use styx config with system mpv", "mpv-system-styx-conf", false, paddingValues = PaddingValues(13.dp, 10.dp))
                    SettingsCheckbox("Try to use flatpak (Linux only)", "mpv-flatpak", false, paddingValues = PaddingValues(13.dp, 10.dp))
                    SettingsCheckbox(
                        "Play next automatically",
                        "mpv-play-next",
                        true,
                        description = "Plays next episode (if any) when you reached the end and are paused/stopped.",
                        paddingValues = PaddingValues(13.dp, 10.dp)
                    )
                    Divider(Modifier.fillMaxWidth().padding(12.dp, 8.dp), thickness = 2.dp)
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
                    }
                    Divider(Modifier.fillMaxWidth().padding(12.dp, 8.dp), thickness = 2.dp)
                    Text("Language Preferences", style = MaterialTheme.typography.titleLarge)
                    Column(Modifier.padding(6.dp)) {
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
                Divider(Modifier.fillMaxWidth().padding(12.dp, 8.dp), thickness = 2.dp)
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


@Composable
fun StringChoices(
    title: String,
    choices: List<String>,
    description: String? = null,
    value: String? = null,
    onUpdate: (String) -> String
) {
    val value = value ?: choices[0]
    var selected by mutableStateOf(value)
    Column(Modifier.padding(10.dp, 8.dp)) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.Top) {
            for (choice in choices) {
                Row(Modifier.padding(8.dp, 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected eqI choice, onClick = { selected = onUpdate(choice) })
                    Text(choice, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        if (!description.isNullOrBlank())
            Text(description, Modifier.padding(6.dp, 2.dp), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun MpvCheckbox(title: String, value: Boolean, description: String? = null, enabled: Boolean = true, onUpdate: (Boolean) -> Unit = {}) {
    Column(Modifier.padding(10.dp, 5.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = value, enabled = enabled, onCheckedChange = { onUpdate(it) })
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        }
        if (!description.isNullOrBlank())
            Text(description, Modifier.padding(6.dp, 2.dp), style = MaterialTheme.typography.labelMedium)
    }
}