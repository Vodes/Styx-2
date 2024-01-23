package moe.styx.views.login

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.delay
import moe.styx.logic.login.checkLogin
import moe.styx.logic.login.generateCode
import moe.styx.logic.login.isLoggedIn
import moe.styx.moe.styx.navigation.LocalGlobalNavigator
import moe.styx.moe.styx.views.other.LoadingView
import moe.styx.types.CreationResponse

class LoginView() : Screen {

    private val creationResponse: MutableState<CreationResponse> = mutableStateOf(generateCode())
    private val countdown: MutableState<Int> = mutableStateOf(30);

    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current

        LaunchedEffect(Unit) {
            while (!isLoggedIn()) {
                countdown.value--
                val log = checkLogin(creationResponse.value.GUID, true)
                if (log != null) {
                    nav.push(LoadingView())
                    break
                }

                delay(1000)
                if (countdown.value < 2) {
                    countdown.value = 30
                    creationResponse.value = generateCode()
                }
            }
        }

        val count = remember { countdown }
        val resp = remember { creationResponse }

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
                    resp.value.code.toString(),
                    Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 10.dp),
                    fontSize = 30.sp, fontWeight = FontWeight.Bold
                )

                CircularProgressIndicator(
                    progressAnimation,
                    Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 15.dp).fillMaxSize(.3F)
                )
            }

            Button(onClick = {
                println("Penis")
            }, Modifier.height(38.dp).align(Alignment.BottomCenter)) {
                Text("Open styx.moe")
            }
        }
    }
}