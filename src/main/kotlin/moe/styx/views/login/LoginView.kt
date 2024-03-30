package moe.styx.views.login

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.delay
import moe.styx.Styx__.BuildConfig
import moe.styx.common.compose.components.buttons.IconButtonWithTooltip
import moe.styx.common.compose.http.checkLogin
import moe.styx.common.compose.http.generateCode
import moe.styx.common.compose.http.isLoggedIn
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.views.other.LoadingView
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI

class LoginView() : Screen {

    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current
        var creationResponse by remember { mutableStateOf(generateCode()) }
        var countdown by remember { mutableStateOf(30) }

        LaunchedEffect(Unit) {
            while (!isLoggedIn()) {
                countdown--
                val log = creationResponse?.let {
                    return@let checkLogin(creationResponse!!.GUID, true)
                }
                if (log != null) {
                    nav.push(LoadingView())
                    break
                }

                delay(1000)
                if (countdown < 2) {
                    countdown = 30
                    creationResponse = generateCode()
                }
            }
        }

        val progressAnimation by animateFloatAsState(
            countdown / 30F,
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
        )
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.fillMaxWidth().weight(1f, true)) {
                Text(
                    "Registration Code",
                    Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 10.dp),
                    style = MaterialTheme.typography.titleLarge
                )

                Row(Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        creationResponse?.let { "${it.code}" } ?: "Failed to request code",
                        Modifier.padding(0.dp, 10.dp),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    creationResponse?.let {
                        IconButtonWithTooltip(Icons.Default.ContentCopy, "Copy code") {
                            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                            val content = StringSelection(it.code.toString())
                            clipboard.setContents(content, content)
                        }
                    }
                }

                CircularProgressIndicator(
                    progress = { progressAnimation },
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 15.dp).fillMaxSize(.3F).weight(0.5F),
                )
            }
            Button(onClick = {
                if (Desktop.isDesktopSupported())
                    Desktop.getDesktop().browse(URI(BuildConfig.SITE_URL))
            }, Modifier.padding(10.dp)) {
                Text("Open ${BuildConfig.SITE}")
            }
        }
    }
}