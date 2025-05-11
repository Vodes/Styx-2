package moe.styx

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.dokar.sonner.ToastWidthPolicy
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import io.kamel.image.config.LocalKamelConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import moe.styx.Styx_2.BuildConfig
import moe.styx.common.compose.AppConfig
import moe.styx.common.compose.AppContextImpl.appConfig
import moe.styx.common.compose.components.*
import moe.styx.common.compose.extensions.kamelConfig
import moe.styx.common.compose.http.Endpoints
import moe.styx.common.compose.http.sendObject
import moe.styx.common.compose.settings
import moe.styx.common.compose.threads.DownloadQueue
import moe.styx.common.compose.threads.Heartbeats
import moe.styx.common.compose.threads.RequestQueue
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.utils.LocalToaster
import moe.styx.common.extension.formattedStrFile
import moe.styx.common.http.getHttpClient
import moe.styx.common.util.Log
import moe.styx.common.util.launchGlobal
import moe.styx.logic.DiscordRPC
import moe.styx.logic.Files
import moe.styx.logic.runner.currentPlayer
import moe.styx.views.anime.AnimeOverview
import java.io.File
import java.io.PrintStream

object Main {
    var isUiModeDark: MutableState<Boolean> = mutableStateOf(true)
    var useMonoFont: MutableState<Boolean> = mutableStateOf(false)
    var densityScale: MutableState<Float> = mutableStateOf(1f)

    private val _windowSizeFlow = MutableStateFlow<IntSize?>(null)
    val windowSizeFlow: MutableStateFlow<IntSize?>
        get() = _windowSizeFlow

    var wasLaunchedInDebug = false

    fun setupLogFile() {
        val time = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).formattedStrFile()
        val dir = File(Files.getAppDir(), "Logs")
        dir.mkdirs()
        val file = File(dir, "Log - $time.txt")
        val stream = PrintStream(file.outputStream())
        System.setOut(stream)
        System.setErr(stream)
        if (settings["enable-debug-logs", false])
            Log.debugEnabled = true
    }
}

fun main(args: Array<String>) = application {
    if (!args.contains("-debug") && System.getenv("STYX_DEBUG").isNullOrBlank())
        Main.setupLogFile()
    else {
        Log.i { "Launching in debug mode." }
        Main.wasLaunchedInDebug = true
        Log.debugEnabled = true
    }
    getHttpClient("${BuildConfig.APP_NAME} - ${BuildConfig.APP_VERSION}", true)
    appConfig = {
        AppConfig(
            BuildConfig.APP_SECRET,
            BuildConfig.APP_VERSION,
            BuildConfig.BASE_URL,
            BuildConfig.IMAGE_URL,
            null,
            Files.getCacheDir().absolutePath,
            Files.getDataDir().absolutePath,
            BuildConfig.VERSION_CHECK_URL
        )
    }
    if (settings["discord-rpc", true]) {
        DiscordRPC.start()
    }

    launchGlobal {
        Heartbeats.start()
        delay(10000)
        RequestQueue.start()
        DownloadQueue.start()
        while (true) {
            delay(3000)
            DiscordRPC.updateActivity()
            if (currentPlayer == null)
                Heartbeats.mediaActivity = null
        }
    }
    Main.useMonoFont.value = settings["mono-font", false]
    Main.isUiModeDark.value = settings["darkmode", true]
    Main.densityScale.value = settings["density-scale", 1f]
    val darkMode by remember { Main.isUiModeDark }
    val monoFont by remember { Main.useMonoFont }
    var popCalled by remember { mutableStateOf(false) }
    val windowState = rememberWindowState(width = settings["last_window_width", 800].dp, height = settings["last_window_height", 750].dp)

    Window(
        onCloseRequest = { onClose() },
        title = "${BuildConfig.APP_NAME} - ${BuildConfig.APP_VERSION}",
        state = windowState,
        icon = painterResource("icons/icon.ico"),
        onKeyEvent = {
            if (it.key == Key.Escape) {
                popCalled = true
            }
            false
        }
    )
    {
        Log.i { "Compose window initialized with: ${this.window.renderApi}" }
        Log.i { "Starting ${BuildConfig.APP_NAME} v${BuildConfig.APP_VERSION}" }
        Surface(modifier = Modifier.fillMaxSize()) {
            val currentDensity = LocalDensity.current
            LaunchedEffect(windowState.size) {
                with(currentDensity) {
                    Main.windowSizeFlow.emit(IntSize(windowState.size.width.roundToPx(), windowState.size.height.roundToPx()))
                }
            }
            CompositionLocalProvider(LocalDensity provides Density(currentDensity.density * Main.densityScale.value)) {
                val toasterState = rememberToasterState()
                val font = if (monoFont) AppFont.JetbrainsMono() else AppFont.OpenSans()
                MaterialTheme(
                    colorScheme = if (darkMode) darkScheme else lightScheme,
                    typography = font.Typography,
                    shapes = AppShapes
                ) {
                    Toaster(toasterState, darkTheme = darkMode, richColors = true, widthPolicy = { ToastWidthPolicy(0.dp, 450.dp) })
                    Navigator(AnimeOverview()) { navigator ->
                        CompositionLocalProvider(
                            LocalGlobalNavigator provides navigator,
                            LocalKamelConfig provides kamelConfig,
                            LocalToaster provides toasterState
                        ) {
                            LaunchedEffect(popCalled) {
                                if (popCalled) {
                                    navigator.pop()
                                    popCalled = false
                                }
                            }
                            SlideTransition(
                                navigator, animationSpec = spring(
                                    stiffness = Spring.StiffnessMedium,
                                    visibilityThreshold = IntOffset.VisibilityThreshold
                                )
                            )

                            val debouncedWindowSize by Main.windowSizeFlow.debounce(500L).collectAsState(null)
                            LaunchedEffect(debouncedWindowSize) {
                                if (debouncedWindowSize != null) {
                                    settings["last_window_width"] = debouncedWindowSize!!.width
                                    settings["last_window_height"] = debouncedWindowSize!!.height
                                }
                            }
                        }
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