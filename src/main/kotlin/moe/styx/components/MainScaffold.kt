package moe.styx.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import moe.styx.common.compose.components.buttons.PopButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    modifier: Modifier = Modifier,
    title: String,
    addPopButton: Boolean = true,
    actions: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Scaffold(modifier = modifier, topBar = {
        TopAppBar(
            title = { Text(title) },
            actions = {
                actions()
                if (addPopButton)
                    PopButton()
            }
        )
    }) {
        Box(Modifier.fillMaxSize().padding(it)) {
            content()
        }
    }
}