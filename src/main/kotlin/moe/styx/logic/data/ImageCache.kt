package moe.styx.logic.data

import androidx.compose.runtime.getValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import moe.styx.Styx__.BuildConfig
import moe.styx.common.data.Image
import moe.styx.common.extension.eqI
import moe.styx.common.extension.toBoolean
import moe.styx.common.http.httpClient
import java.io.File

suspend fun updateImageCache() = coroutineScope {
    delay(500)

    val images by DataManager.images
    images.filter { !it.isCached() }.forEach { it.downloadFile() }
    cleanUnusedImages()
}

private fun cleanUnusedImages() {
    val files = getImageDir().listFiles()
    if (files.isNullOrEmpty())
        return

    val images by DataManager.images

    files.associateWith { f -> images.find { it.GUID eqI f.nameWithoutExtension } }
        .filterValues { it == null }
        .forEach { it.key.delete() }
}

private fun getImageDir(): File {
    return File(DataManager.getAppDir(), "Images").also { it.mkdirs() }
}

suspend fun Image.downloadFile() {
    val response = httpClient.get(getURL())
    if (response.status.isSuccess())
        response.bodyAsChannel().copyAndClose(getFile().writeChannel())
}

fun Image.isCached(): Boolean {
    return getFile().exists()
}

fun Image.getFile(): File {
    return if (hasWEBP?.toBoolean() == true)
        File(getImageDir(), "$GUID.webp")
    else if (hasJPG?.toBoolean() == true)
        File(getImageDir(), "$GUID.jpg")
    else
        File(getImageDir(), "$GUID.png")
}

fun Image.getURL(): String {
    return if (hasWEBP?.toBoolean() == true) {
        "${BuildConfig.IMAGE_URL}/$GUID.webp"
    } else if (hasJPG?.toBoolean() == true) {
        "${BuildConfig.IMAGE_URL}/$GUID.jpg"
    } else if (hasPNG?.toBoolean() == true) {
        "${BuildConfig.IMAGE_URL}/$GUID.png"
    } else {
        return externalURL as String
    }
}

fun String?.getImageFromID(): Image? {
    return this.let { DataManager.images.value.find { it.GUID eqI this } }
}
