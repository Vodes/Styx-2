package moe.styx.moe.styx.views.anime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import moe.styx.moe.styx.navigation.*
import moe.styx.views.settings.SettingsView

class AnimeOverview() : Screen {

    val tabNavigator: TabNavigator? = null

    @Composable
    override fun Content() {
        val scaffoldState = rememberScaffoldState()
        val nav = LocalGlobalNavigator.current

        Scaffold(scaffoldState = scaffoldState, topBar = {
            TopAppBar(
                title = { Text("Styx 2 — Beta") },
                backgroundColor = MaterialTheme.colors.primaryVariant,
                actions = {
                    IconButton(onClick = { nav.push(SettingsView()) }, content = { Icon(Icons.Filled.Settings, null) })
                }
            )
        }) {

            TabNavigator(defaultTab) {
                Scaffold(
                    bottomBar = {
                        BottomNavigation {
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

        BottomNavigationItem(
            selected = tabNavigator.current.key == tab.key,
            onClick = { tabNavigator.current = tab },
            icon = { Icon(painter = tab.options.icon!!, contentDescription = tab.options.title) },
            label = { Text(tab.options.title) },
            alwaysShowLabel = false
        )
    }
}