package com.android.sample.ui.calendar.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.data.LocalDateRange
import com.android.sample.ui.calendar.data.workWeekDays
import com.android.sample.ui.calendar.style.CalendarDefaults.DefaultDaysInWeek
import com.android.sample.ui.calendar.style.CalendarDefaults.DefaultTotalHour
import com.android.sample.ui.calendar.style.CalendarDefaults.strokeWidthDefault
import com.android.sample.ui.calendar.style.GridContentStyle
import com.android.sample.ui.calendar.style.defaultGridContentStyle
import com.android.sample.ui.calendar.utils.rememberWeekViewMetrics
import java.time.LocalDate

@Composable
fun GridCanvas(
    modifier: Modifier = Modifier,
    columnCount: Int = DefaultDaysInWeek,
    rowHeightDp: Dp = defaultGridContentStyle().dimensions.rowHeightDp,
    totalHours: Int = DefaultTotalHour,
    days: List<LocalDate> = workWeekDays(),
    style: GridContentStyle = defaultGridContentStyle(),
) {

  val range = LocalDateRange(days.first(), days.last())
  val metrics = rememberWeekViewMetrics(dateRange = range)

  Canvas(
      modifier =
          modifier
              .fillMaxWidth()
              .height(metrics.gridHeightDp)
              .testTag(CalendarScreenTestTags.EVENT_GRID)) {
        val columnWidthPx = if (columnCount > 0) size.width / columnCount else size.width
        val rowHeightPx = rowHeightDp.toPx()

        // Vertical lines (day columns)
        for (i in 0..columnCount) {
          val x = i * columnWidthPx
          drawLine(
              color = style.colors.gridLineColor,
              start = Offset(x, 0f),
              end = Offset(x, size.height),
              strokeWidth = strokeWidthDefault)
        }

        // Horizontal lines (hours) - full width
        val hourLineCount = totalHours
        for (i in 0..hourLineCount) {
          val y = i * rowHeightPx
          drawLine(
              color = style.colors.gridLineColor,
              start = Offset(0f, y),
              end = Offset(size.width, y),
              strokeWidth = strokeWidthDefault)
        }

        // Today highlight
        val today = LocalDate.now()
        val todayIndex = days.indexOf(today)
        if (todayIndex in 0 until columnCount) {
          val left = todayIndex * columnWidthPx
          drawRect(
              color = style.colors.todayHighlight,
              topLeft = Offset(left, 0f),
              size = Size(columnWidthPx, size.height))
        }
      }
}
