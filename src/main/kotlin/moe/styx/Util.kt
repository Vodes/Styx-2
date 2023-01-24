package moe.styx

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import moe.styx.logic.login.login

val json = Json {
    prettyPrint = true
    isLenient = true
}

val httpClient = HttpClient() {
    install(ContentNegotiation) {
        json
    }
}

private const val baseURL = "http://localhost:8080"

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

    CHANGES("/changes");

    fun url(): String {
        return baseURL + path
    }
}

/*
for example gets you a list of all Media by doing

val media = getList<Media>(Endpoints.MEDIA)
 */

inline fun <reified T> getList(endpoint: Endpoints): List<T> = runBlocking {
    var list = listOf<T>()

    val response: HttpResponse = httpClient.submitForm(
        endpoint.url(),
        formParameters = Parameters.build {
            append("token", login!!.accessToken)
        }
    )

    if (response.status.value in 200..203) {
        list = json.decodeFromString(response.bodyAsText())
    }

    return@runBlocking list
}

inline fun <reified T> getObject(endpoint: Endpoints): T? = runBlocking {
    val response: HttpResponse = httpClient.get(endpoint.url())

    if (response.status.value in 200..203) {
        return@runBlocking json.decodeFromString(response.bodyAsText())
    }

    return@runBlocking null
}