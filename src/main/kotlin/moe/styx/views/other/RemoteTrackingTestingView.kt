package moe.styx.views.other

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import moe.styx.common.compose.components.layout.MainScaffold
import moe.styx.common.compose.components.tracking.common.CommonMediaListStatus
import moe.styx.common.compose.components.tracking.common.CommonMediaStatus
import moe.styx.common.compose.components.tracking.common.RemoteMediaComponent
import pw.vodes.anilistkmp.graphql.type.ScoreFormat
import kotlin.random.Random

class RemoteTrackingTestingView : Screen {

    @Composable
    override fun Content() {
        MainScaffold(Modifier.fillMaxSize(), "Tracking Playground") {
            Column {
                ShowRemoteComp(ScoreFormat.POINT_10, 7F)
                ShowRemoteComp(ScoreFormat.POINT_100, 70F)
                ShowRemoteComp(ScoreFormat.POINT_10_DECIMAL, 7.6F)
                ShowRemoteComp(ScoreFormat.POINT_5, 3F)
                ShowRemoteComp(ScoreFormat.POINT_3, 2F)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ShowRemoteComp(scoreFormat: ScoreFormat, score: Float) {
        var status by remember {
            mutableStateOf(
                CommonMediaStatus(
                    entryID = 305127977,
                    mediaID = 156822,
                    status = CommonMediaListStatus.WATCHING,
                    progress = Random.nextInt(24),
                    knownMax = 24,
                    score = score
                )
            )
        }
        var showBottomSheet by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState()
        Button({
            showBottomSheet = true
        }) {
            Text("Show sheet for: ${scoreFormat.name}")
        }
        if (showBottomSheet) {
            ModalBottomSheet({ showBottomSheet = false }, sheetState = sheetState) {
                RemoteMediaComponent(
                    "That Time I Got Reincarnated as a Slime Season 3",
                    "https://s4.anilist.co/file/anilistcdn/media/anime/cover/medium/bx156822-Jzo2ITWgm4kM.jpg",
                    "",
                    true,
                    true,
                    scoreFormat,
                    status
                ) {
                    status = it
                }
            }
        }
    }
}