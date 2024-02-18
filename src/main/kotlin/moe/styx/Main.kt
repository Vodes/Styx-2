package moe.styx

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import moe.styx.Main.isUiModeDark
import moe.styx.Main.settings
import moe.styx.Styx__.BuildConfig
import moe.styx.common.http.getHttpClient
import moe.styx.logic.DiscordRPC
import moe.styx.logic.Endpoints
import moe.styx.logic.login.ServerStatus
import moe.styx.logic.login.isLoggedIn
import moe.styx.logic.login.login
import moe.styx.logic.loops.Heartbeats
import moe.styx.logic.loops.RequestQueue
import moe.styx.logic.sendObject
import moe.styx.logic.utils.Log
import moe.styx.logic.utils.setupLogFile
import moe.styx.navigation.LocalGlobalNavigator
import moe.styx.theme.*
import moe.styx.views.login.LoginView
import moe.styx.views.login.OfflineView
import moe.styx.views.other.LoadingView
import java.util.prefs.Preferences

object Main {
    private val delegate = Preferences.userNodeForPackage(this.javaClass)
    val settings: Settings = PreferencesSettings(delegate)
    var isUiModeDark: MutableState<Boolean> = mutableStateOf(true)
    var wasLaunchedInDebug = false
}

fun main(args: Array<String>) = application {
    if (!args.contains("-debug"))
        setupLogFile()
    else
        Main.wasLaunchedInDebug = true
    getHttpClient("${BuildConfig.APP_NAME} - ${BuildConfig.APP_VERSION}")
    if (settings["discord-rpc", true]) {
        DiscordRPC.start()
    }
    RequestQueue.start()
    Heartbeats.start()

    isUiModeDark.value = settings["darkmode", true]
    val darkMode by remember { isUiModeDark }

    Window(
        onCloseRequest = { onClose() },
        title = "${BuildConfig.APP_NAME} - ${BuildConfig.APP_VERSION}",
        state = WindowState(width = 750.dp, height = 750.dp),
        icon = painterResource("icons/icon.ico")
    )
    {
        Log.i { "Compose window initialized with: ${this.window.renderApi}" }
        Log.i { "Starting ${BuildConfig.APP_NAME} v${BuildConfig.APP_VERSION}" }
        Surface(modifier = Modifier.fillMaxSize()) {
            MaterialTheme(
                colorScheme = (if (darkMode) DarkColorScheme else LightColorScheme).transition(),
                typography = AppTypography,
                shapes = AppShapes
            ) {
                val view = if (isLoggedIn()) {
                    Log.i { "Logged in as: ${login?.name}" }
                    LoadingView()
                } else {
                    if (ServerStatus.lastKnown !in listOf(ServerStatus.ONLINE, ServerStatus.UNAUTHORIZED))
                        OfflineView()
                    else
                        LoginView()
                }
                Navigator(view) { navigator ->
                    CompositionLocalProvider(LocalGlobalNavigator provides navigator) {
                        SlideTransition(
                            navigator, animationSpec = spring(
                                stiffness = Spring.StiffnessMedium,
                                visibilityThreshold = IntOffset.VisibilityThreshold
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun ApplicationScope.onClose() {
    runCatching { sendObject(Endpoints.LOGOUT, "") }
    exitApplication()
}