// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import logic.login.isLoggedIn
import views.anime.AnimeListView
import views.login.LoginView

val settings: Settings = Settings()
var isUiModeDark: MutableState<Boolean> = mutableStateOf(true)

fun main() = application {
    isUiModeDark.value = settings["darkmode", true]
    val darkMode = remember { isUiModeDark }
    val nav = LocalNavigator.current

    Window(
        onCloseRequest = ::exitApplication,
        title = "Styx 2",
        state = WindowState(width = 740.dp, height = 600.dp),
        onKeyEvent = {
            println("keke")
            if (nav != null && nav.canPop)
                nav.pop()

            true
        }
    )
    {
        Surface(modifier = Modifier.fillMaxSize()) {
            MaterialTheme(
                colors = if (darkMode.value) styxDarkColors() else styxLightColors(),
                typography = styxTypography
            ) {
                Navigator(if (isLoggedIn()) AnimeListView() else LoginView())
            }
        }
    }
}