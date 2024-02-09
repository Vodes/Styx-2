package moe.styx.logic.utils

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import moe.styx.Styx__.BuildConfig
import moe.styx.common.extension.eqI
import moe.styx.common.http.httpClient

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