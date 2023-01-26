package moe.styx

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import moe.styx.logic.data.DataManager
import moe.styx.logic.login.isLoggedIn
import moe.styx.moe.styx.navigation.LocalGlobalNavigator
import moe.styx.moe.styx.views.other.LoadingView
import moe.styx.views.login.LoginView

val settings: Settings = Settings()
var isUiModeDark: MutableState<Boolean> = mutableStateOf(true)
val dataManager = DataManager()

@OptIn(ExperimentalAnimationApi::class)
fun main() = application {
    isUiModeDark.value = settings["darkmode", true]
    val darkMode = remember { isUiModeDark }
    val nav = LocalNavigator.current

    Window(
        onCloseRequest = ::exitApplication,
        title = "Styx 2",
        state = WindowState(width = 750.dp, height = 750.dp),
        onKeyEvent = {
            if (nav != null && nav.canPop)
                nav.pop()

            true
        }
    )
    {
        Surface(modifier = Modifier.fillMaxSize()) {
            MaterialTheme(
                colors = (if (darkMode.value) styxDarkColors() else styxLightColors()).switch(),
                typography = styxTypography
            ) {
                Navigator(if (isLoggedIn()) LoadingView() else LoginView()) { navigator ->
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