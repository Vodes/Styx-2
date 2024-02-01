package moe.styx.components.misc

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.russhwolf.settings.get
import moe.styx.Main.settings

@Composable
fun TwoStateIconButton(
    key: String,
    default: Boolean,
    iconTrue: ImageVector,
    iconFalse: ImageVector,
    onChange: (Boolean) -> Unit = {},
    trueTooltip: String? = null,
    falseTooltip: String? = null
) {
    var setting by remember { mutableStateOf(settings[key, default]) }
    IconButton(
        {
            setting = !setting
            settings.putBoolean(key, setting)
            onChange(setting)
        },
        content = {
            if (setting) {
                if (!trueTooltip.isNullOrBlank()) {
                    ToolTipWrapper(trueTooltip) {
                        Icon(if (setting) iconTrue else iconFalse, "")
                    }
                } else
                    Icon(if (setting) iconTrue else iconFalse, "")
            } else {
                if (!falseTooltip.isNullOrBlank()) {
                    ToolTipWrapper(falseTooltip) {
                        Icon(if (setting) iconTrue else iconFalse, "")
                    }
                } else
                    Icon(if (setting) iconTrue else iconFalse, "")
            }
        }
    )
}