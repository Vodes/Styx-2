package moe.styx.moe.styx.views.other

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.styx.dataManager
import moe.styx.moe.styx.navigation.LocalGlobalNavigator
import moe.styx.moe.styx.views.anime.AnimeOverview

class LoadingView() : Screen {
    @Composable
    override fun Content() {
        val scaffoldState = rememberScaffoldState()
        val nav = LocalGlobalNavigator.current
        val coroutineScope = rememberCoroutineScope()
        val progress = remember { mutableStateOf("") }

        coroutineScope.launch {
            launch { dataManager.load { progress.value = it } }
            while (!dataManager.isLoaded.value) {
                println("Waiting...")
                delay(500)
            }
            nav.replaceAll(AnimeOverview())
        }

        Scaffold(scaffoldState = scaffoldState, topBar = {
            TopAppBar(
                title = { Text("Loading") },
                backgroundColor = MaterialTheme.colors.secondary,
            )
        }) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.padding(10.dp).align(Alignment.TopCenter)) {
                    Text(
                        text = progress.value,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 25.dp)
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 15.dp).fillMaxSize(.4F)
                    )
                }
            }
        }
    }
}