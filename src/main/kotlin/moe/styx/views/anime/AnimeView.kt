package moe.styx.views.anime

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource
import moe.styx.dataManager
import moe.styx.logic.data.getImageFromID
import moe.styx.logic.data.getURL
import moe.styx.moe.styx.components.PopButton
import moe.styx.moe.styx.navigation.LocalGlobalNavigator
import java.net.URLDecoder

data class AnimeView(val ID: String) : Screen {

    @Composable
    override fun Content() {
        val scaffoldState = rememberScaffoldState()
        val nav = LocalGlobalNavigator.current
        val anime = dataManager.media.value.find { a -> a.name.contentEquals(ID) }

        if (anime == null) {
            nav.pop();
            return
        }
        val episodes =
            dataManager.entries.value.filter { it.mediaID == anime.GUID }.sortedByDescending { it.entryNumber }

        Scaffold(scaffoldState = scaffoldState, topBar = {
            TopAppBar(
                title = { Text(anime.name) },
                backgroundColor = MaterialTheme.colors.primaryVariant,
                actions = { PopButton(nav) }
            )
        }) {
            Row(Modifier.fillMaxSize()) {
                Column(modifier = Modifier.padding(5.dp).fillMaxHeight().fillMaxWidth(0.35F)) {
                    //Card(modifier= Modifier.padding(3.dp).aspectRatio(0.71F).fillMaxHeight(0.45F), elevation = 5.dp){
                    KamelImage(
                        lazyPainterResource(
                            anime.thumbID.getImageFromID()!!.getURL(),
                            filterQuality = FilterQuality.High
                        ),
                        contentDescription = "Anime",
                        modifier = Modifier.padding(2.dp).aspectRatio(0.71F).fillMaxHeight(0.45F),
                        contentScale = ContentScale.FillBounds,
                    )
                    Column {
                        Text("English:\n${anime.nameEN}")
                        Text("Romaji:\n${anime.nameJP}")
                    }
                }
                Column(modifier = Modifier.padding(5.dp)) {
                    Text(URLDecoder.decode(anime.synopsisEN, "UTF-8"))

                    Card(modifier = Modifier.padding(5.dp, 10.dp).fillMaxSize(), elevation = 5.dp) {
                        LazyColumn(modifier = Modifier.padding(5.dp)) {
                            items(episodes.size) { i ->
                                val ep = episodes[i]
                                Text("Episode ${ep.entryNumber} — ${ep.nameEN}")
                            }
                        }
                    }
                }
            }
        }
    }
}