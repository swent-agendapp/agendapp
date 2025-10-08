package com.android.sample.ui.calendar.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.style.GridContentStyle
import com.android.sample.ui.calendar.style.defaultGridContentStyle
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

@Composable
fun GridCanvas(
    modifier: Modifier = Modifier,
    columnCount: Int,
    rowHeightDp: Dp,
    totalHours: Float,
    days: List<LocalDate>,
    now: LocalTime,
    gridStartTime: LocalTime,
    effectiveEndTime: LocalTime,
    style: GridContentStyle = defaultGridContentStyle(),
) {
  Canvas(modifier = modifier.testTag(CalendarScreenTestTags.EVENT_GRID)) {
    val columnWidthPx = if (columnCount > 0) size.width / columnCount else size.width
    val rowHeightPx = rowHeightDp.toPx()

    // Vertical lines (day columns)
    for (i in 0..columnCount) {
      val x = i * columnWidthPx
      drawLine(
          color = style.colors.gridLineColor,
          start = Offset(x, 0f),
          end = Offset(x, size.height),
          strokeWidth = 2f)
    }

    // Horizontal lines (hours) - full width
    val hourLineCount = ceil(totalHours).toInt()
    for (i in 0..hourLineCount) {
      val y = i * rowHeightPx
      drawLine(
          color = style.colors.gridLineColor,
          start = Offset(0f, y),
          end = Offset(size.width, y),
          strokeWidth = 2f)
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
