package moe.styx.moe.styx.logic.data

import kotlinx.coroutines.delay

suspend fun updateImageCache() {
    delay(1000)
    cleanUnusedImages()
}

private fun cleanUnusedImages() {

}

