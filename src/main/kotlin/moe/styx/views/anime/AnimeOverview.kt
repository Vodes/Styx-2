package moe.styx.moe.styx.views.anime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.*
import moe.styx.moe.styx.navigation.*
import moe.styx.moe.styx.views.other.FontSizeView
import moe.styx.views.settings.SettingsView

class AnimeOverview() : Screen {

    val tabNavigator: TabNavigator? = null

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current

        Scaffold(topBar = {
            TopAppBar(
                title = { Text("Styx 2 — Beta") },
                actions = {
                    IconButton(onClick = { nav.push(SettingsView()) }, content = { Icon(Icons.Filled.Settings, null) })
                    IconButton(onClick = { nav.push(FontSizeView()) }, content = { Icon(Icons.Filled.QuestionMark, null) })
                }
            )
        }) { values ->
            TabNavigator(defaultTab) {
                Scaffold(
                    modifier = Modifier.padding(values),
                    bottomBar = {
                        NavigationBar(tonalElevation = 5.dp) {
                            TabNavigationItem(defaultTab)
                            TabNavigationItem(movieTab)
                            TabNavigationItem(favsTab)
                            TabNavigationItem(scheduleTab)
                        }
                    }
                ) {
                    Column(modifier = Modifier.padding(it)) { CurrentTab() }
                }
            }
        }
    }

    @Composable
    private fun RowScope.TabNavigationItem(tab: Tab) {
        val tabNavigator = LocalTabNavigator.current

        NavigationBarItem(
            selected = tabNavigator.current.key == tab.key,
            onClick = { tabNavigator.current = tab },
            icon = { Icon(painter = tab.options.icon!!, contentDescription = tab.options.title) },
            label = { Text(tab.options.title) },
            alwaysShowLabel = false,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                indicatorColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}