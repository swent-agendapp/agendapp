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
import com.android.sample.ui.calendar.style.CalendarDefaults.DEFAULT_DAYS_IN_WEEK
import com.android.sample.ui.calendar.style.CalendarDefaults.DefaultTotalHour
import com.android.sample.ui.calendar.style.CalendarDefaults.STROKE_WIDTH_DEFAULT
import com.android.sample.ui.calendar.style.GridContentStyle
import com.android.sample.ui.calendar.style.defaultGridContentStyle
import com.android.sample.ui.calendar.utils.rememberWeekViewMetrics
import java.time.LocalDate

/**
 * Paints the background grid for the week view: vertical day separators, horizontal hour lines, and
 * a soft highlight for today's column. No events are drawn here.
 *
 * @param modifier [Modifier] applied to the canvas container.
 * @param columnCount Number of day columns to draw.
 * @param rowHeightDp Height per hour row, in [Dp].
 * @param totalHours Number of hour rows to render (e.g., 24 or a clipped range).
 * @param days Ordered list of days corresponding to the columns.
 * @param style Visual style (colors, stroke widths) for grid elements.
 * @return Unit. This is a composable that renders UI side-effects only.
 */
@Composable
fun GridCanvas(
    modifier: Modifier = Modifier,
    columnCount: Int = DEFAULT_DAYS_IN_WEEK,
    rowHeightDp: Dp = defaultGridContentStyle().dimensions.rowHeightDp,
    totalHours: Int = DefaultTotalHour,
    days: List<LocalDate> = workWeekDays(),
    today: LocalDate = LocalDate.now(),
    selectedDate: LocalDate? = null,
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
              strokeWidth = STROKE_WIDTH_DEFAULT)
        }

        // Horizontal lines (hours) - full width
        val hourLineCount = totalHours
        for (i in 0..hourLineCount) {
          val y = i * rowHeightPx
          drawLine(
              color = style.colors.gridLineColor,
              start = Offset(0f, y),
              end = Offset(size.width, y),
              strokeWidth = STROKE_WIDTH_DEFAULT)
        }

        // Today highlight
        val todayIndex = days.indexOf(today)
        if (todayIndex in 0 until columnCount && selectedDate == null) {
          val left = todayIndex * columnWidthPx
          drawRect(
              color = style.colors.todayHighlight,
              topLeft = Offset(left, 0f),
              size = Size(columnWidthPx, size.height))
        }
      }
}
