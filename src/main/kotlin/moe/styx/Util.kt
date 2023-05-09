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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import moe.styx.logic.login.login
import moe.styx.moe.styx.logic.login.ServerStatus

val json = Json {
    prettyPrint = true
    isLenient = true
}

val httpClient = HttpClient() {
    install(ContentNegotiation) {
        json
    }
}

//private const val baseURL = "http://localhost:8080"
private const val baseURL = "https://beta.styx.moe"

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
    var list = listOf<T>()

    if (!hasInternet())
        return list

    val response: HttpResponse = httpClient.submitForm(
        endpoint.url(),
        formParameters = Parameters.build {
            append("token", login!!.accessToken)
        }
    )

    ServerStatus.setLastKnown(response.status)

    if (response.status.value in 200..203) {
        list = json.decodeFromString(response.bodyAsText())
    }

    return list
}

inline fun <reified T> sendObject(endpoint: Endpoints, data: T?): Boolean = runBlocking {
    val request = httpClient.post(endpoint.url()) {
        contentType(ContentType.Application.Json)
        setBody(data)
    }

    ServerStatus.setLastKnown(request.status)

    return@runBlocking request.status.value in 200..203
}

inline fun <reified T> getObject(endpoint: Endpoints): T? = runBlocking {
    val response: HttpResponse = httpClient.get(endpoint.url())

    ServerStatus.setLastKnown(response.status)

    if (response.status.value in 200..203) {
        return@runBlocking json.decodeFromString(response.bodyAsText())
    }

    return@runBlocking null
}

fun hasInternet(): Boolean {
    return true
}

suspend fun awaitAll(vararg jobs: Job) {
    jobs.asList().joinAll()
}