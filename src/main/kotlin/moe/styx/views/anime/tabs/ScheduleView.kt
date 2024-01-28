package moe.styx.views.anime.tabs

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import moe.styx.components.ScheduleDay
import moe.styx.components.misc.createTabOptions
import moe.styx.types.ScheduleWeekday

class ScheduleView : Tab {
    override val options: TabOptions
        @Composable
        get() {
            return createTabOptions("Schedule", Icons.Default.CalendarViewWeek)
        }

    @Composable
    override fun Content() {
        val days = ScheduleWeekday.entries.toTypedArray()
        LazyColumn {
            items(items = days, itemContent = { day ->
                ScheduleDay(day)
            })
        }
    }
}