package com.android.sample.ui.calendar.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.sample.ui.calendar.data.LocalDateRange
import com.android.sample.ui.calendar.data.TimeSpan
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * Immutable layout metrics used to render the week-view grid and overlays.
 *
 * @property days Ordered list of days (columns) displayed in the grid.
 * @property columnCount Number of day columns (usually days.size).
 * @property rowHeightDp Logical height of one hour row, in [Dp].
 * @property totalHours Total number of visible hour rows.
 * @property gridHeightDp Total scrollable canvas height (rowHeightDp × totalHours).
 * @property leftOffsetDp Width reserved for the time-axis column.
 * @property timeLabels Hour tick labels aligned with each row.
 * @property visibleTimeSpan The inclusive/exclusive time span covered by the grid.
 * @property gridStartTime Start of the visible window, truncated to the hour.
 * @property effectiveEndTime Exclusive end of the visible window (often equals DefaultEndTime).
 */
@Immutable
data class WeekViewMetrics(
    val days: List<LocalDate>,
    val columnCount: Int,
    val rowHeightDp: Dp,
    val totalHours: Int,
    val gridHeightDp: Dp,
    val leftOffsetDp: Dp,
    val topOffsetDp: Dp,
    val timeLabels: List<LocalTime>,
    val visibleTimeSpan: TimeSpan,
    val gridStartTime: LocalTime,
    val effectiveEndTime: LocalTime,

    // later : topOffsetDp, effective start
)

/**
 * Computes and memoizes [WeekViewMetrics] derived from a [LocalDateRange]. Values are recomputed
 * only when [dateRange] changes.
 *
 * @param dateRange Visible inclusive date range to generate columns for.
 * @return A stable [WeekViewMetrics] instance describing grid geometry and labels.
 */
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
    val topOffsetDp = 42.dp

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
        topOffsetDp = topOffsetDp,
        timeLabels = timeLabels,
        visibleTimeSpan = visibleTimeSpan,
        gridStartTime = gridStartTime,
        effectiveEndTime = endTime,
    )
  }
}
