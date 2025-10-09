package com.android.sample.ui.calendar.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.android.sample.ui.calendar.mockData.MockEvent
import java.time.LocalTime
import java.time.temporal.ChronoUnit

@Composable
internal fun rememberWeekViewMetrics(
    dateRange: LocalDateRange,
    events: List<MockEvent>,
): WeekViewMetrics {
  return remember(dateRange, events) {
    val days = dateRange.toList()
    val columnCount = days.size
    val leftOffsetDp = 48.dp
    val topOffsetDp = 42.dp

    // Later : adapt to the admin's choice
    val startTime = LocalTime.of(8, 0)
    val endTime = LocalTime.of(23, 0)

    val rowHeightDp = 60.dp // for zoom, add “ * scalingFactor “

    val gridStartTime = startTime.truncatedTo(ChronoUnit.HOURS)
    val visibleTimeSpan = TimeSpan(gridStartTime, endTime)

    val totalHours = visibleTimeSpan.duration.toHours().toInt()
    val gridHeightDp = rowHeightDp * totalHours
    val timeLabels = visibleTimeSpan.hourlyTimes().toList()

    WeekViewMetrics(
        days = days,
        columnCount = columnCount,
        leftOffsetDp = leftOffsetDp,
        topOffsetDp = topOffsetDp,
        effectiveStartTime = startTime,
        effectiveEndTime = endTime,
        gridStartTime = gridStartTime,
        rowHeightDp = rowHeightDp,
        totalHours = totalHours,
        gridHeightDp = gridHeightDp,
        timeLabels = timeLabels,
        visibleTimeSpan = visibleTimeSpan,
    )
  }
}
