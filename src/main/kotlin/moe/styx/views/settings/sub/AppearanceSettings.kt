package moe.styx.views.settings.sub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import moe.styx.Main.densityScale
import moe.styx.Main.isUiModeDark
import moe.styx.Main.useMonoFont
import moe.styx.common.compose.components.AppShapes
import moe.styx.common.compose.components.buttons.IconButtonWithTooltip
import moe.styx.common.compose.components.misc.Toggles
import moe.styx.common.compose.settings

@Composable
fun AppearanceSettings() {
    var darkMode by remember { isUiModeDark }
    var monoFont by remember { useMonoFont }

    Text(
        "Note that these may not fully apply until you go in and out of an anime screen.\nThis is a bug in a 3rd party library and will be fixed in a later update.",
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(10.dp, 5.dp)
    )

    Row(Modifier.fillMaxWidth()) {
        Toggles.ContainerSwitch(
            "Darkmode",
            modifier = Modifier.weight(1f),
            value = darkMode,
            paddingValues = Toggles.rowStartPadding
        ) { darkMode = it.also { settings["darkmode"] = it } }

        Toggles.ContainerSwitch(
            "Mono Font",
            modifier = Modifier.weight(1f),
            value = monoFont,
            paddingValues = Toggles.rowEndPadding
        ) { monoFont = it.also { settings["mono-font"] = it } }
    }
    WindowScaleSetting()
    Toggles.ContainerSwitch("Show names by default", value = settings["display-names", false]) { settings["display-names"] = it }
    Spacer(Modifier.height(5.dp))
}

@Composable
fun WindowScaleSetting(modifier: Modifier = Modifier) {
    var densityScale by remember { densityScale }
    Row(
        modifier.padding(8.dp, 4.dp).clip(AppShapes.large).background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.padding(10.dp).weight(1f), horizontalAlignment = Alignment.Start) {
            Text("Window Scale: $densityScale", style = MaterialTheme.typography.bodyLarge)
        }
        Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            IconButtonWithTooltip(Icons.Outlined.Remove, "Decrease Scale") {
                val newScale = densityScale - 0.25f
                settings["density-scale"] = newScale
                densityScale = newScale
            }
            IconButtonWithTooltip(Icons.Outlined.Add, "Increase Scale") {
                val newScale = densityScale + 0.25f
                settings["density-scale"] = newScale
                densityScale = newScale
            }
        }
    }
}

@Composable
fun MetadataSettings() {
    Toggles.ContainerSwitch(
        "Show episode summaries",
        value = settings["display-ep-synopsis", false]
    ) { settings["display-ep-synopsis"] = it }
    Toggles.ContainerSwitch(
        "Prefer german titles and summaries",
        value = settings["prefer-german-metadata", false]
    ) { settings["prefer-german-metadata"] = it }
    Row(Modifier.fillMaxWidth()) {
        Toggles.ContainerSwitch(
            "Use list for shows",
            modifier = Modifier.weight(1f),
            value = settings["shows-list", false],
            paddingValues = Toggles.rowStartPadding
        ) { settings["shows-list"] = it }
        Toggles.ContainerSwitch(
            "Use list for movies",
            modifier = Modifier.weight(1f),
            value = settings["movies-list", false],
            paddingValues = Toggles.rowEndPadding
        ) { settings["movies-list"] = it }
    }
    Toggles.ContainerSwitch(
        "Sort episodes ascendingly",
        value = settings["episode-asc", false],
        paddingValues = Toggles.colEndPadding
    ) { settings["episode-asc"] = it }
    Spacer(Modifier.height(5.dp))
}