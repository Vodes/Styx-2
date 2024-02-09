package moe.styx.logic.login

import com.russhwolf.settings.get
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import moe.styx.Main.settings
import moe.styx.Styx__.BuildConfig
import moe.styx.common.data.CreationResponse
import moe.styx.common.data.DeviceInfo
import moe.styx.common.data.LoginResponse
import moe.styx.common.http.httpClient
import moe.styx.common.json
import moe.styx.logic.Endpoints
import moe.styx.logic.data.DataManager
import moe.styx.logic.login.ServerStatus.Companion.setLastKnown
import oshi.SystemInfo
import java.io.File
import kotlin.system.exitProcess


fun generateCode(): CreationResponse = runBlocking {
    val info = json.encodeToString(fetchDeviceInfo())
    val response: HttpResponse = httpClient.submitForm(
        Endpoints.DEVICE_CREATE.url(),
        formParameters = Parameters.build {
            append("info", info)
        }
    )

    if (response.status.value !in 200..299) {
        exitProcess(1)
    }

    return@runBlocking json.decodeFromString(response.bodyAsText())
}

fun fetchDeviceInfo(): DeviceInfo {
    val sInfo = SystemInfo()
    val hal = sInfo.hardware
    val power = sInfo.hardware.powerSources
    val gpus = hal.graphicsCards
    val os = sInfo.operatingSystem
    return DeviceInfo(
        power?.firstOrNull()?.let { if (it.voltage == -1.0 || it.amperage == 0.0 || it.cycleCount == -1) "PC" else "Laptop" } ?: "PC",
        os.networkParams.hostName,
        null,
        hal.processor?.processorIdentifier?.name?.trim(),
        if (gpus.isEmpty()) null else gpus.joinToString { it.name },
        "$os".trim(),
        null,
        "${System.getProperty("java.vm.name")} (${System.getProperty("java.vm.version")})",
        System.getProperty("java.version"),
        BuildConfig.APP_SECRET
    )
}

var login: LoginResponse? = null

fun isLoggedIn(): Boolean {
    val debugFile = File(DataManager.getAppDir(), "debug.token")
    val token = if (debugFile.exists() && debugFile.readText().isNotBlank()) {
        debugFile.readText().trim()
    } else {
        settings["refreshToken", ""]
    }

    if (token.isBlank()) {
        ServerStatus.lastKnown = ServerStatus.UNAUTHORIZED
        return false
    }

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
    val response = runCatching {
        httpClient.submitForm(
            (if (first) Endpoints.DEVICE_FIRST_AUTH else Endpoints.LOGIN).url(),
            formParameters = Parameters.build {
                append("token", token)
                append("content", runCatching { json.encodeToString(fetchDeviceInfo()) }.onFailure { it.printStackTrace() }.getOrNull() ?: "")
            }
        )
    }.onFailure { it.printStackTrace().also { ServerStatus.lastKnown = ServerStatus.UNKNOWN } }.getOrNull() ?: return@runBlocking null

    setLastKnown(response.status)

    if (response.status.value in 200..203) {
        val log = json.decodeFromString<LoginResponse>(response.bodyAsText())
        login = log
        if (first && log.refreshToken != null)
            settings.putString("refreshToken", log.refreshToken!!)
        return@runBlocking login
    } else {
        println(response.bodyAsText())
    }
    return@runBlocking null
}