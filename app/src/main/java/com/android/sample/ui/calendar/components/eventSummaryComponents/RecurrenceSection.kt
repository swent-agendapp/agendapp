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
import androidx.compose.ui.unit.dp
import com.android.sample.ui.calendar.components.EventSummaryCardTags

/**
 * Shows a concise recurrence line (e.g., "every week (Mon)") in a subdued style.
 *
 * The section self-hides when `recurrenceText` is null or blank.
 *
 * @param recurrenceText Human-readable recurrence string (already localized/constructed).
 */
@Composable
fun RecurrenceSection(recurrenceText: String?) {
    if (!recurrenceText.isNullOrBlank()) {
        // Extra breathing room to separate from the date/time block
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Repeat,
                contentDescription = "Recurrence",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            Spacer(Modifier.width(8.dp))
            Text(
                text = recurrenceText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.testTag(EventSummaryCardTags.Recurrence))
        }
    }
}