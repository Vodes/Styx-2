package moe.styx.logic.login

import com.russhwolf.settings.get
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import moe.styx.*
import moe.styx.moe.styx.logic.login.ServerStatus.Companion.setLastKnown
import moe.styx.types.CreationResponse
import moe.styx.types.DeviceInfo
import moe.styx.types.LoginResponse
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
    val token = settings["refreshToken", "4AC27790-3472-4409-87EC-625FF86B9D63"]
    if (token.isBlank())
        return false

    if (login != null || !hasInternet())
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

    setLastKnown(response.status)

    if (response.status.value in 200..203) {
        val log = json.decodeFromString<LoginResponse>(response.bodyAsText())
        login = log
        if (first && log.refreshToken != null)
            settings.putString("refreshToken", log.refreshToken!!)
        return@runBlocking login
    }
    return@runBlocking null
}