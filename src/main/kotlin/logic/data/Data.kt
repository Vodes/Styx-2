package logic.data

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

data class AnimeOld(val name: String, val listlink: String, val coverURL: String,
                 val season: String, val coverMD5: String, val english: String, val romaji: String,
                 val schedule: String, val synopsis: String)

data class Media(val GUID: String, val name: String, val nameJP: String, val nameEN: String, val synopsisEN: String,
                 val synopsisDE: String, val thumbID: String, val bannerID: String, val categoryID: String,
                 val prequel: String, val sequel: String, val genres: String, val tags: String,
                 val metadataMap: String, val isSeries: Int)

data class Category(val GUID: String, val sort: Int, val isSeries: Int, val name: String)

data class Image(val GUID: String, val hasWEBP: Int = 0, val hasPNG: Int = 0,
                 val hasJPG: Int = 0, val externalURL: String, val type: Int = 0)

data class MediaEntry(val GUID: String, val mediaID: String, val timestamp: Float, val entryNumber: String,
                      val nameEN: String, val nameDE: String, val synopsisEN: String, val synopsisDE: String,
                      val thumbID: String, val filePath: String, val fileSize: Double, val originalName: String)

data class MediaInfo(val entryID: String, val videoCodec: String, val videoBitdepth: Int, val videoRes: String,
                     val hasEnglishDub: Int, val hasGermanDub: Int, val hasGermanSub: Int)

data class MediaWatched(val entryID: String, val userID: String, val lastWatched: Float, val progress: Float)

data class User(val GUID: String, val name: String, val discordID: String, val added: Float, val lastLogin: Float,
                val permissions: Int)


fun get_anime(): List<AnimeOld>{
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

    for (line in lines){
        try {
            val s = line.split(";;")
            anime.add(AnimeOld(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8]))
        } catch(e: Exception){
            //e.printStackTrace()
        }
    }
    return anime
}

fun String.utf8(): String = URLEncoder.encode(this, "UTF-8")

fun formData(data: Map<String, String>): HttpRequest.BodyPublisher? {

    val res = data.map {(k, v) -> "${(k.utf8())}=${v.utf8()}"}
        .joinToString("&")

    return HttpRequest.BodyPublishers.ofString(res)
}