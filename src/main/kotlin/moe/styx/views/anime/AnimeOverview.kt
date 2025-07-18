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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberNavigatorScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.*
import com.dokar.sonner.TextToastAction
import com.dokar.sonner.Toast
import com.dokar.sonner.ToasterDefaults
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import moe.styx.Main
import moe.styx.Styx_2.BuildConfig
import moe.styx.common.compose.components.AppShapes
import moe.styx.common.compose.components.buttons.IconButtonWithTooltip
import moe.styx.common.compose.components.layout.MainScaffold
import moe.styx.common.compose.components.misc.OnlineUsersIcon
import moe.styx.common.compose.files.Storage
import moe.styx.common.compose.http.login
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.utils.LocalToaster
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.compose.viewmodels.MainDataViewModel
import moe.styx.common.data.Changes
import moe.styx.logic.utils.pushMediaView
import moe.styx.logic.viewmodels.DesktopOverViewModel
import moe.styx.views.*
import moe.styx.views.login.LoginView
import moe.styx.views.other.FontSizeView
import moe.styx.views.other.OutdatedView
import moe.styx.views.settings.AboutView

class AnimeOverview() : Screen {

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val toaster = LocalToaster.current
        val overviewSm = rememberScreenModel { DesktopOverViewModel() }

        val nav = LocalGlobalNavigator.current

        if (overviewSm.isOutdated == true) {
            nav.replaceAll(OutdatedView())
        }

        if (overviewSm.isLoggedIn == false && ServerStatus.lastKnown == ServerStatus.UNAUTHORIZED) {
            nav.replaceAll(LoginView())
        }

        LaunchedEffect(overviewSm.availablePreRelease) {
            val ver = overviewSm.availablePreRelease
            if (!ver.isNullOrBlank()) {
                toaster.show(
                    Toast(
                        "New Pre-Release version available: $ver",
                        action = TextToastAction("Download") {
                            nav.push(OutdatedView(ver))
                        },
                        duration = ToasterDefaults.DurationLong
                    )
                )
                overviewSm.availablePreRelease = null
            }
        }

        val sm = nav.rememberNavigatorScreenModel("main-vm") { MainDataViewModel() }
        val isLoading by sm.isLoadingStateFlow.collectAsState()
        val loadingState by sm.loadingStateFlow.collectAsState()

        MainScaffold(title = BuildConfig.APP_NAME, addPopButton = false, addAnimatedTitleBackground = true, titleClickable = {
            nav.push(AboutView())
        }, actions = {
            if (isLoading) {
                TooltipArea({ Text(loadingState) }, Modifier.fillMaxHeight(.9f), delayMillis = 200) {
                    Row(Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            trackColor = MaterialTheme.colorScheme.surfaceColorAtElevation(18.dp),
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
                IconButtonWithTooltip(Icons.Filled.NoAccounts, "You are not logged in!\nClick to retry.") {
                    overviewSm.screenModelScope.launch {
                        val loginJob = overviewSm.runLoginAndChecks()
                        loginJob.join()
                        if (login != null) {
                            sm.updateData(updateStores = true)
                        }
                    }
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
        NavigationRail(
            Modifier.fillMaxHeight().padding(7.dp, 6.dp, 3.dp, 8.dp).shadow(2.dp, AppShapes.large).clip(AppShapes.large),
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        ) {
            Spacer(Modifier.height(8.dp))
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
            Spacer(Modifier.height(5.dp))
        }
    }


    @Composable
    private fun RailNavItem(tab: Tab, colors: NavigationRailItemColors? = null) {
        val tabNavigator = LocalTabNavigator.current

        NavigationRailItem(
            selected = tabNavigator.current.key == tab.key,
            onClick = { tabNavigator.current = tab },
            icon = { Icon(painter = tab.options.icon!!, contentDescription = tab.options.title) },
            label = { Text(tab.options.title, modifier = Modifier.padding(3.dp, 1.dp)) },
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