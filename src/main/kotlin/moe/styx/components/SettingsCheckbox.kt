package moe.styx.moe.styx.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    onUpdate: (Boolean) -> Unit = {}
) {
    val setting = remember { mutableStateOf(settings[key, default]) }

    Row(modifier = Modifier.height(40.dp).padding(paddingValues)) {
        Text(text = title, modifier = Modifier.align(Alignment.CenterVertically))
        Checkbox(
            checked = setting.value, onCheckedChange = {
                settings.putBoolean(key, it)
                onUpdate(it)
                setting.value = it
            },
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}