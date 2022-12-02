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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import logic.data.get_anime

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun animeListView(onAnimeClick: (String) -> Unit){
    val anime = get_anime().reversed();
    val scaffoldState = rememberScaffoldState()
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    Scaffold (scaffoldState=scaffoldState, topBar = {
        TopAppBar (
            title={ Text("Styx Test") },
            backgroundColor = MaterialTheme.colors.primaryVariant,
            actions = {
                IconButton(onClick = {
                    coroutineScope.launch {
                        scaffoldState.snackbarHostState.showSnackbar("Settings", "NE")
                    }
                }, content={ Icon(Icons.Filled.Settings, null) })
            }
        )
    }) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(10.dp, 7.dp),
            content = {
                items(anime.size) { i ->
                    Card(modifier= Modifier.padding(2.dp).aspectRatio(0.71F), onClick = {
                        onAnimeClick(anime[i].name)
                    }){
                        KamelImage(
                            lazyPainterResource(anime[i].coverURL),
                            contentDescription = "Anime",
                            modifier= Modifier.padding(2.dp),
                            contentScale = ContentScale.FillBounds)
                    }
                }
            }
        )
    }
}