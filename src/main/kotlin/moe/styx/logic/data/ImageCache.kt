package moe.styx.moe.styx.logic.data

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import moe.styx.dataManager
import moe.styx.httpClient
import moe.styx.types.Image
import moe.styx.types.toBoolean
import java.io.File

suspend fun updateImageCache() = coroutineScope {
    delay(1000)

    val images = dataManager.images.value
    for (i in images) {
        if (i.isCached())
            continue

        i.downloadFile()
    }

    cleanUnusedImages()
}

private fun cleanUnusedImages() {
    val files = getImageDir().listFiles()
    if (files.isNullOrEmpty())
        return

    val images = dataManager.images.value

    for (f in files) {
        val corresponding = images.find { it.GUID.equals(f.nameWithoutExtension, true) }
        if (corresponding == null) {
            f.delete()
        }
    }
}

private fun getImageDir(): File {
    val dir = File(dataManager.getAppDir(), "Images")
    dir.mkdirs()
    return dir
}

@OptIn(InternalAPI::class)
suspend fun Image.downloadFile() {
    val response = httpClient.get(getURL())
    if (response.status.isSuccess())
        response.content.copyAndClose(getFile().writeChannel())
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
        "https://i.styx.moe/$GUID.webp"
    } else if (hasJPG?.toBoolean() == true) {
        "https://i.styx.moe/$GUID.jpg"
    } else if (hasPNG?.toBoolean() == true) {
        "https://i.styx.moe/$GUID.png"
    } else {
        return externalURL as String
    }
}

fun String?.getImageFromID(): Image? {
    if (this == null)
        return null
    return dataManager.images.value.find { it.GUID.equals(this, true) }
}
