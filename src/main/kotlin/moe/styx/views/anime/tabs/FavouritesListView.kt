package moe.styx.moe.styx.views.anime.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import moe.styx.moe.styx.components.createTabOptions

class FavouritesListView() : Tab {
    override val options: TabOptions
        @Composable
        get() {
            return createTabOptions("Favourites", Icons.Default.Star)
        }

    @Composable
    override fun Content() {
        Column {
            Text("lulw")
        }
    }
}