package moe.styx.moe.styx.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.get
import moe.styx.settings

@OptIn(ExperimentalFoundationApi::class)
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
                    TooltipArea(delayMillis = 250, tooltip = {
                        Card(
                            backgroundColor = MaterialTheme.colors.surface,
                            contentColor = MaterialTheme.colors.onSurface
                        ) {
                            Text(trueTooltip, modifier = Modifier.padding(6.dp))
                        }
                    }, content = { Icon(if (setting.value) iconTrue else iconFalse, "") })
                } else
                    Icon(if (setting.value) iconTrue else iconFalse, "")
            } else {
                if (!falseTooltip.isNullOrBlank()) {
                    TooltipArea(delayMillis = 250, tooltip = {
                        Card(
                            backgroundColor = MaterialTheme.colors.surface,
                            contentColor = MaterialTheme.colors.onSurface
                        ) {
                            Text(falseTooltip, modifier = Modifier.padding(6.dp))
                        }
                    }, content = { Icon(if (setting.value) iconTrue else iconFalse, "") })
                } else
                    Icon(if (setting.value) iconTrue else iconFalse, "")
            }
        }
    )
}