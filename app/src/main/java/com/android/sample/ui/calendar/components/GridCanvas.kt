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
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun GridCanvas(
    modifier: Modifier = Modifier,
    columnCount: Int = 5,
    rowHeightDp: Dp = defaultGridContentStyle().dimensions.rowHeightDp,
    totalHours: Int = 15,
    days: List<LocalDate> = run {
      val today = LocalDate.now()
      val startOfWeek = today.with(DayOfWeek.MONDAY)
      val endOfWeek = today.with(DayOfWeek.FRIDAY)
      generateSequence(startOfWeek) { it.plusDays(1) }.takeWhile { it <= endOfWeek }.toList()
    },
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
    val hourLineCount = totalHours
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
  }
}
