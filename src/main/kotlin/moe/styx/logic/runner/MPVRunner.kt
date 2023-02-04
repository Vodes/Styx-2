package moe.styx.moe.styx.logic.runner

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import moe.styx.Endpoints
import moe.styx.logic.data.MediaEntry
import moe.styx.logic.login.login
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

fun launchMPV(entry: MediaEntry, onFail: (String) -> Unit = {}) {
    val isWindows = System.getProperty("os.name").contains("win", true)

    val mpvExecutable = getExecutableFromPath(if (isWindows) "mpv.exe" else "mpv")
    if (mpvExecutable == null) {
        onFail("MPV could not be found.")
        return;
    }

    val url = "${Endpoints.WATCH.url()}/${entry.GUID}?token=${login!!.watchToken}"

    GlobalScope.launch {
        val proc = ProcessBuilder(listOf(mpvExecutable.absolutePath, url)).directory(mpvExecutable.parentFile)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
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