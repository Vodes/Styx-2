package moe.styx.moe.styx.views.anime

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource
import moe.styx.dataManager
import moe.styx.moe.styx.components.*
import moe.styx.moe.styx.logic.data.getFile
import moe.styx.moe.styx.logic.data.getImageFromID
import moe.styx.moe.styx.logic.data.getURL
import moe.styx.moe.styx.logic.data.isCached
import moe.styx.moe.styx.navigation.LocalGlobalNavigator
import java.util.*

class AnimeDetailView(val ID: String) : Screen {

    private var sKey: String? = null

    private fun generateKey(): String {
        if (sKey == null)
            sKey = UUID.randomUUID().toString()
        return sKey as String
    }

    override val key: ScreenKey
        get() = ID

    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
    @Composable
    override fun Content() {
        val scaffoldState = rememberScaffoldState()
        val nav = LocalGlobalNavigator.current
        val anime = dataManager.media.value.find { a -> a.GUID == ID }

        if (anime == null) {
            nav.pop();
            return
        }
        val episodes =
            dataManager.entries.value.filter { it.mediaID == anime.GUID }.sortedByDescending { it.entryNumber }

        val scrollState = rememberScrollState()
        val showSelection = remember { mutableStateOf(false) }

        MainScaffold(scaffoldState, title = anime.name) {
            Card(Modifier.padding(8.dp).fillMaxSize(), elevation = 9.dp) {
                Row(Modifier.padding(5.dp).fillMaxSize()) {
                    Column(Modifier.fillMaxHeight().fillMaxWidth(.55F).verticalScroll(scrollState)) {
                        Row(Modifier.align(Alignment.Start).fillMaxWidth().requiredHeightIn(150.dp, 500.dp)) {
                            Card(
                                Modifier.align(Alignment.Top).padding(12.dp).wrapContentWidth().fillMaxWidth(0.5F)
                                    .wrapContentSize().aspectRatio(0.71F),
                                elevation = 2.dp
                            ) {
                                val img = anime.thumbID.getImageFromID()!!
                                KamelImage(
                                    lazyPainterResource(
                                        if (img.isCached()) img.getFile() else img.getURL(),
                                        filterQuality = FilterQuality.High
                                    ),
                                    contentDescription = "Anime",
                                    modifier = Modifier.padding(2.dp).clip(RoundedCornerShape(4.dp)).fillMaxSize()
                                        .aspectRatio(0.71F),
                                    contentScale = ContentScale.FillBounds
                                )
                            }
                            SelectionContainer {
                                MediaNameListing(anime)
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        Text("About", Modifier.padding(6.dp, 2.dp), style = MaterialTheme.typography.h6)
                        MediaGenreListing(anime)
                        if (!anime.synopsisEN.isNullOrBlank())
                            SelectionContainer {
                                Text(anime.synopsisEN, Modifier.padding(6.dp), style = MaterialTheme.typography.caption)
                            }

                        if (anime.sequel != null || anime.prequel != null) {
                            Divider(Modifier.fillMaxWidth().padding(0.dp, 4.dp, 0.dp, 2.dp), thickness = 3.dp)
                            MediaRelations(anime)
                        }
                    }
                    Column(Modifier.padding(6.dp).width(2.dp).fillMaxHeight()) {
                        Divider(Modifier.fillMaxHeight(), thickness = 3.dp)
                    }

                    EpisodeList(episodes, showSelection)
                }
            }
        }
    }
}