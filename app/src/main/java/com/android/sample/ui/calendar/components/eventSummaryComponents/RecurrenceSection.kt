package com.android.sample.ui.calendar.components.eventSummaryComponents

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.android.sample.ui.calendar.components.EventSummaryCardTags
import com.android.sample.ui.theme.AlphaMedium
import com.android.sample.ui.theme.IconSizeMedium
import com.android.sample.ui.theme.SpacingMedium
import com.android.sample.ui.theme.SpacingSmall

/**
 * Shows a concise recurrence line (e.g., "every week (Mon)") in a subdued style.
 *
 * The section self-hides when `recurrenceText` is null or blank.
 *
 * @param recurrenceText Human-readable recurrence string (already localized/constructed).
 */
@Composable
fun RecurrenceSection(recurrenceText: String? = null) {
  if (!recurrenceText.isNullOrBlank()) {
    // Extra breathing room to separate from the date/time block
    Spacer(Modifier.height(SpacingMedium))
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
          imageVector = Icons.Filled.Repeat,
          contentDescription = "Recurrence",
          modifier = Modifier.size(IconSizeMedium),
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AlphaMedium))
      Spacer(Modifier.width(SpacingSmall))
      Text(
          text = recurrenceText,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AlphaMedium),
          modifier = Modifier.testTag(EventSummaryCardTags.Recurrence))
    }
  }
}
