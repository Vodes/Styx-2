package moe.styx.views.settings

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import moe.styx.Styx_2.BuildConfig
import moe.styx.common.compose.components.about.AboutViewComponent
import moe.styx.common.compose.components.layout.MainScaffold

class AboutView : Screen {

    @Composable
    override fun Content() {
        MainScaffold(title = "About") {
            AboutViewComponent(BuildConfig.APP_NAME)
        }
    }
}