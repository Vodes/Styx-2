package moe.styx

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import moe.styx.logic.login.login
import moe.styx.moe.styx.logic.login.ServerStatus
import moe.styx.types.Changes
import moe.styx.types.json

val httpClient = HttpClient() {
    install(ContentNegotiation) {
        json
    }
}

//private const val baseURL = "http://localhost:8081"
private const val baseURL = "https://api.styx.moe"

enum class Endpoints(private val path: String) {
    // TODO: Login
    LOGIN("/login"),
    DEVICE_CREATE("/device/create"),
    DEVICE_FIRST_AUTH("/device/firstAuth"),

    // TODO: Media
    MEDIA("/media/list"),
    MEDIA_ENTRIES("/media/entries"),
    IMAGES("/media/images"),
    CATEGORIES("/media/categories"),
    SCHEDULES("/media/schedules"),

    // TODO: Favourites
    FAVOURITES("/favourites/list"),
    FAVOURITES_ADD("/favourites/add/"),
    FAVOURITES_DELETE("/favourites/delete/"),

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

inline fun <reified T> sendObject(endpoint: Endpoints, data: T?): Boolean = runBlocking {
    val request = runCatching {
        httpClient.post {
            url(endpoint.url())
            contentType(ContentType.Application.Json)
            setBody(data)
        }
    }.onFailure { ServerStatus.lastKnown = ServerStatus.UNKNOWN }.getOrNull() ?: return@runBlocking false

    ServerStatus.setLastKnown(request.status)

    return@runBlocking request.status.value in 200..203
}

inline fun <reified T> getObject(endpoint: Endpoints): T? = runBlocking {
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
    getObject<Changes>(Endpoints.CHANGES)
    return ServerStatus.lastKnown != ServerStatus.UNKNOWN
}

suspend fun awaitAll(vararg jobs: Job) {
    jobs.asList().joinAll()
}