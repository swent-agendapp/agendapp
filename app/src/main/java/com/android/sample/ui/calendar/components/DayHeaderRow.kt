package com.android.sample.ui.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.android.sample.ui.calendar.style.GridContentStyle
import com.android.sample.ui.calendar.style.defaultGridContentDimensions
import com.android.sample.ui.calendar.style.defaultGridContentStyle
import com.android.sample.ui.theme.Salmon
import com.android.sample.ui.theme.SpacingExtraSmall
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Row that displays the header of the calendar days.
 *
 * It shows the day name and a short date for each visible day, and can optionally:
 * - Highlight "today" in a different text style.
 * - Highlight a selected day with a different background.
 * - Handle clicks on each day, when [onDayClick] is not null.
 */
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
    today: LocalDate = LocalDate.now(),
    selectedDate: LocalDate? = null,
    onDayClick: ((LocalDate) -> Unit)? = null,
) {
  Row(modifier = Modifier.testTag(CalendarScreenTestTags.DAY_ROW)) {
    Box(modifier = Modifier.size(leftOffsetDp, topOffsetDp))

    days.forEach { date ->
      val isToday = date == today
      val isSelected = selectedDate != null && selectedDate == date

      // Background:
      // - Selected day uses a light primary container to show the "selected" state.
      // - Other days keep the transparent background.
      val bg =
          if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
          } else {
            Color.Transparent
          }

      // Text color:
      // - Today is Salmon to clearly show "today".
      // - Other days use the default header text color.
      val color =
          if (isToday) {
            Salmon
          } else {
            style.colors.dayHeaderText
          }

      // Today is extra bold, other days use a medium weight.
      val weight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium

      val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
      val shortDate = date.format(DateTimeFormatter.ofPattern("dd.MM"))

      // If onDayClick is not null, the header cell is clickable.
      val cellModifier =
          if (onDayClick != null) {
            Modifier.size(width = columnWidth, height = topOffsetDp).background(bg).clickable {
              onDayClick(date)
            }
          } else {
            Modifier.size(width = columnWidth, height = topOffsetDp).background(bg)
          }

      Column(
          modifier = cellModifier,
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
      ) {
        Text(
            text = dayName,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = color,
            fontSize = 16.sp,
            fontWeight = weight,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(SpacingExtraSmall))
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
