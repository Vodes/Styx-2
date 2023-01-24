package moe.styx.logic.data

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

data class AnimeOld(
    val name: String, val listlink: String, val coverURL: String,
    val season: String, val coverMD5: String, val english: String, val romaji: String,
    val schedule: String, val synopsis: String
)

fun get_anime(): List<AnimeOld> {
    val values = mapOf("action" to "anime", "hwid" to "64E10CEA-D911-43F1-8C5F-3005A7C2F155")

    val client = HttpClient.newBuilder().build();
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://vodes.pw/awcp/Auth/Lists.php"))
        .POST(formData(values))
        .header("Content-Type", "application/x-www-form-urlencoded")
        .build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString());
    val lines = response.body().split("\n")
    val anime: MutableList<AnimeOld> = mutableListOf()

    for (line in lines) {
        try {
            val s = line.split(";;")
            anime.add(AnimeOld(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8]))
        } catch (e: Exception) {
            //e.printStackTrace()
        }
    }
    return anime
}

fun String.utf8(): String = URLEncoder.encode(this, "UTF-8")

fun formData(data: Map<String, String>): HttpRequest.BodyPublisher? {

    val res = data.map { (k, v) -> "${(k.utf8())}=${v.utf8()}" }
        .joinToString("&")

    return HttpRequest.BodyPublishers.ofString(res)
}