package moe.styx.views.anime.tabs

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.runtime.Composable
import moe.styx.common.compose.components.schedule.ScheduleDay
import moe.styx.common.compose.extensions.SimpleTab
import moe.styx.common.compose.utils.LocalGlobalNavigator
import moe.styx.common.data.ScheduleWeekday
import moe.styx.logic.utils.pushMediaView

class ScheduleView : SimpleTab("Schedule", Icons.Default.CalendarViewWeek) {

    @Composable
    override fun Content() {
        val nav = LocalGlobalNavigator.current
        val days = ScheduleWeekday.entries.toTypedArray()
        LazyColumn {
            items(items = days, itemContent = { day ->
                ScheduleDay(day) { nav.pushMediaView(it) }
            })
        }
    }
}