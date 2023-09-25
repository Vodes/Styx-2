package moe.styx.moe.styx.logic.runner

import com.russhwolf.settings.get
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.styx.Endpoints
import moe.styx.logic.login.login
import moe.styx.settings
import moe.styx.types.MediaEntry
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

@OptIn(DelicateCoroutinesApi::class)
fun launchMPV(entry: MediaEntry, onFail: (String) -> Unit = {}) {
    val isWindows = System.getProperty("os.name").contains("win", true)
    val tryFlatpak = settings["mpv-flatpak", false]

    var mpvExecutable = getExecutableFromPath(if (isWindows) "mpv.exe" else "mpv")
    if (!isWindows && tryFlatpak) {
        mpvExecutable = getExecutableFromPath("flatpak")
    }
    if (mpvExecutable == null) {
        onFail("MPV could not be found.")
        return;
    }

    val url = "${Endpoints.WATCH.url()}/${entry.GUID}?token=${login!!.watchToken}"

    GlobalScope.launch {
        var commands = if (!isWindows && tryFlatpak) listOf(
            mpvExecutable.absolutePath,
            "run",
            "io.mpv.Mpv",
            url
        ) else listOf(mpvExecutable.absolutePath, url)
        val proc = ProcessBuilder(commands).directory(mpvExecutable.parentFile)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE).start()

        launch {
            doSomethingAboutIt(proc.inputStream)
        }

        launch {
            doSomethingAboutIt(proc.errorStream)
        }

        proc.waitFor()
    }
}

fun doSomethingAboutIt(stream: InputStream) {
    var output = ""
    val inputStream = BufferedReader(InputStreamReader(stream))
    while (inputStream.readLine()?.also { output = it } != null) {
        //lol lmao rofl don't need the output
    }
    inputStream.close()
}