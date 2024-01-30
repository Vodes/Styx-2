package moe.styx.logic

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import moe.styx.logic.login.ServerStatus
import moe.styx.logic.login.isLoggedIn
import moe.styx.logic.login.login
import moe.styx.logic.utils.currentUnixSeconds
import moe.styx.types.ApiResponse
import moe.styx.types.json

val httpClient = HttpClient() {
    install(ContentNegotiation) {
        json
    }
}

//private const val baseURL = "http://localhost:8081"
private const val baseURL = "https://api.styx.moe"

enum class Endpoints(private val path: String) {
    LOGIN("/login"),
    DEVICE_CREATE("/device/create"),
    DEVICE_FIRST_AUTH("/device/firstAuth"),
    HEARTBEAT("/heartbeat"),

    MEDIA("/media/list"),
    MEDIA_ENTRIES("/media/entries"),
    IMAGES("/media/images"),
    CATEGORIES("/media/categories"),
    SCHEDULES("/media/schedules"),

    FAVOURITES("/favourites/list"),
    FAVOURITES_ADD("/favourites/add"),
    FAVOURITES_DELETE("/favourites/delete"),
    FAVOURITES_SYNC("/favourites/sync"),

    WATCHED("/watched/list"),
    WATCHED_ADD("/watched/add"),
    WATCHED_DELETE("/watched/delete"),
    WATCHED_SYNC("/watched/sync"),

    CHANGES("/changes"),
    WATCH("/watch");

    fun url(): String {
        return baseURL + path
    }
}

/*
for example gets you a list of all Media by doing

val media = getList<Media>(Endpoints.MEDIA)
 */
suspend inline fun <reified T> getList(endpoint: Endpoints): List<T> {
    if (currentUnixSeconds() > login!!.tokenExpiry) {
        if (!isLoggedIn())
            return emptyList()
    }
    val response = runCatching {
        httpClient.submitForm(
            endpoint.url(),
            formParameters = Parameters.build {
                append("token", login!!.accessToken)
            }
        )
    }.onFailure { ServerStatus.lastKnown = ServerStatus.UNKNOWN }.getOrNull() ?: return emptyList()

    ServerStatus.setLastKnown(response.status)

    if (response.status.value in 200..203)
        return json.decodeFromString(response.bodyAsText())

    return emptyList()
}

inline fun <reified T> sendObjectWithResponse(endpoint: Endpoints, data: T?): ApiResponse? = runBlocking {
    if (currentUnixSeconds() > login!!.tokenExpiry) {
        if (!isLoggedIn())
            return@runBlocking null
    }
    val request = runCatching {
        httpClient.submitForm(endpoint.url(), formParameters = parameters {
            append("token", login!!.accessToken)
            append("content", json.encodeToString(data))
        })
    }.onFailure { ServerStatus.lastKnown = ServerStatus.UNKNOWN }.getOrNull() ?: return@runBlocking null

    ServerStatus.setLastKnown(request.status)

    if (!request.status.isSuccess()) {
        println("Request Failed for Endpoint `$endpoint`\n${request.bodyAsText()}")
    }

    val response = runCatching {
        json.decodeFromString<ApiResponse>(request.bodyAsText())
    }.getOrNull()

    return@runBlocking response
}

inline fun <reified T> sendObject(endpoint: Endpoints, data: T?): Boolean = runBlocking {
    return@runBlocking sendObjectWithResponse<T>(endpoint, data) != null
}

inline fun <reified T> getObject(endpoint: Endpoints): T? = runBlocking {
    if (currentUnixSeconds() > login!!.tokenExpiry) {
        if (!isLoggedIn())
            return@runBlocking null
    }
    val response = runCatching {
        httpClient.get {
            url(endpoint.url())
        }
    }.onFailure { ServerStatus.lastKnown = ServerStatus.UNKNOWN }.getOrNull() ?: return@runBlocking null

    ServerStatus.setLastKnown(response.status)

    if (response.status.value in 200..203) {
        return@runBlocking json.decodeFromString(response.bodyAsText())
    }

    return@runBlocking null
}

fun hasInternet(): Boolean {
    return ServerStatus.lastKnown != ServerStatus.UNKNOWN
}