package moe.styx.moe.styx.components

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun MainScaffold(
    scaffoldState: ScaffoldState,
    modifier: Modifier = Modifier,
    title: String,
    color: Color = MaterialTheme.colors.primaryVariant,
    addPopButton: Boolean = true,
    actions: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Scaffold(scaffoldState = scaffoldState, modifier = modifier, topBar = {
        TopAppBar(
            title = { Text(title) },
            backgroundColor = color,
            actions = {
                actions()
                if (addPopButton)
                    PopButton()
            }
        )
    }) {
        content()
    }
}