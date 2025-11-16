package com.android.sample.ui.calendar.components.eventSummaryComponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.sample.ui.calendar.components.EventSummaryCardTags
import com.android.sample.ui.calendar.utils.DatePresentation
import com.android.sample.ui.calendar.utils.NO_DATA_DEFAULT_VALUE
import com.android.sample.ui.theme.AlphaHigh
import com.android.sample.ui.theme.IconSizeMedium
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.SpacingExtraLarge
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.SpacingSmall
import java.time.ZonedDateTime

/**
 * Renders the date/time area of the Event Summary.
 * - Single-day events: two simple rows : date then time.
 * - Multi-day events: three aligned columns to avoid ambiguity:
 *     1) labels (“From” / “To”) with a calendar icon on the first row,
 *     2) start/end dates,
 *     3) start/end times prefixed with "at ".
 *
 * Formatting is delegated to [DatePresentation] so this composable remains layout-only.
 *
 * @param model Pre-formatted strings and flags produced by `DatePresentation`.
 */
@Composable
fun DateSection(
    model: DatePresentation =
        DatePresentation(
            isMultiDay = false,
            dateLine1 = NO_DATA_DEFAULT_VALUE,
            dateLine2 = NO_DATA_DEFAULT_VALUE,
            startDateShort = NO_DATA_DEFAULT_VALUE,
            endDateShort = NO_DATA_DEFAULT_VALUE,
            startTimeStr = NO_DATA_DEFAULT_VALUE,
            endTimeStr = NO_DATA_DEFAULT_VALUE,
            startZdt = ZonedDateTime.now(),
            endZdt = ZonedDateTime.now())
) {
  // Three aligned columns => predictable scan: labels -> dates -> times
  if (model.isMultiDay) {
    // === Structure for multi-day ===
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      // First row shows calendar icon + "From"; second row aligns "To" text without icon for
      // balance
      Column(modifier = Modifier.padding(end = PaddingMedium)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
              imageVector = Icons.Filled.DateRange,
              contentDescription = "From date",
              modifier = Modifier.size(IconSizeMedium),
              tint = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaHigh))
          Spacer(Modifier.width(SpacingSmall))
          Text(
              text = "From",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaHigh),
              modifier = Modifier.testTag(EventSummaryCardTags.MULTI_FROM_LABEL))
        }
        Spacer(Modifier.height(SpacingSmall))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Spacer(Modifier.width(SpacingExtraLarge))
          Text(
              text = "To",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaHigh),
              modifier = Modifier.testTag(EventSummaryCardTags.MULTI_TO_LABEL))
        }
      }

      // Column 2: Start/End dates
      Column {
        Text(
            text = model.startDateShort,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaHigh),
            modifier = Modifier.testTag(EventSummaryCardTags.MULTI_START_DATE))
        Spacer(Modifier.height(SpacingSmall))
        Text(
            text = model.endDateShort,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaHigh),
            modifier = Modifier.testTag(EventSummaryCardTags.MULTI_END_DATE))
      }

      // Times are prefixed with "at " to read naturally next to the dates
      Column(horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Spacer(Modifier.width(SpacingSmall))
          Text(
              text = "at " + model.startTimeStr,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaHigh),
              modifier = Modifier.testTag(EventSummaryCardTags.MULTI_START_TIME))
        }
        Spacer(Modifier.height(SpacingSmall))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Spacer(Modifier.width(SpacingSmall))
          Text(
              text = "at " + model.endTimeStr,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaHigh),
              modifier = Modifier.testTag(EventSummaryCardTags.MULTI_END_TIME))
        }
      }
    }
  } else {
    // === Structure for single-day ===
    Column(modifier = Modifier.fillMaxWidth()) {
      // Row 1: Date
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.DateRange,
            contentDescription = "Date",
            modifier = Modifier.size(SpacingLarge),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaHigh))
        Spacer(Modifier.width(SpacingSmall))
        Text(
            text = model.dateLine1,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaHigh),
            modifier = Modifier.testTag(EventSummaryCardTags.DATE_LINE1))
      }
      Spacer(Modifier.height(SpacingSmall))
      // Row 2: Time
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.AccessTime,
            contentDescription = "Time",
            modifier = Modifier.size(SpacingLarge),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaHigh))
        Spacer(Modifier.width(SpacingSmall))
        Text(
            text = model.dateLine2,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaHigh),
            modifier = Modifier.testTag(EventSummaryCardTags.DATE_LINE2))
      }
    }
  }
}
