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
import com.android.sample.ui.calendar.components.EventSummaryCardTags
import com.android.sample.ui.calendar.style.EventSummaryCardStyle
import com.android.sample.ui.theme.AlphaHigh
import com.android.sample.ui.theme.BorderWidthThin
import com.android.sample.ui.theme.CornerRadiusLarge
import com.android.sample.ui.theme.IconSizeMedium
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.RowHeightMedium
import com.android.sample.ui.theme.SpacingSmall
import com.android.sample.utils.EventColor

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
    participantNames: List<String> = emptyList(),
    rowHeight: Dp = RowHeightMedium,
    visibleRows: Int = EventSummaryCardStyle().participantsVisibleRows,
    borderColor: Color = EventColor.Blue.toComposeColor(),
) {
  if (participantNames.isNotEmpty()) {
    // Section header with "Participants" label and people icon
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
          imageVector = Icons.Filled.Group,
          contentDescription = "Participants",
          modifier = Modifier.size(IconSizeMedium),
          tint = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaHigh))
      Spacer(Modifier.width(SpacingSmall))
      Text(
          text = "Participants",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = AlphaHigh))
    }
    Spacer(Modifier.height(SpacingSmall))

    val listState = rememberLazyListState()
    val totalItems = participantNames.size
    // Add a small extra (~0.6 row) so the next item peeks into view and hints scrollability
    val containerHeight = rowHeight * visibleRows + rowHeight * 3 / 5

    Box(
        modifier =
            Modifier.fillMaxWidth()
                .border(
                    width = BorderWidthThin,
                    color = borderColor,
                    shape = RoundedCornerShape(CornerRadiusLarge))
                .clip(RoundedCornerShape(CornerRadiusLarge))) {
          LazyColumn(
              state = listState,
              modifier =
                  Modifier.height(if (totalItems > visibleRows) containerHeight else Dp.Unspecified)
                      .fillMaxWidth()
                      .testTag(EventSummaryCardTags.ParticipantsList)) {
                itemsIndexed(participantNames) { idx, name ->
                  // Gentle zebra striping improves scan-ability for long lists
                  val bg =
                      if (idx % 2 == 0) MaterialTheme.colorScheme.surface
                      else MaterialTheme.colorScheme.surfaceVariant
                  Box(
                      modifier = Modifier.fillMaxWidth().height(rowHeight).background(bg),
                      contentAlignment = Alignment.CenterStart) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = PaddingMedium))
                      }
                }
              }
        }
  }
}
