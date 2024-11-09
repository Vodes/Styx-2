package moe.styx.views.anime

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.lifecycle.LifecycleEffectOnce
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.*
import kotlinx.coroutines.runBlocking
import moe.styx.Main
import moe.styx.Styx_2.BuildConfig
import moe.styx.common.compose.components.buttons.IconButtonWithTooltip
import moe.styx.common.compose.components.layout.MainScaffold
import moe.styx.common.compose.components.misc.OnlineUsersIcon
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.data.Changes
import moe.styx.logic.utils.pushMediaView
import moe.styx.views.*
import moe.styx.views.data.MainDataViewModel
import moe.styx.views.data.OverviewViewModel
import moe.styx.views.login.LoginView
import moe.styx.views.other.FontSizeView
import moe.styx.views.other.OutdatedView

class AnimeOverview() : Screen {

    @OptIn(ExperimentalFoundationApi::class, ExperimentalVoyagerApi::class)
    @Composable
    override fun Content() {
        val overviewSm = rememberScreenModel { OverviewViewModel() }
        LifecycleEffectOnce {
            overviewSm.runChecks()
        }

        val nav = LocalGlobalNavigator.current

        if (overviewSm.isOutdated == true) {
            nav.replaceAll(OutdatedView())
        }
        
        LaunchedEffect(overviewSm.isLoggedIn) {
            if (overviewSm.isLoggedIn == false && ServerStatus.lastKnown == ServerStatus.UNAUTHORIZED) {
                nav.replaceAll(LoginView())
            }
        }

        val sm = nav.rememberNavigatorScreenModel("main-vm") { MainDataViewModel() }
        val isLoading by sm.isLoadingStateFlow.collectAsState()
        val loadingState by sm.loadingStateFlow.collectAsState()

        MainScaffold(title = BuildConfig.APP_NAME, addPopButton = false, actions = {
            if (isLoading) {
                TooltipArea({ Text(loadingState) }, Modifier.fillMaxHeight(.9f), delayMillis = 200) {
                    Row(Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            trackColor = MaterialTheme.colorScheme.onSurface,
                            gapSize = 0.dp,
                            modifier = Modifier.requiredWidthIn(20.dp, 40.dp)
                        )
                    }
                }
            }
            if (overviewSm.isOffline == true) {
                IconButtonWithTooltip(Icons.Filled.CloudOff, ServerStatus.getLastKnownText()) {}
            }

            if (overviewSm.isLoggedIn == false) {
                IconButtonWithTooltip(Icons.Filled.NoAccounts, "You are not logged in!") {
                    overviewSm.runChecks()
                }
            }

            OnlineUsersIcon { nav.pushMediaView(it, false) }
            if (Main.wasLaunchedInDebug)
                IconButton(onClick = { nav.push(FontSizeView()) }, content = { Icon(Icons.Filled.QuestionMark, null) })
            IconButtonWithTooltip(Icons.Filled.Refresh, "Reload") {
                runBlocking { Storage.stores.changesStore.set(Changes(0, 0)) }
                sm.updateData(forceUpdate = true, updateStores = true)
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