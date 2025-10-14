package com.android.sample.ui.calendar.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.style.CalendarDefaults
import com.android.sample.ui.calendar.style.GridContentStyle
import com.android.sample.ui.calendar.style.defaultGridContentStyle
import com.android.sample.ui.calendar.utils.LocalDateRange
import com.android.sample.ui.calendar.utils.rememberWeekViewMetrics
import com.android.sample.ui.calendar.utils.workWeekDays
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.ranges.rangeTo
import kotlin.ranges.until

@Composable
fun NowIndicatorLine(
    modifier: Modifier = Modifier,
    columnCount: Int = CalendarDefaults.DefaultDaysInWeek,
    rowHeightDp: Dp = defaultGridContentStyle().dimensions.rowHeightDp,
    days: List<LocalDate> = workWeekDays(),
    now: LocalTime = LocalTime.now(),
    gridStartTime: LocalTime = CalendarDefaults.DefaultStartTime,
    effectiveEndTime: LocalTime = CalendarDefaults.DefaultEndTime,
    style: GridContentStyle = defaultGridContentStyle(),
) {
  val range = LocalDateRange(days.first(), days.last())
  val metrics = rememberWeekViewMetrics(dateRange = range)

  Canvas(
      modifier =
          modifier.height(metrics.gridHeightDp).testTag(CalendarScreenTestTags.NOW_INDICATOR)) {
        val today = LocalDate.now()
        val todayIndex = days.indexOf(today)

        val columnWidthPx = if (columnCount > 0) size.width / columnCount else size.width
        val rowHeightPx = rowHeightDp.toPx()

        // Now indicator line
        if (now.isAfter(gridStartTime) &&
            now.isBefore(effectiveEndTime) &&
            todayIndex in 0 until columnCount) {
          val minutesFromStart = ChronoUnit.MINUTES.between(gridStartTime, now)
          val nowY = (minutesFromStart / 60f) * rowHeightPx
          if (nowY in 0f..size.height) {
            val left = todayIndex * columnWidthPx
            val right = left + columnWidthPx
            drawLine(
                color = style.colors.nowIndicator,
                start = Offset(left, nowY),
                end = Offset(right, nowY),
                strokeWidth = 4f)
            drawCircle(color = style.colors.nowIndicator, radius = 8f, center = Offset(left, nowY))
          }
        }
      }
}
