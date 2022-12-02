// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.lifecycle.LifecycleController
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import views.anime.animeListView
import views.anime.animeView

@OptIn(ExperimentalDecomposeApi::class)
fun main() = application {
    val lifecycle = LifecycleRegistry()
    val rootComponentContext = DefaultComponentContext(lifecycle = lifecycle)
    val windowState = rememberWindowState()

    LifecycleController(lifecycle, windowState)
    Window(
        onCloseRequest = ::exitApplication,
        title="Styx 2",
        state = WindowState(width = 740.dp, height=600.dp)
    )
    {
        Surface(modifier = Modifier.fillMaxSize()) {
            MaterialTheme (colors = styxDarkColors(), typography = styxTypography) {
                CompositionLocalProvider(LocalScrollbarStyle provides defaultScrollbarStyle()) {
                    ProvideComponentContext(rootComponentContext) {
                        MainContent()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun MainContent() {
    val navigation = remember { StackNavigation<Screen>() }

    ChildStack(
        source = navigation,
        initialStack = { listOf(Screen.AnimeList) },
        animation = stackAnimation(fade()),
    ) { screen ->
        when (screen) {
            is Screen.AnimeList -> animeListView(onAnimeClick = { navigation.push(Screen.AnimeView(id = it)) })
            is Screen.AnimeView -> animeView(id = screen.id, onBack = { navigation.pop() })
        }
    }
}

sealed class Screen : Parcelable {

    @Parcelize
    object AnimeList : Screen()

    @Parcelize
    data class AnimeView(val id: String) : Screen()
}
