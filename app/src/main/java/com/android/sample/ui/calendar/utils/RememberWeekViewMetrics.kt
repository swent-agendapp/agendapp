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
    // TODO: leftOffsetDp, topOffsetDp, effectiveStart/End, timeLabels, visibleTimeSpan
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

    val totalHours = ChronoUnit.HOURS.between(startTime, endTime).toInt()
    val gridHeightDp = rowHeightDp * totalHours

    WeekViewMetrics(
        days = days,
        columnCount = columnCount,
        rowHeightDp = rowHeightDp,
        totalHours = totalHours,
        gridHeightDp = gridHeightDp,
    )
  }
}
