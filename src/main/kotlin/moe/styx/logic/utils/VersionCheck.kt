package moe.styx.logic.utils

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import moe.styx.Styx__.BuildConfig
import moe.styx.common.compose.http.login
import moe.styx.common.extension.eqI
import moe.styx.common.http.httpClient
import moe.styx.common.util.launchGlobal
import moe.styx.logic.Files
import java.awt.Desktop
import java.io.File
import kotlin.system.exitProcess

// TODO: Actually check at some point lol.
private val versionRegex = "^version = \\\"(?<version>(?<major>\\d)\\.(?<minor>\\d)(?:\\.(?<patch>\\d))?)\\\"\$".toRegex(RegexOption.IGNORE_CASE)
fun isUpToDate(): Boolean = runBlocking {
    val response = httpClient.get(BuildConfig.VERSION_CHECK_URL)
    if (!response.status.isSuccess())
        return@runBlocking true

    val lines = response.bodyAsText().lines()
    for (line in lines) {
        val match = versionRegex.matchEntire(line) ?: continue
        runCatching {
            val parsed = match.groups["version"]!!.value
            if (parsed eqI BuildConfig.APP_VERSION)
                return@runBlocking true
        }
    }

    return@runBlocking false
}

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
            Desktop.getDesktop().open(outFile)
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