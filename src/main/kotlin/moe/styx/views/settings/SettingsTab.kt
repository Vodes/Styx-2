package moe.styx.views.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import com.dokar.sonner.Toast
import com.dokar.sonner.ToastType
import com.dokar.sonner.ToasterDefaults
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import moe.styx.common.compose.components.layout.MainScaffold
import moe.styx.common.compose.components.misc.ExpandableSettings
import moe.styx.common.compose.components.misc.ServerSelection
import moe.styx.common.compose.components.misc.Toggles
import moe.styx.common.compose.components.misc.Toggles.settingsContainer
import moe.styx.common.compose.extensions.SimpleTab
import moe.styx.common.compose.http.login
import moe.styx.common.compose.settings
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.utils.LocalToaster
import moe.styx.common.isWindows
import moe.styx.common.util.Log
import moe.styx.components.misc.MpvVersionAndDownload
import moe.styx.logic.Files
import moe.styx.logic.runner.openURI
import moe.styx.views.settings.sub.*
import java.awt.Desktop
import java.io.File

class SettingsTab : SimpleTab("Settings", Icons.Default.Settings) {

    @Composable
    override fun Content() {
        SettingsViewComponent()
    }
}

class SettingsView : Screen {

    @Composable
    override fun Content() {
        MainScaffold(Modifier.fillMaxSize(), "Settings") {
            SettingsViewComponent()
        }
    }
}

class SettingsViewModel : ScreenModel {
    var appearanceExpanded by mutableStateOf(true)
    var metadataExpanded by mutableStateOf(true)
    var discordExpanded by mutableStateOf(false)
    var trackingExpanded by mutableStateOf(false)
    var systemExpanded by mutableStateOf(false)
}

@Composable
fun SettingsViewComponent() {
    val nav = LocalGlobalNavigator.current
    val vm = nav.rememberNavigatorScreenModel("settings-vm") { SettingsViewModel() }
    val scrollState = rememberScrollState(0)
    val toaster = LocalToaster.current
    Column(Modifier.fillMaxSize().padding(5.dp)) {
        Column(Modifier.padding(5.dp).weight(1F).verticalScroll(scrollState, true)) {
            ExpandableSettings(
                "Appearance Options",
                vm.appearanceExpanded,
                { vm.appearanceExpanded = !vm.appearanceExpanded },
                withContainer = false
            ) {
                AppearanceSettings()
            }
            ExpandableSettings("Metadata Options", vm.metadataExpanded, { vm.metadataExpanded = !vm.metadataExpanded }, withContainer = false) {
                MetadataSettings()
            }
            ExpandableSettings("Discord", vm.discordExpanded, { vm.discordExpanded = !vm.discordExpanded }, withContainer = false) {
                DiscordSettings()
            }
            ExpandableSettings("Tracking", vm.trackingExpanded, { vm.trackingExpanded = !vm.trackingExpanded }, withContainer = false) {
                TrackingSettings()
            }
            ExpandableSettings("System", vm.systemExpanded, { vm.systemExpanded = !vm.systemExpanded }, withContainer = false) {
                ServerSelection()
                Toggles.ContainerSwitch(
                    "Enable Debug Logs",
                    description = "Please enable this and try to reproduce your issue if you want to report a bug to me!",
                    value = settings["enable-debug-logs", false]
                ) {
                    settings["enable-debug-logs"] = it
                    Log.debugEnabled = it
                }
                Button({
                    val logFolder = File(Files.getAppDir(), "Logs")
                    if (!isWindows) {
                        openURI(logFolder.absolutePath)
                        return@Button
                    }
                    runCatching {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(logFolder)
                        }
                        return@Button
                    }.onFailure {
                        Log.e(exception = it)
                    }.getOrNull()
                    toaster.show(
                        Toast(
                            "Could not open log folder!\nPlease check it yourself at: ${logFolder.absolutePath}",
                            type = ToastType.Error,
                            duration = ToasterDefaults.DurationLong
                        )
                    )
                }, Modifier.padding(8.dp, 5.dp)) {
                    Text("Open Log Folder")
                }
                Spacer(Modifier.height(5.dp))
            }
            Column(Modifier.settingsContainer()) {
                Text("MPV Options", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(10.dp, 7.dp))
                Button({ nav.push(MpvConfigView()) }, Modifier.padding(8.dp, 4.dp)) {
                    Text("Open Mpv Configuration")
                }
                MpvVersionAndDownload()
            }
        }
        HorizontalDivider(Modifier.padding(5.dp), thickness = 2.dp)
        LoggedInComponent(nav)
    }
}


@Composable
fun LoggedInComponent(nav: Navigator) {
    val primaryColor = MaterialTheme.colorScheme.primary
    if (login != null) {
        Text(
            "Logged in as: ${login!!.name}",
            Modifier.padding(10.dp)
        )
    }
}