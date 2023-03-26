package moe.styx.moe.styx.views.anime.tabs

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import moe.styx.components.ScheduleDay
import moe.styx.logic.data.ScheduleWeekday
import moe.styx.moe.styx.components.createTabOptions

class ScheduleView : Tab {
    override val options: TabOptions
        @Composable
        get() {
            return createTabOptions("Schedule", Icons.Default.CalendarViewWeek)
        }

    @Composable
    override fun Content() {
        val days = ScheduleWeekday.values()
        LazyColumn {
            items(items = days, itemContent = { day ->
                ScheduleDay(day)
            })
        }
    }
}