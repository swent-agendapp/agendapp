package com.android.sample.ui.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
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
import com.android.sample.ui.calendar.style.GridContentStyle
import com.android.sample.ui.calendar.style.defaultGridContentDimensions
import com.android.sample.ui.calendar.style.defaultGridContentStyle
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DayHeaderRow(
    columnWidth: Dp = defaultGridContentDimensions().defaultColumnWidthDp,
    days: List<LocalDate> = run {
      val today = LocalDate.now()
      val startOfWeek = today.with(DayOfWeek.MONDAY)
      val endOfWeek = today.with(DayOfWeek.FRIDAY)
      generateSequence(startOfWeek) { it.plusDays(1) }.takeWhile { !it.isAfter(endOfWeek) }.toList()
    },
    leftOffsetDp: Dp = defaultGridContentStyle().dimensions.leftOffsetDp,
    topOffsetDp: Dp = defaultGridContentStyle().dimensions.topOffsetDp,
    style: GridContentStyle = defaultGridContentStyle(),
) {
  Row(modifier = Modifier.testTag(CalendarScreenTestTags.DAY_ROW)) {
    Box(modifier = Modifier.size(leftOffsetDp, topOffsetDp))

    days.forEach { date ->
      val isToday = date == LocalDate.now()
      val bg =
          if (isToday) {
            style.colors.currentDayBackground
          } else {
            Color.Transparent
          }

      val color =
          if (isToday) {
            style.colors.currentDayText
          } else {
            style.colors.dayHeaderText
          }

      val weight = if (isToday) FontWeight.Bold else FontWeight.Medium

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
