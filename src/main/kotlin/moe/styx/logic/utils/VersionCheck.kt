package moe.styx.logic.utils

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.styx.Styx_2.BuildConfig
import moe.styx.common.compose.http.login
import moe.styx.common.http.httpClient
import moe.styx.common.util.launchGlobal
import moe.styx.logic.Files
import java.awt.Desktop
import java.io.File
import kotlin.system.exitProcess

fun downloadNewInstaller() = launchGlobal {
    val response = httpClient.get("${BuildConfig.BASE_URL}/download/desktop?token=${login?.accessToken}")
    if (!response.status.isSuccess())
        return@launchGlobal
    val contentDispositionHeader = response.headers[HttpHeaders.ContentDisposition]
    val filename = extractFilename(contentDispositionHeader)
    launch(Dispatchers.IO) {
        val outFile = File(Files.getAppDir(), filename)
        val openChannel = response.bodyAsChannel()
        val outChannel = outFile.writeChannel()
        openChannel.copyAndClose(outChannel)
        runCatching { outChannel.close() }
        if (Desktop.isDesktopSupported() && outFile.exists() && outFile.length() > 100) {
            delay(1500L)
            Desktop.getDesktop().open(outFile.parentFile)
            delay(500L)
            Desktop.getDesktop().open(outFile)
            delay(1000L)
            exitProcess(0)
        }
    }
}


fun extractFilename(contentDisposition: String?): String {
    if (contentDisposition == null)
        return "Installer.msi"

    val startIndex = contentDisposition.indexOf("filename=")
    return if (startIndex != -1) {
        val startQuoteIndex = contentDisposition.indexOf('"', startIndex)
        val endQuoteIndex = contentDisposition.indexOf('"', startQuoteIndex + 1)
        contentDisposition.substring(startQuoteIndex + 1, endQuoteIndex)
    } else {
        "Installer.msi"
    }
}