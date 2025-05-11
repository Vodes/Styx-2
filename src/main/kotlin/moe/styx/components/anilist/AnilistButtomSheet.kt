package moe.styx.components.anilist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.styx.common.data.Media

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnilistButtomSheet(media: Media, sheetModel: AnilistBottomSheetModel, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    LaunchedEffect(sheetModel.viewer, media.GUID) {
        sheetModel.fetchMediaState(media)
    }
    val anilistData by sheetModel.anilistData.collectAsState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (anilistData == null) {
                Text("Loading anilist data...", style = MaterialTheme.typography.headlineMedium)
            } else if (anilistData?.errored == true) {
                Text("Failed to load anilist data! Please send the logs to the admin.", style = MaterialTheme.typography.headlineMedium)
            } else {
                val mapped =
                    anilistData?.alMedia?.map { media -> media to anilistData?.userMedia?.find { it.media.id == media.id } }
                if (mapped != null) {
                    mapped.forEach { mappedMedia ->
                        AnilistMediaComponent(media, sheetModel.viewer, mappedMedia.first, mappedMedia.second)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}