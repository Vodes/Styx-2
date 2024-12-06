package moe.styx.views.other

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import moe.styx.Main
import moe.styx.common.compose.components.layout.MainScaffold

class FontSizeView : Screen {

    @Composable
    override fun Content() {
        MainScaffold(title = "Font Sizes") {
            Column {
                Row {
                    Button({
                        Main.densityScale.value -= 0.25f
                    }) {
                        Text("Down the scale")
                    }
                    Button({
                        Main.densityScale.value += 0.25f
                    }) {
                        Text("Up the scale")
                    }
                }
                Text("Display Large", style = MaterialTheme.typography.displayLarge)
                Text("Display Medium", style = MaterialTheme.typography.displayMedium)
                Text("Display Small", style = MaterialTheme.typography.displaySmall)
                Text("Headline Large", style = MaterialTheme.typography.headlineLarge)
                Text("Headline Medium", style = MaterialTheme.typography.headlineMedium)
                Text("Headline Small", style = MaterialTheme.typography.headlineSmall)
                Text("Title Large", style = MaterialTheme.typography.titleLarge)
                Text("Title Medium", style = MaterialTheme.typography.titleMedium)
                Text("Title Small", style = MaterialTheme.typography.titleSmall)
                Text("Body Large", style = MaterialTheme.typography.bodyLarge)
                Text("Body Medium", style = MaterialTheme.typography.bodyMedium)
                Text("Body Small", style = MaterialTheme.typography.bodySmall)
                Text("Label Large", style = MaterialTheme.typography.labelLarge)
                Text("Label Medium", style = MaterialTheme.typography.labelMedium)
                Text("Label Small", style = MaterialTheme.typography.labelSmall)
                HorizontalDivider()
                LinearProgressIndicator(
                    { .5f },
                    Modifier.padding(5.dp).fillMaxWidth().height(5.dp),
                    trackColor = MaterialTheme.colorScheme.onSurface
                )
                LinearProgressIndicator(
                    { .5f },
                    Modifier.padding(5.dp).fillMaxWidth().height(5.dp),
                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    { .5f },
                    Modifier.padding(5.dp).fillMaxWidth().height(5.dp),
                    trackColor = MaterialTheme.colorScheme.inverseOnSurface
                )
                LinearProgressIndicator(
                    { .5f },
                    Modifier.padding(5.dp).fillMaxWidth().height(5.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                )
                LinearProgressIndicator(
                    { .5f },
                    Modifier.padding(5.dp).fillMaxWidth().height(5.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
                LinearProgressIndicator(
                    { .5f },
                    Modifier.padding(5.dp).fillMaxWidth().height(5.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(5.dp)
                )
                LinearProgressIndicator(
                    { .5f },
                    Modifier.padding(5.dp).fillMaxWidth().height(5.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(15.dp)
                )
            }
        }
    }
}