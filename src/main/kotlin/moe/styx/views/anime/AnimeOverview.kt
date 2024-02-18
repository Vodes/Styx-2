package moe.styx.views.anime

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.*
import moe.styx.Main
import moe.styx.Styx__.BuildConfig
import moe.styx.components.MainScaffold
import moe.styx.components.misc.IconButtonWithTooltip
import moe.styx.components.user.OnlineUsersIcon
import moe.styx.logic.data.DataManager
import moe.styx.navigation.*
import moe.styx.views.other.FontSizeView
import moe.styx.views.other.LoadingView
import moe.styx.views.settings.SettingsView

class AnimeOverview() : Screen {

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current

        MainScaffold(title = "${BuildConfig.APP_NAME} — Beta", addPopButton = false, actions = {
            OnlineUsersIcon()
            if (Main.wasLaunchedInDebug)
                IconButton(onClick = { nav.push(FontSizeView()) }, content = { Icon(Icons.Filled.QuestionMark, null) })
            IconButtonWithTooltip(Icons.Filled.Refresh, "Reload") {
                DataManager.updateLocalChange(0L, 0L)
                DataManager.isLoaded.value = false
                nav.replaceAll(LoadingView())
            }
            IconButtonWithTooltip(Icons.Filled.Settings, "Settings") { nav.push(SettingsView()) }
        }) {
            TabNavigator(defaultTab) {
                Scaffold(
                    bottomBar = {
                        NavigationBar(tonalElevation = 10.dp) {
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