package moe.styx.views.anime.tabs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.runtime.Composable
import moe.styx.common.compose.components.schedule.ScheduleViewComponent
import moe.styx.common.compose.extensions.SimpleTab
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.logic.utils.pushMediaView

class ScheduleView : SimpleTab("Schedule", Icons.Default.CalendarViewWeek) {

    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current
        ScheduleViewComponent {
            nav.pushMediaView(it, false)
        }
    }
}