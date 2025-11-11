package com.android.sample.ui.calendar.components.eventSummaryComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.sample.ui.calendar.components.EventSummaryCardTags

/**
 * Displays a participant list with gentle zebra striping and optional fixed-height scrolling.
 *
 * If `participantNames.size` exceeds `visibleRows`, the list gets a fixed height and becomes
 * scrollable. Otherwise it sizes to its content height.
 *
 * @param participantNames Names to display (already resolved by the caller).
 * @param rowHeight Height of each row, also used to compute the container height.
 * @param visibleRows Number of fully visible rows before enabling scrolling.
 * @param borderColor Color of the rounded border around the list.
 */
@Composable
fun ParticipantsSection(
    participantNames: List<String>,
    rowHeight: Dp,
    visibleRows: Int,
    borderColor: Color,
) {
    if (participantNames.isNotEmpty()) {
        // Section header with "Participants" label and people icon
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Group,
                contentDescription = "Participants",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Participants",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        }
        Spacer(Modifier.height(4.dp))

        val listState = rememberLazyListState()
        val totalItems = participantNames.size
        // Add a small extra (~0.6 row) so the next item peeks into view and hints scrollability
        val containerHeight = rowHeight * visibleRows + rowHeight * 3 / 5

        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))) {
            LazyColumn(
                state = listState,
                modifier =
                    Modifier.height(if (totalItems > visibleRows) containerHeight else Dp.Unspecified)
                        .fillMaxWidth()
                        .testTag(EventSummaryCardTags.ParticipantsList)) {
                itemsIndexed(participantNames) { idx, name ->
                    // Gentle zebra striping improves scan-ability for long lists
                    val bg =
                        if (idx % 2 == 0) Color.Transparent
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    Box(
                        modifier = Modifier.fillMaxWidth().height(rowHeight).background(bg),
                        contentAlignment = Alignment.CenterStart) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp))
                    }
                }
            }
        }
    }
}