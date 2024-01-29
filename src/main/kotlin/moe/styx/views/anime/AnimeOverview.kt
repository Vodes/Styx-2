package moe.styx.views.anime

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.*
import kotlinx.coroutines.delay
import moe.styx.logic.data.DataManager
import moe.styx.logic.login.login
import moe.styx.logic.loops.Heartbeats
import moe.styx.navigation.*
import moe.styx.types.ActiveUser
import moe.styx.types.eqI
import moe.styx.types.toBoolean
import moe.styx.views.other.FontSizeView
import moe.styx.views.settings.SettingsView

class AnimeOverview() : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current
        var users by remember { mutableStateOf(Heartbeats.currentUsers) }
        var numUsers by remember { mutableStateOf(0) }

        LaunchedEffect(Unit) {
            while (login != null) {
                users = Heartbeats.currentUsers
                numUsers = users.size
                delay(2000)
            }
        }
        var showUserDropDown by mutableStateOf(false)
        Scaffold(topBar = {
            TopAppBar(
                title = { Text("Styx 2 — Beta") },
                actions = {
                    UsersIconWithNum(numUsers) { showUserDropDown = if (numUsers > 0) !showUserDropDown else false }
                    IconButton(onClick = { nav.push(FontSizeView()) }, content = { Icon(Icons.Filled.QuestionMark, null) })
                    IconButton(onClick = { nav.push(SettingsView()) }, content = { Icon(Icons.Filled.Settings, "Settings") })
                    DropdownMenu(showUserDropDown, { showUserDropDown = false }, Modifier.defaultMinSize(260.dp, 0.dp)) {
                        Text("Online Users", Modifier.padding(7.dp, 10.dp), style = MaterialTheme.typography.titleLarge)
                        Divider(Modifier.fillMaxWidth().padding(10.dp, 0.dp, 10.dp, 8.dp), thickness = 3.dp)
                        UserListComponent(nav, users)
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersIconWithNum(num: Int, onClick: () -> Unit) {
    BadgedBox(badge = {
        Badge(Modifier.size(20.dp).offset((-10).dp, 12.dp), MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary) {
            Text("$num")
        }
    }) {
        IconButton(onClick = onClick, content = { Icon(Icons.Filled.Group, "Online userlist") })
    }
}

@Composable
fun UserListComponent(nav: Navigator, userList: List<ActiveUser>) {
    userList.forEachIndexed { index, user ->
        if (index != 0)
            Divider(Modifier.fillMaxWidth().padding(10.dp, 10.dp), thickness = 1.dp)
        Row(Modifier.padding(10.dp, 0.dp, 0.dp, 5.dp), verticalAlignment = Alignment.CenterVertically) {
            when (user.deviceType) {
                "PC" -> Icon(Icons.Filled.Computer, "PC")
                "Laptop" -> Icon(Icons.Filled.LaptopWindows, "Laptop")
                "Phone" -> Icon(Icons.Filled.PhoneAndroid, "Phone")
                else -> Icon(Icons.Filled.DeviceUnknown, "Unknown")
            }
            Column(Modifier.padding(10.dp, 0.dp)) {
                Text(user.user.name, Modifier.padding(3.dp, 0.dp, 0.dp, 0.dp), style = MaterialTheme.typography.titleMedium)
                if (user.mediaActivity != null) {
                    val entry = DataManager.entries.value.find { it.GUID eqI user.mediaActivity?.mediaEntry }
                    val parentMedia = DataManager.media.value.find { it.GUID eqI entry?.mediaID }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (user.mediaActivity!!.playing)
                            Icon(Icons.Filled.PlayArrow, "Playing", Modifier.size(30.dp).padding(0.dp, 0.dp, 10.dp, 0.dp))
                        else
                            Icon(Icons.Filled.Pause, "Paused", Modifier.size(30.dp).padding(0.dp, 0.dp, 10.dp, 0.dp))
                        if (entry == null || parentMedia == null)
                            Text("Unknown", style = MaterialTheme.typography.bodyMedium)
                        else
                            Text(
                                "${parentMedia.name}${if (parentMedia.isSeries.toBoolean()) " - ${entry.entryNumber}" else ""}",
                                Modifier.clickable {
                                    nav.push(AnimeDetailView(parentMedia.GUID))
                                }, style = MaterialTheme.typography.bodyMedium
                            )
                    }
                }
            }
        }
    }
}