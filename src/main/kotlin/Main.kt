package moe.styx

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
import moe.styx.moe.styx.navigation.LocalGlobalNavigator
import moe.styx.moe.styx.views.login.OfflineView
import moe.styx.moe.styx.views.other.LoadingView
import moe.styx.views.login.LoginView

val settings: Settings = Settings()
var isUiModeDark: MutableState<Boolean> = mutableStateOf(true)
val dataManager = DataManager()

val desktopConfig = KamelConfig {
    takeFrom(KamelConfig.Default)
    resourcesFetcher()
}

@OptIn(ExperimentalAnimationApi::class)
fun main() = application {
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
        StyxSurface(preferRounded) {
            MaterialTheme(
                colors = (if (darkMode.value) styxDarkColors() else styxLightColors()).switch(),
                typography = styxTypography
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
                                stiffness = Spring.StiffnessHigh,
                                visibilityThreshold = IntOffset.VisibilityThreshold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StyxSurface(rounded: Boolean = false, content: @Composable () -> Unit) {
    if (rounded) {
        Surface(
            modifier = Modifier.fillMaxSize().padding(2.dp).shadow(4.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp)
        ) {
            content()
        }
    } else {
        Surface(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}