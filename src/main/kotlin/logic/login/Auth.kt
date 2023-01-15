package logic.login

import Endpoints
import com.russhwolf.settings.get
import httpClient
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import logic.data.CreationResponse
import logic.data.DeviceInfo
import logic.data.LoginResponse
import settings
import kotlin.system.exitProcess

fun generateCode(): CreationResponse = runBlocking {
    val response: HttpResponse = httpClient.submitForm(
        Endpoints.DEVICE_CREATE.url(),
        formParameters = Parameters.build {
            append(
                "info",
                json.encodeToString(
                    DeviceInfo(
                        "PC",
                        "Alex' PC",
                        null,
                        null,
                        null,
                        "Windows 10",
                        "1904",
                        null,
                        null
                    )
                )
            )
        }
    )

    if (response.status.value !in 200..299) {
        exitProcess(1)
    }

    return@runBlocking json.decodeFromString(response.bodyAsText())
}

var login: LoginResponse? = null

fun isLoggedIn(): Boolean {
    val token = settings["refreshToken", ""]
    if (token.isBlank())
        return false

    if (login != null)
        return true

    val loginTry = checkLogin(token)
    if (loginTry != null) {
        login = loginTry
        return true
    }

    return false
}

fun checkLogin(token: String, first: Boolean = false): LoginResponse? = runBlocking {
    val response: HttpResponse = httpClient.submitForm(
        (if (first) Endpoints.DEVICE_FIRST_AUTH else Endpoints.LOGIN).url(),
        formParameters = Parameters.build {
            append("token", token)
        }
    )

    if (response.status.value in 200..203) {
        val log = json.decodeFromString<LoginResponse>(response.bodyAsText())
        login = log
        if (first && log.refreshToken != null)
            settings.putString("refreshToken", log.refreshToken)
        return@runBlocking login
    }
    return@runBlocking null
}