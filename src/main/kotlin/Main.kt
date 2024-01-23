package moe.styx

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.takeFrom
import io.kamel.image.config.Default
import io.kamel.image.config.resourcesFetcher
import moe.styx.logic.data.DataManager
import moe.styx.logic.login.isLoggedIn
import moe.styx.moe.styx.logic.login.ServerStatus
import moe.styx.moe.styx.logic.requests.RequestQueue
import moe.styx.moe.styx.navigation.LocalGlobalNavigator
import moe.styx.moe.styx.views.login.OfflineView
import moe.styx.moe.styx.views.other.LoadingView
import moe.styx.theme.*
import moe.styx.views.login.LoginView

val settings: Settings = Settings()
var isUiModeDark: MutableState<Boolean> = mutableStateOf(true)
val dataManager = DataManager()
val requestQueue = RequestQueue()

val desktopConfig = KamelConfig {
    takeFrom(KamelConfig.Default)
    resourcesFetcher()
}

fun main() = application {
    requestQueue.start()
    isUiModeDark.value = settings["darkmode", true]
    val darkMode = remember { isUiModeDark }
    val nav = LocalNavigator.current

    val preferRounded = settings["rounded-corners", false]

    Window(
        onCloseRequest = ::exitApplication,
        title = "Styx 2",
        state = WindowState(width = 750.dp, height = 750.dp),
        undecorated = preferRounded,
        transparent = preferRounded,
        onKeyEvent = {
            if (nav != null && nav.canPop)
                nav.pop()

            true
        }
    )
    {
        Surface(modifier = Modifier.fillMaxSize()) {
            MaterialTheme(
                colorScheme = (if (darkMode.value) DarkColorScheme else LightColorScheme).transition(),
                typography = AppTypography,
                shapes = AppShapes
            ) {
                val view = if (isLoggedIn())
                    LoadingView()
                else {
                    if (ServerStatus.lastKnown != ServerStatus.ONLINE)
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