package com.android.sample.ui.calendar.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.style.GridContentStyle
import com.android.sample.ui.calendar.style.defaultGridContentStyle
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.collections.forEach

/**
 * Left-side time axis listing hour labels aligned with the grid rows. Scroll is shared with the
 * grid area to keep labels in sync.
 *
 * @param timeLabels Ordered hour tick labels to display (e.g., 08:00, 09:00, ...).
 * @param rowHeightDp Height per hour row, in [Dp].
 * @param gridHeightDp Total height of the scrollable grid area, in [Dp].
 * @param leftOffsetDp Width reserved for the time axis column, in [Dp].
 * @param style Visual style for text and spacing.
 * @param scrollState Shared scroll state to sync with the grid content.
 * @return Unit. This is a composable that renders UI side-effects only.
 */
@Composable
internal fun TimeAxisColumn(
    timeLabels: List<LocalTime>,
    rowHeightDp: Dp = defaultGridContentStyle().dimensions.rowHeightDp,
    gridHeightDp: Dp = rowHeightDp * (32 - 8),
    leftOffsetDp: Dp = defaultGridContentStyle().dimensions.leftOffsetDp,
    style: GridContentStyle = defaultGridContentStyle(),
    scrollState: ScrollState = rememberScrollState()
) {
  Box(
      modifier = Modifier.width(leftOffsetDp).height(gridHeightDp),
      // Total height of the scrollable grid
  ) {

    // Regular time labels (hours)
    Column(
        modifier =
            Modifier.verticalScroll(scrollState).testTag(CalendarScreenTestTags.TIME_AXIS_COLUMN)) {
          timeLabels.forEach { timeLabel ->
            Box(modifier = Modifier.size(leftOffsetDp, rowHeightDp)) {
              Text(
                  modifier = Modifier.padding(horizontal = 4.dp),
                  text = timeLabel.format(DateTimeFormatter.ofPattern("HH:mm")),
                  style = TextStyle(fontSize = 12.sp, color = style.colors.timeLabelTextColor),
              )
            }
          }
        }
  }
}
