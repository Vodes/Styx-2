package moe.styx.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.get
import moe.styx.settings

@Composable
fun SettingsCheckbox(
    title: String,
    key: String,
    default: Boolean,
    paddingValues: PaddingValues = PaddingValues(10.dp),
    description: String = "",
    onUpdate: (Boolean) -> Unit = {}
) {
    var setting by rememberSaveable { mutableStateOf(settings[key, default]) }

    if (description.isBlank())
        Row(modifier = Modifier.height(40.dp).padding(paddingValues)) {
            TextWithCheckBox(title, setting, Modifier.align(Alignment.CenterVertically)) { updated ->
                setting = updated.also { settings.putBoolean(key, updated) }.also { onUpdate(updated) }
            }
        }
    else
        Column(Modifier.height(90.dp).padding(paddingValues)) {
            Row {
                TextWithCheckBox(title, setting, Modifier.align(Alignment.CenterVertically)) { updated ->
                    setting = updated.also { settings.putBoolean(key, updated) }.also { onUpdate(updated) }
                }
            }
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
}

@Composable
private fun TextWithCheckBox(title: String, value: Boolean, modifier: Modifier = Modifier, onUpdate: (Boolean) -> Unit) {
    Text(text = title, modifier = modifier)
    Checkbox(
        checked = value, onCheckedChange = {
            onUpdate(it)
        },
        modifier = modifier
    )
}