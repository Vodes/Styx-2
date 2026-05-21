package moe.styx.views.settings

import androidx.compose.runtime.Composable
import moe.styx.Styx_2.BuildConfig
import moe.styx.common.compose.components.about.AboutViewComponent
import moe.styx.common.compose.components.layout.MainScaffold
import moe.styx.common.compose.navigation.Screen

class AboutView : Screen {

    @Composable
    override fun Content() {
        MainScaffold(title = "About") {
            AboutViewComponent(BuildConfig.APP_NAME)
        }
    }
}
