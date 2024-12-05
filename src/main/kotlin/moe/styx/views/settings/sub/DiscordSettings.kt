package moe.styx.views.settings.sub

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import moe.styx.common.compose.components.misc.Toggles
import moe.styx.common.compose.settings
import moe.styx.logic.DiscordRPC

@Composable
fun DiscordSettings(modifier: Modifier = Modifier) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
        Toggles.ContainerSwitch(
            "Enable RPC",
            modifier = Modifier.weight(1f).fillMaxHeight(),
            value = settings["discord-rpc", true],
            paddingValues = Toggles.rowStartPadding
        ) {
            settings["discord-rpc"] = it
            if (it && !DiscordRPC.isStarted())
                DiscordRPC.start()
            else if (!it && DiscordRPC.isStarted()) {
                DiscordRPC.clearActivity()
            }
        }

        Toggles.ContainerSwitch(
            "Show RPC when idle",
            "Disabling this means the discord status will only show while you're watching something.",
            value = settings["discord-rpc-idle", true], modifier = Modifier.weight(1f), paddingValues = Toggles.rowEndPadding
        ) { settings["discord-rpc-idle"] = it }
    }
    Spacer(Modifier.height(5.dp))
}