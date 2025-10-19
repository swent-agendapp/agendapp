package com.android.sample.ui.calendar.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * # DaysSegmentedControl
 * Components related to the segmented control for days (1, 5, 7).
 *
 * This file defines a segmented control that allows the user to select the display range of the
 * calendar (1 day, 5 days, or 7 days). The white indicator slides smoothly under the selected label
 * with an animation, and each segment’s width is dynamically calculated based on the measured width
 * of the container.
 */

/**
 * Segmented control to choose how many days to display (1, 5, or 7).
 *
 * UI behavior:
 * - A rounded white indicator slides beneath the selected label.
 * - Each segment’s width is one-third of the total available width.
 * - Ripple effect is disabled to avoid visual artifacts during animation.
 *
 * @param modifier Modifiers applied to the outer container.
 * @param selected Currently selected value. Expected values: 1, 5, or 7.
 * @param onSelect Callback invoked when a segment is tapped, passing the selected value.
 */
@Composable
fun DaysSegmentedControl(modifier: Modifier = Modifier, selected: Int, onSelect: (Int) -> Unit) {
  // Corner radius for the outer container and the inner indicator
  val outerShape = RoundedCornerShape(14.dp)
  val innerShape = RoundedCornerShape(10.dp)
  // Density access for converting between px and dp during measurements
  val density = LocalDensity.current

  // Base control dimensions and internal padding
  val controlHeight = 36.dp
  val contentPadding = 4.dp

  // Measured width of the Row in pixels (updated via onSizeChanged)
  var rowWidthPx by remember { mutableIntStateOf(0) }

  // Segment width = 1/3 of total width; height = control height minus padding
  val segmentWidthDp: Dp = with(density) { (rowWidthPx / 3).toDp() }
  val segmentHeightDp: Dp = controlHeight - (contentPadding * 2)

  // Animated horizontal offset of the indicator depending on the selected segment
  val indicatorOffsetX by
      animateDpAsState(
          targetValue =
              when (selected) {
                1 -> 0.dp
                5 -> segmentWidthDp
                7 -> segmentWidthDp + segmentWidthDp
                else -> 0.dp // defensive fallback if unexpected value
              },
          label = "segmentedIndicatorX")

  Surface(
      modifier = modifier.height(controlHeight),
      shape = outerShape,
      color = MaterialTheme.colorScheme.surfaceVariant,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    // Inner content box managing padding and layering (zIndex)
    Box(Modifier.padding(contentPadding)) {
      // White indicator drawn underneath the labels (zIndex lower than the Row)
      if (rowWidthPx > 0) {
        Box(
            Modifier.offset(x = indicatorOffsetX)
                .width(segmentWidthDp)
                .height(segmentHeightDp)
                .clip(innerShape)
                .background(MaterialTheme.colorScheme.surface)
                .zIndex(0f))
      }

      // Clickable labels drawn on top of the indicator
      Row(
          modifier =
              Modifier.onSizeChanged { size ->
                    // Capture the measured width to compute each segment’s width
                    rowWidthPx = size.width
                  }
                  .fillMaxWidth()
                  .zIndex(1f)) {
            DaySegmentButton(
                text = "1 day",
                selected = selected == 1,
                onClick = { if (selected != 1) onSelect(1) },
                segmentWidth = segmentWidthDp,
                segmentHeight = segmentHeightDp)
            DaySegmentButton(
                text = "5 days",
                selected = selected == 5,
                onClick = { if (selected != 5) onSelect(5) },
                segmentWidth = segmentWidthDp,
                segmentHeight = segmentHeightDp)
            DaySegmentButton(
                text = "7 days",
                selected = selected == 7,
                onClick = { if (selected != 7) onSelect(7) },
                segmentWidth = segmentWidthDp,
                segmentHeight = segmentHeightDp)
          }
    }
  }
}

/**
 * A single segment (button) used by [DaysSegmentedControl].
 *
 * Displays a centered label, adjusts text color depending on the *selected* state, and triggers an
 * *onClick* action without any ripple effect (to maintain visual consistency with the animated
 * indicator).
 *
 * @param modifier Modifier for this segment.
 * @param text Label for the segment (e.g., “1 day”).
 * @param selected Whether the segment is currently active (affects text color).
 * @param onClick Callback invoked when the segment is tapped.
 * @param segmentWidth Fixed width calculated by the parent.
 * @param segmentHeight Fixed height calculated by the parent.
 */
@Composable
private fun DaySegmentButton(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    segmentWidth: Dp,
    segmentHeight: Dp
) {
  Box(
      modifier =
          modifier
              .width(segmentWidth)
              .height(segmentHeight)
              .clickable(
                  // Disable ripple effect for a cleaner look under the animated indicator
                  interactionSource = remember { MutableInteractionSource() },
                  indication = null,
                  onClick = onClick),
      contentAlignment = Alignment.Center) {
        Text(
            text = text,
            color =
                if (selected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
      }
}
