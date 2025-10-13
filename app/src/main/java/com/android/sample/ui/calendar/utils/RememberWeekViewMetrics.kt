package com.android.sample.ui.calendar.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@Immutable
data class WeekViewMetrics(
    val days: List<LocalDate>,
    val columnCount: Int,
    val rowHeightDp: Dp,
    val totalHours: Int,
    val gridHeightDp: Dp,
    val leftOffsetDp: Dp,
    val timeLabels: List<LocalTime>,
    val visibleTimeSpan: TimeSpan,
    val gridStartTime: LocalTime,
    val effectiveEndTime: LocalTime,

    // TODO: topOffsetDp, effective start, timeLabels, visibleTimeSpan
)

@Composable
internal fun rememberWeekViewMetrics(
    dateRange: LocalDateRange,
): WeekViewMetrics {
  return remember(dateRange) {
    val days = dateRange.toList()
    val columnCount = days.size

    val startTime = LocalTime.of(8, 0)
    val endTime = LocalTime.of(23, 0)
    val rowHeightDp = 60.dp
    val leftOffsetDp = 48.dp

    val totalHours = ChronoUnit.HOURS.between(startTime, endTime).toInt()
    val gridHeightDp = rowHeightDp * totalHours

    val gridStartTime = startTime.truncatedTo(ChronoUnit.HOURS)
    val visibleTimeSpan = TimeSpan(gridStartTime, endTime)
    val timeLabels = visibleTimeSpan.hourlyTimes().toList()

    WeekViewMetrics(
        days = days,
        columnCount = columnCount,
        rowHeightDp = rowHeightDp,
        totalHours = totalHours,
        gridHeightDp = gridHeightDp,
        leftOffsetDp = leftOffsetDp,
        timeLabels = timeLabels,
        visibleTimeSpan = visibleTimeSpan,
        gridStartTime = gridStartTime,
        effectiveEndTime = endTime,
    )
  }
}
