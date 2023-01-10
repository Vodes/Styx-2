package views.anime

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource
import logic.data.get_anime
import java.net.URLDecoder

data class AnimeView(val ID: String) : Screen {

    @Composable
    override fun Content() {
        val scaffoldState = rememberScaffoldState()
        val nav = LocalNavigator.currentOrThrow
        val anime = get_anime().find { a -> a.name.contentEquals(ID) }
        val episodes = (1..12).map { it.toString() }
        Scaffold(scaffoldState = scaffoldState, topBar = {
            TopAppBar(
                title = { Text(anime?.name ?: "No Anime found.") },
                backgroundColor = MaterialTheme.colors.primaryVariant,
                actions = {
                    IconButton(onClick = { nav.pop(); }, content = { Icon(Icons.Filled.Close, null) })
                }
            )
        }) {
            if (anime == null)
                return@Scaffold

            Row(Modifier.fillMaxSize()) {
                Column(modifier = Modifier.padding(5.dp).fillMaxHeight().fillMaxWidth(0.35F)) {
                    //Card(modifier= Modifier.padding(3.dp).aspectRatio(0.71F).fillMaxHeight(0.45F), elevation = 5.dp){
                    KamelImage(
                        lazyPainterResource(anime.coverURL, filterQuality = FilterQuality.High),
                        contentDescription = "Anime",
                        modifier = Modifier.padding(2.dp).aspectRatio(0.71F).fillMaxHeight(0.45F),
                        contentScale = ContentScale.FillBounds,
                    )
                    Column {
                        Text("English:\n${anime.english}")
                        Text("Romaji:\n${anime.romaji}")
                    }
                }
                Column(modifier = Modifier.padding(5.dp)) {
                    Text(URLDecoder.decode(anime.synopsis, "UTF-8"))

                    Card(modifier = Modifier.padding(5.dp, 10.dp).fillMaxSize(), elevation = 5.dp) {
                        LazyColumn(modifier = Modifier.padding(5.dp)) {
                            items(episodes.size) { i ->
                                Text("Episode $i")
                            }
                        }
                    }
                }
            }
        }
    }
}