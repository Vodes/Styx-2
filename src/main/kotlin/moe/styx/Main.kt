package moe.styx

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterState
import com.dokar.sonner.rememberToasterState
import com.russhwolf.settings.get
import io.kamel.image.config.LocalKamelConfig
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import moe.styx.Main.isUiModeDark
import moe.styx.Main.setupLogFile
import moe.styx.Styx_2.BuildConfig
import moe.styx.common.compose.AppConfig
import moe.styx.common.compose.AppContextImpl.appConfig
import moe.styx.common.compose.extensions.kamelConfig
import moe.styx.common.compose.http.Endpoints
import moe.styx.common.compose.http.sendObject
import moe.styx.common.compose.settings
import moe.styx.common.compose.threads.DownloadQueue
import moe.styx.common.compose.threads.Heartbeats
import moe.styx.common.compose.threads.RequestQueue
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.extension.formattedStrFile
import moe.styx.common.http.getHttpClient
import moe.styx.common.util.Log
import moe.styx.common.util.launchGlobal
import moe.styx.logic.DiscordRPC
import moe.styx.logic.Files
import moe.styx.logic.runner.currentPlayer
import moe.styx.theme.*
import moe.styx.views.anime.AnimeOverview
import java.io.File
import java.io.PrintStream

object Main {
    var isUiModeDark: MutableState<Boolean> = mutableStateOf(true)
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

val LocalToasterState: ProvidableCompositionLocal<ToasterState> =
    staticCompositionLocalOf { error("LocalToasterState not initialized") }

@OptIn(ExperimentalComposeUiApi::class)
fun main(args: Array<String>) = application {
    if (!args.contains("-debug"))
        setupLogFile()
    else {
        Main.wasLaunchedInDebug = true
        Log.debugEnabled = true
    }
    getHttpClient("${BuildConfig.APP_NAME} - ${BuildConfig.APP_VERSION}")
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

    isUiModeDark.value = settings["darkmode", true]
    val darkMode by remember { isUiModeDark }

    Window(
        onCloseRequest = { onClose() },
        title = "${BuildConfig.APP_NAME} - ${BuildConfig.APP_VERSION}",
        state = WindowState(width = 800.dp, height = 750.dp),
        icon = painterResource("icons/icon.ico")
    )
    {
        Log.i { "Compose window initialized with: ${this.window.renderApi}" }
        Log.i { "Starting ${BuildConfig.APP_NAME} v${BuildConfig.APP_VERSION}" }
        Surface(modifier = Modifier.fillMaxSize()) {
            val toasterState = rememberToasterState()
            MaterialTheme(
                colorScheme = if (darkMode) darkScheme else lightScheme,
                typography = AppTypography,
                shapes = AppShapes
            ) {
                Toaster(toasterState, darkTheme = darkMode)
                Navigator(AnimeOverview()) { navigator ->
                    CompositionLocalProvider(
                        LocalGlobalNavigator provides navigator,
                        LocalKamelConfig provides kamelConfig,
                        LocalToasterState provides toasterState
                    ) {
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