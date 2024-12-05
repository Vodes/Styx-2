package moe.styx.views.other

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.dokar.sonner.*
import kotlinx.coroutines.delay
import moe.styx.Styx_2.BuildConfig
import moe.styx.common.compose.components.layout.MainScaffold
import moe.styx.common.compose.http.Endpoints
import moe.styx.common.compose.http.login
import moe.styx.common.compose.utils.LocalToaster
import moe.styx.common.http.DownloadResult
import moe.styx.common.http.downloadFileStream
import moe.styx.common.isWindows
import moe.styx.common.util.launchThreaded
import moe.styx.logic.Files
import moe.styx.logic.runner.openURI
import okio.Path.Companion.toPath
import java.awt.Desktop
import java.io.File
import kotlin.system.exitProcess

class OutdatedView(private val requestedVersion: String? = null) : Screen {

    @Composable
    override fun Content() {
        val shouldBeDownloading = remember { mutableStateOf(false) }

        MainScaffold(title = "Outdated", addPopButton = requestedVersion != null) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (requestedVersion == null) "This version of Styx is outdated." else "Download $requestedVersion",
                    Modifier.padding(10.dp).weight(1f),
                    style = MaterialTheme.typography.headlineMedium
                )
                DownloadButtons(shouldBeDownloading)
                Button({
                    openURI("${BuildConfig.SITE_URL}/user")
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary), modifier = Modifier.padding(10.dp)) {
                    Text("Open ${BuildConfig.SITE}")
                }
            }
        }
    }


    @Suppress("NAME_SHADOWING")
    @Composable
    fun ColumnScope.DownloadButtons(shouldBeDownloading: MutableState<Boolean>) {
        val toaster = LocalToaster.current
        var shouldBeDownloading by shouldBeDownloading
        Row(Modifier.weight(1f)) {
            if (isWindows) {
                Button({
                    shouldBeDownloading = true
                    runDownload("win", toaster) { shouldBeDownloading = false }
                }, enabled = !shouldBeDownloading) {
                    Text("Download and open installer")
                }
            } else {
                Button({
                    shouldBeDownloading = true
                    runDownload("rpm", toaster) { shouldBeDownloading = false }
                }, enabled = !shouldBeDownloading) {
                    Text("RPM")
                }
                Button({
                    shouldBeDownloading = true
                    runDownload("deb", toaster) { shouldBeDownloading = false }
                }, enabled = !shouldBeDownloading) {
                    Text("DEB")
                }
            }
        }
    }

    private fun runDownload(platform: String, toaster: ToasterState, onDone: () -> Unit) = launchThreaded {
        val outFile = File(Files.getDataDir().parentFile, "Installer." + if (isWindows) "msi" else platform)
        val result = downloadFileStream(
            Endpoints.DOWNLOAD_BUILD_BASE.url() + "/$platform" + (if (requestedVersion != null) "/$requestedVersion" else "") + "?token=${login?.accessToken}",
            outFile.absolutePath.toPath()
        )
        if (result !in arrayOf(DownloadResult.OK, DownloadResult.AbortExists)) {
            toaster.show(
                Toast(
                    "Failed to download installer! Please check the logs.",
                    type = ToastType.Error,
                    duration = ToasterDefaults.DurationLong
                )
            )
            onDone()
            return@launchThreaded
        }
        onDone()
        if (isWindows) {
            if (Desktop.isDesktopSupported() && outFile.exists() && outFile.length() > 100) {
                delay(1500L)
                Desktop.getDesktop().open(outFile.parentFile)
                delay(500L)
                Desktop.getDesktop().open(outFile)
                delay(1500L)
                exitProcess(0)
            }
        } else {
            openURI(outFile.parentFile.absolutePath)
            delay(1500L)
            exitProcess(0)
        }
    }
}
