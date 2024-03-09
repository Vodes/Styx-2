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
import com.russhwolf.settings.get
import io.kamel.image.config.LocalKamelConfig
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import moe.styx.Main.isUiModeDark
import moe.styx.Main.setupLogFile
import moe.styx.Styx__.BuildConfig
import moe.styx.common.compose.AppConfig
import moe.styx.common.compose.appConfig
import moe.styx.common.compose.extensions.kamelConfig
import moe.styx.common.compose.http.*
import moe.styx.common.compose.settings
import moe.styx.common.compose.threads.Heartbeats
import moe.styx.common.compose.threads.RequestQueue
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.compose.utils.Log
import moe.styx.common.compose.utils.ServerStatus
import moe.styx.common.extension.formattedStrFile
import moe.styx.common.http.getHttpClient
import moe.styx.common.util.launchGlobal
import moe.styx.logic.DiscordRPC
import moe.styx.logic.data.DataManager
import moe.styx.logic.runner.currentPlayer
import moe.styx.theme.*
import moe.styx.views.login.LoginView
import moe.styx.views.login.OfflineView
import moe.styx.views.other.LoadingView
import java.io.File
import java.io.PrintStream

object Main {
    var isUiModeDark: MutableState<Boolean> = mutableStateOf(true)
    var wasLaunchedInDebug = false

    fun setupLogFile() {
        val time = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).formattedStrFile()
        val dir = File(DataManager.getAppDir(), "Logs")
        dir.mkdirs()
        val file = File(dir, "Log - $time.txt")
        val stream = PrintStream(file.outputStream())
        System.setOut(stream)
        System.setErr(stream)
    }
}

fun main(args: Array<String>) = application {
    if (!args.contains("-debug"))
        setupLogFile()
    else
        Main.wasLaunchedInDebug = true
    getHttpClient("${BuildConfig.APP_NAME} - ${BuildConfig.APP_VERSION}")
    appConfig = {
        AppConfig(
            BuildConfig.APP_SECRET,
            BuildConfig.APP_VERSION,
            BuildConfig.BASE_URL,
            BuildConfig.IMAGE_URL,
            null,
            DataManager.getCacheDir().absolutePath,
            DataManager.getDataDir().absolutePath
        )
    }
    if (settings["discord-rpc", true]) {
        DiscordRPC.start()
    }
    RequestQueue.start()
    Heartbeats.start()

    launchGlobal {
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
                    CompositionLocalProvider(LocalGlobalNavigator provides navigator, LocalKamelConfig provides kamelConfig) {
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