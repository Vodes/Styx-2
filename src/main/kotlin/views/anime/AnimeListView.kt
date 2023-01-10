package views.anime

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource
import logic.data.get_anime
import views.settings.SettingsView

@OptIn(ExperimentalMaterialApi::class)
class AnimeListView : Screen {
    @Composable
    override fun Content() {
        val anime = get_anime().reversed();
        val scaffoldState = rememberScaffoldState()
        val nav = LocalNavigator.currentOrThrow
        Scaffold(scaffoldState = scaffoldState, topBar = {
            TopAppBar(
                title = { Text("Styx Test") },
                backgroundColor = MaterialTheme.colors.primaryVariant,
                actions = {
                    IconButton(onClick = { nav.push(SettingsView()) }, content = { Icon(Icons.Filled.Settings, null) })
                }
            )
        }) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(10.dp, 7.dp),
                content = {
                    items(anime.size) { i ->
                        Card(modifier = Modifier.padding(2.dp).aspectRatio(0.71F), onClick = {
                            nav.push(AnimeView(anime[i].name))
                        }) {
                            KamelImage(
                                lazyPainterResource(anime[i].coverURL),
                                contentDescription = "Anime",
                                modifier = Modifier.padding(2.dp),
                                contentScale = ContentScale.FillBounds
                            )
                        }
                    }
                }
            )
        }
    }
}