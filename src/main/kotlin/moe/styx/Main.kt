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
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import moe.styx.Main.isUiModeDark
import moe.styx.Main.settings
import moe.styx.logic.login.ServerStatus
import moe.styx.logic.login.isLoggedIn
import moe.styx.logic.login.login
import moe.styx.logic.loops.Heartbeats
import moe.styx.logic.loops.RequestQueue
import moe.styx.logic.utils.Log
import moe.styx.logic.utils.MpvUtils
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
}

fun main() = application {
    RequestQueue.start()
    Heartbeats.start()
    isUiModeDark.value = settings["darkmode", true]
    val darkMode by remember { isUiModeDark }
    val nav = LocalNavigator.current

    val preferRounded = settings["rounded-corners", false]

    Window(
        onCloseRequest = ::exitApplication,
        title = "Styx 2",
        state = WindowState(width = 750.dp, height = 750.dp),
        undecorated = preferRounded,
        transparent = preferRounded,
        icon = painterResource("icons/icon.ico"),
        onKeyEvent = {
            if (nav != null && nav.canPop)
                nav.pop()

            true
        }
    )
    {
        Log.i { "Compose window initialized with: ${this.window.renderApi}" }
        Surface(modifier = Modifier.fillMaxSize()) {
            MaterialTheme(
                colorScheme = (if (darkMode) DarkColorScheme else LightColorScheme).transition(),
                typography = AppTypography,
                shapes = AppShapes
            ) {
                val view = if (isLoggedIn()) {
                    Log.i { "Logged in as: ${login?.name}" }
                    MpvUtils.checkVersionAndDownload()
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