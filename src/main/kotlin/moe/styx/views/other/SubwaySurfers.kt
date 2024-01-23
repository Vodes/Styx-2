package moe.styx.moe.styx.views.other

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import com.multiplatform.webview.util.KLogSeverity
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.styx.dataManager
import moe.styx.moe.styx.components.MainScaffold
import java.io.File
import kotlin.math.max

class SubwaySurfers : Screen {

    @Composable
    override fun Content() {
        var restartRequired by remember { mutableStateOf(false) }
        var downloading by remember { mutableStateOf(0F) }
        var initialized by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                KCEF.init(builder = {
                    installDir(File(dataManager.getAppDir(), "kcef-bundle"))
                    progress {
                        onDownloading {
                            downloading = max(it, 0F)
                        }
                        onInitialized {
                            initialized = true
                        }
                    }
                    settings {
                        cachePath = File(dataManager.getAppDir(), "cache").absolutePath
                    }
                }, onError = {
                    it?.printStackTrace()
                }, onRestartRequired = {
                    restartRequired = true
                })
            }
        }

        MainScaffold(title = "Subway Surfers") {
            if (restartRequired || !initialized) {
                if (restartRequired)
                    Text("Restart required.")
                else
                    Text(text = "Downloading $downloading%")
            } else
                SubwayWebview()
        }

        DisposableEffect(Unit) {
            onDispose {
                KCEF.disposeBlocking()
            }
        }
    }

    @Composable
    fun SubwayWebview() {
        val state = rememberWebViewState("https://ad-freegames.github.io/subway-surfers/")
        val navigator = rememberWebViewNavigator()
        state.webSettings.apply {
            logSeverity = KLogSeverity.Error
            isJavaScriptEnabled = true
            customUserAgentString =
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_1) AppleWebKit/625.20 (KHTML, like Gecko) Version/14.3.43 Safari/625.20"
        }
        Column(Modifier.fillMaxSize().background(Color.LightGray)) {
            WebView(state, Modifier.fillMaxSize(), navigator = navigator)
        }
    }
}