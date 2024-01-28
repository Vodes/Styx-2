package moe.styx.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.styx.components.anime.AnimeListItem
import moe.styx.logic.data.DataManager
import moe.styx.logic.utils.dayOfWeek
import moe.styx.logic.utils.getTargetTime
import moe.styx.logic.utils.makeFirstLetterBig
import moe.styx.navigation.LocalGlobalNavigator
import moe.styx.types.ScheduleWeekday
import java.time.format.DateTimeFormatter
import java.util.*


@Composable
fun ScheduleDay(day: ScheduleWeekday) {
    val schedules = DataManager.schedules.value.filter { it.getTargetTime().dayOfWeek == day.dayOfWeek() }
    if (schedules.isEmpty())
        return
    Column(Modifier.padding(2.dp, 6.dp)) {
        Text(
            day.name.lowercase().makeFirstLetterBig(),
            modifier = Modifier.padding(2.dp),
            style = MaterialTheme.typography.titleLarge
        )
        for (schedule in schedules) {
            val media = DataManager.media.value.find { it.GUID == schedule.mediaID } ?: continue
            val target = schedule.getTargetTime()
            Text(
                target.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())),
                modifier = Modifier.padding(6.dp),
                style = MaterialTheme.typography.titleMedium
            )
            Column(Modifier.padding(6.dp, 1.dp)) { AnimeListItem(LocalGlobalNavigator.current, media) }
        }
    }
}