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
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.style.GridContentStyle
import com.android.sample.ui.calendar.style.defaultGridContentDimensions
import com.android.sample.ui.calendar.style.defaultGridContentStyle
import com.android.sample.ui.theme.CalendarPalette
import com.android.sample.ui.theme.FontSizeMedium
import com.android.sample.ui.theme.FontSizeMediumSmall
import com.android.sample.ui.theme.GeneralPalette
import com.android.sample.ui.theme.LineHeightLarge
import com.android.sample.ui.theme.LineHeightSmall
import com.android.sample.ui.theme.SpacingExtraSmall
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// Modularization assisted by AI

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

    days.forEachIndexed { index, date ->
      val isToday = date == today
      val isSelected = selectedDate != null && selectedDate == date

      DayHeaderCell(
          date = date,
          isToday = isToday,
          isSelected = isSelected,
          index = index,
          columnWidth = columnWidth,
          topOffsetDp = topOffsetDp,
          style = style,
          onDayClick = onDayClick,
      )
    }
  }
}

@Composable
private fun DayHeaderCell(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    index: Int,
    columnWidth: Dp,
    topOffsetDp: Dp,
    style: GridContentStyle,
    onDayClick: ((LocalDate) -> Unit)?,
) {
  // Background:
  // - Selected day uses a light primary container to show the "selected" state.
  // - Other days keep the transparent background.
  val backgroundColor = dayHeaderBackgroundColor(isSelected)

  // Text color:
  // - Today is Salmon to clearly show "today".
  // - Other days use the default header text color.
  val textColor = dayHeaderTextColor(isToday, style)

  // Today is extra bold, other days use a medium weight.
  val fontWeight = dayHeaderFontWeight(isToday)

  val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
  val shortDate = date.format(DateTimeFormatter.ofPattern("dd.MM"))

  // If onDayClick is not null, the header cell is clickable.
  val baseModifier =
      Modifier.size(width = columnWidth, height = topOffsetDp).background(backgroundColor).let {
          base ->
        if (onDayClick != null) {
          base.clickable { onDayClick(date) }
        } else {
          base
        }
      }

  val cellModifier = baseModifier.testTag(CalendarScreenTestTags.DAY_HEADER_DAY_PREFIX + index)

  Column(
      modifier = cellModifier,
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
  ) {
    Text(
        text = dayName,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = textColor,
        fontSize = FontSizeMedium,
        fontWeight = fontWeight,
        textAlign = TextAlign.Center,
        lineHeight = LineHeightLarge,
        modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(SpacingExtraSmall))
    Text(
        text = shortDate,
        maxLines = 1,
        color = textColor,
        fontSize = FontSizeMediumSmall,
        fontWeight = fontWeight,
        textAlign = TextAlign.Center,
        lineHeight = LineHeightSmall,
        modifier = Modifier.fillMaxWidth())
  }
}

// Returns the background color for a day header cell.
// Selected days use the primary container, others are transparent.
private fun dayHeaderBackgroundColor(isSelected: Boolean): Color {
  return if (isSelected) {
    CalendarPalette.currentDayBackground
  } else {
    Color.Transparent
  }
}

// Returns the text color for a day header cell.
// Today is highlighted with the primary color; other days use the header text color.
private fun dayHeaderTextColor(isToday: Boolean, style: GridContentStyle): Color {
  return if (isToday) {
    GeneralPalette.Primary
  } else {
    style.colors.dayHeaderText
  }
}

// Returns the font weight for a day header cell.
// Today is extra bold to stand out, other days use a medium weight.
private fun dayHeaderFontWeight(isToday: Boolean): FontWeight {
  return if (isToday) {
    FontWeight.ExtraBold
  } else {
    FontWeight.Medium
  }
}
