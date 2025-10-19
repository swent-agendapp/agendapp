package com.android.sample.ui.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.data.LocalDateRange
import com.android.sample.ui.calendar.style.CalendarDefaults.DefaultDateRange
import com.android.sample.ui.calendar.style.GridContentStyle
import com.android.sample.ui.calendar.style.defaultGridContentDimensions
import com.android.sample.ui.calendar.style.defaultGridContentStyle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * # DayHeaderRow
 * This file defines the header row of the calendar grid displaying the days of the week and their
 * corresponding dates.
 *
 * Each column represents one day in the current date range. The header adapts its visual style
 * depending on the current day and whether a single-day view is active.
 */

/**
 * Displays the header row containing abbreviated weekday names and dates.
 * - When a single day is selected, that column is highlighted using the primary container color.
 * - The current day is visually emphasized with a different text and background color.
 * - All other days are displayed with neutral styling.
 *
 * @param columnWidth The width of each day column.
 * @param dateRange The inclusive date range of the days to display in the header.
 * @param singleDay Optional selected day for single-day view highlighting.
 * @param leftOffsetDp Left offset width to align the day headers with the grid.
 * @param topOffsetDp Top offset height used to align with the grid’s content.
 * @param style The visual styling configuration for the grid content.
 */
@Composable
fun DayHeaderRow(
    columnWidth: Dp = defaultGridContentDimensions().defaultColumnWidthDp,
    dateRange: LocalDateRange = DefaultDateRange,
    singleDay: LocalDate? = null,
    leftOffsetDp: Dp = defaultGridContentStyle().dimensions.leftOffsetDp,
    topOffsetDp: Dp = defaultGridContentStyle().dimensions.topOffsetDp,
    style: GridContentStyle = defaultGridContentStyle(),
) {
  val days: List<LocalDate> =
      generateSequence(dateRange.start) { it.plusDays(1) }
          .takeWhile { !it.isAfter(dateRange.endInclusive) }
          .toList()

  Row(modifier = Modifier.fillMaxWidth().testTag(CalendarScreenTestTags.DAY_ROW)) {
    Box(modifier = Modifier.size(leftOffsetDp, topOffsetDp))

    days.forEach { date ->
      val isToday = date == LocalDate.now()
      val isShownSingleDay = singleDay?.let { it == date } ?: false

      val (bg, color, weight) =
          when {
            isShownSingleDay -> {
              val bgc = MaterialTheme.colorScheme.primaryContainer
              val tc = MaterialTheme.colorScheme.onPrimaryContainer
              Triple(bgc, tc, FontWeight.Bold)
            }
            isToday -> {
              Triple(
                  style.colors.currentDayBackground,
                  style.colors.currentDayText,
                  FontWeight.ExtraBold)
            }
            else -> Triple(Color.Transparent, style.colors.dayHeaderText, FontWeight.Medium)
          }

      val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

      val shortDate = date.format(DateTimeFormatter.ofPattern("dd.MM"))

      Column(
          modifier = Modifier.size(columnWidth, topOffsetDp).background(bg),
          horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = color,
                fontSize = 13.sp,
                fontWeight = weight,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.fillMaxWidth())
            Text(
                text = shortDate,
                maxLines = 1,
                color = color,
                fontSize = 13.sp,
                fontWeight = weight,
                textAlign = TextAlign.Center,
                lineHeight = 8.sp,
                modifier = Modifier.fillMaxWidth())
          }
    }
  }
}
