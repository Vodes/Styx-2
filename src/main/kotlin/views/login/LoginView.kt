package views.login

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.delay
import views.anime.AnimeListView
import kotlin.random.Random

class LoginView : Screen {

    private val Countdown: MutableState<Int> = mutableStateOf(30);
    private val Code: MutableState<Int> = mutableStateOf(Random.nextInt(100000, 999999));

    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val count = remember { Countdown }
        val code = remember { Code }

        LaunchedEffect(Unit) {
            while (true) {
                Countdown.value--
                println(Countdown.value)
                delay(1000)
                if (Countdown.value < 2) {
                    Countdown.value = 30
                    Code.value = Random.nextInt(100000, 999999)
                }
            }
        }

        val progressAnimation by animateFloatAsState(
            count.value / 30F,
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
        )

        Box(modifier = Modifier.padding(10.dp)) {
            Column(Modifier.align(Alignment.TopCenter)) {
                Text(
                    "Device registration", Modifier.align(Alignment.CenterHorizontally)
                        .padding(0.dp, 0.dp, 0.dp, 24.dp)
                )

                Text("Code", Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 10.dp), fontSize = 18.sp)

                Text(
                    code.value.toString(),
                    Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 10.dp),
                    fontSize = 30.sp, fontWeight = FontWeight.Bold
                )

                CircularProgressIndicator(
                    progressAnimation,
                    Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 15.dp).fillMaxSize(.3F)
                )
            }

            Button(onClick = {
                nav.push(AnimeListView())
            }, Modifier.height(38.dp).align(Alignment.BottomCenter)) {
                Text("Open styx.moe")
            }
        }
    }
}