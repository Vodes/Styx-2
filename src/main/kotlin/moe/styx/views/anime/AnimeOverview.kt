package moe.styx.views.anime

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.*
import kotlinx.coroutines.runBlocking
import moe.styx.Main
import moe.styx.Styx__.BuildConfig
import moe.styx.common.compose.components.buttons.IconButtonWithTooltip
import moe.styx.common.compose.components.misc.OnlineUsersIcon
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.data.Changes
import moe.styx.components.MainScaffold
import moe.styx.logic.utils.pushMediaView
import moe.styx.views.*
import moe.styx.views.other.FontSizeView
import moe.styx.views.other.LoadingView

class AnimeOverview() : Screen {

    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current


        MainScaffold(title = "${BuildConfig.APP_NAME} — Beta", addPopButton = false, actions = {
            OnlineUsersIcon { nav.pushMediaView(it, false) }
            if (Main.wasLaunchedInDebug)
                IconButton(onClick = { nav.push(FontSizeView()) }, content = { Icon(Icons.Filled.QuestionMark, null) })
            IconButtonWithTooltip(Icons.Filled.Refresh, "Reload") {
                runBlocking { Storage.stores.changesStore.set(Changes(0, 0)) }
                nav.replaceAll(LoadingView())
            }
        }) {
            TabNavigator(defaultTab) {
                Row {
                    SideNavRail()
                    CurrentTab()
                }
            }
        }
    }

    @Composable
    private fun SideNavRail() {
        NavigationRail(Modifier.fillMaxHeight().padding(0.dp, 0.dp, 5.dp, 0.dp)) {
            RailNavItem(defaultTab)
            RailNavItem(movieTab)
            RailNavItem(favsTab)
            RailNavItem(scheduleTab)
            Spacer(Modifier.weight(1f))
            RailNavItem(
                settingsTab, colors = NavigationRailItemDefaults.colors(
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                    selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                    indicatorColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    }


    @Composable
    private fun RailNavItem(tab: Tab, colors: NavigationRailItemColors? = null) {
        val tabNavigator = LocalTabNavigator.current

        NavigationRailItem(
            selected = tabNavigator.current.key == tab.key,
            onClick = { tabNavigator.current = tab },
            icon = { Icon(painter = tab.options.icon!!, contentDescription = tab.options.title) },
            label = { Text(tab.options.title) },
            alwaysShowLabel = true,
            colors = colors
                ?: NavigationRailItemDefaults.colors(
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary
                )
        )
    }
}