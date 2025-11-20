package com.android.sample.ui.calendar.components.eventSummaryComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.sample.ui.calendar.components.EventSummaryCardTags
import com.android.sample.ui.calendar.style.EventSummaryCardStyle
import com.android.sample.ui.theme.BarWidthMedium
import com.android.sample.ui.theme.EventPalette

/**
 * Vertical colored sidebar used as a visual accent on the left of the card.
 *
 * The right corners are flattened so the bar blends into the card body, while the left corners
 * inherit the provided rounded radii to match the container.
 *
 * @param width Physical width of the bar.
 * @param color Bar color (typically derived from the event color).
 * @param shape Rounded shape whose start radii are reused.
 */
@Composable
fun ColoredSideBar(
    width: Dp = BarWidthMedium,
    color: Color = EventPalette.Blue,
    shape: RoundedCornerShape = RoundedCornerShape(EventSummaryCardStyle().cornerRadiusDp)
) {
  Box(
      modifier =
          Modifier.width(width)
              .fillMaxHeight()
              // Keep only the start (left) radii so the bar merges seamlessly with the container
              .background(
                  color,
                  shape =
                      RoundedCornerShape(
                          topStart = shape.topStart,
                          bottomStart = shape.bottomStart,
                          topEnd = CornerSize(0.dp),
                          bottomEnd = CornerSize(0.dp)))
              .testTag(EventSummaryCardTags.SIDE_BAR))
}
