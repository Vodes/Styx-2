package moe.styx.moe.styx.components.misc

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import com.russhwolf.settings.get
import moe.styx.settings

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
    val setting = remember { mutableStateOf(settings[key, default]) }
    IconButton(
        {
            setting.value = !setting.value
            settings.putBoolean(key, setting.value)
            onChange(setting.value)
        },
        content = {
            if (setting.value) {
                if (!trueTooltip.isNullOrBlank()) {
                    ToolTipWrapper(trueTooltip) {
                        Icon(if (setting.value) iconTrue else iconFalse, "")
                    }
                } else
                    Icon(if (setting.value) iconTrue else iconFalse, "")
            } else {
                if (!falseTooltip.isNullOrBlank()) {
                    ToolTipWrapper(falseTooltip) {
                        Icon(if (setting.value) iconTrue else iconFalse, "")
                    }
                } else
                    Icon(if (setting.value) iconTrue else iconFalse, "")
            }
        }
    )
}