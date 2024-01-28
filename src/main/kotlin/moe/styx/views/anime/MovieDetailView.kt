package moe.styx.views.anime

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import java.util.*

class MovieDetailView(val ID: String) : Screen {

    private var sKey: String? = null

    private fun generateKey(): String {
        if (sKey == null)
            sKey = UUID.randomUUID().toString()
        return sKey as String
    }

    override val key: ScreenKey
        get() = generateKey()


    @Composable
    override fun Content() {

    }
}