package com.android.sample.ui.calendar.components.eventSummaryComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import com.android.sample.ui.calendar.components.EventSummaryCardTags
import com.android.sample.ui.calendar.components.ExpandableText
import com.android.sample.ui.calendar.style.EventSummaryCardDefaults
import com.android.sample.ui.calendar.style.EventSummaryCardStyle
import com.android.sample.ui.calendar.style.EventSummaryTextConfig
import com.android.sample.ui.theme.AlphaLow
import com.android.sample.ui.theme.BarWidthSmall
import com.android.sample.ui.theme.SpacingExtraSmall
import com.android.sample.ui.theme.SpacingMedium

/**
 * Renders the event description with expand/collapse and a left accent bar.
 *
 * The accent bar height tracks the measured text height to avoid a floating bar. The section also
 * reports whether the collapsed text overflows so the caller can decide to show a "Show more / Show
 * less" toggle.
 *
 * @param descriptionText Raw description to show. When blank, this section is omitted.
 * @param collapsedMaxLines Maximum number of lines when collapsed.
 * @param isExpanded Whether the text is expanded.
 * @param onToggle Invoked when the user toggles expand/collapse.
 * @param onOverflowChange Reports whether the collapsed content overflows.
 * @param showToggle Whether the toggle button is visible.
 * @param noToggleSpacer Spacer height used when the toggle is NOT shown.
 * @param hasToggleSpacer Spacer height used when the toggle IS shown.
 */
@Composable
fun DescriptionSection(
    descriptionText: String = "No description provided...",
    collapsedMaxLines: Int = EventSummaryTextConfig().descriptionCollapsedMaxLines,
    isExpanded: Boolean = false,
    onToggle: () -> Unit = {},
    onOverflowChange: (Boolean) -> Unit = {},
    showToggle: Boolean = true,
    noToggleSpacer: Dp = EventSummaryCardStyle().descHasToggleSpacer,
    hasToggleSpacer: Dp = EventSummaryCardStyle().descNoToggleSpacer
) {
  if (descriptionText.isBlank()) return

  val textConfig = EventSummaryCardDefaults.texts

  Column(modifier = Modifier.fillMaxWidth()) {

    // Main row: side bar and description text share the same height.
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
      // Left accent bar scaled to text height
      Box(
          modifier =
              Modifier.width(BarWidthSmall)
                  .fillMaxHeight()
                  .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AlphaLow))
                  .testTag(EventSummaryCardTags.SIDE_BAR))

      Spacer(Modifier.width(SpacingMedium))

      // Delegate expansion, overflow detection, and edge-fade to the reusable component
      ExpandableText(
          text = descriptionText,
          style = MaterialTheme.typography.bodyMedium,
          collapsedMaxLines = collapsedMaxLines,
          isExpanded = isExpanded,
          onToggleExpand = onToggle, // not used here since showToggle = false
          onOverflowChange = onOverflowChange,
          showToggle =
              false, // The toggle row is rendered manually below the description (to handle left
          // bar correctly).
          toggleLabels = textConfig.toggleLabels,
          toggleTypography = MaterialTheme.typography.labelMedium,
          onTextHeightChange = {}, // Text height is not needed to size the bar.
          modifier = Modifier.testTag(EventSummaryCardTags.DESCRIPTION_TEXT),
      )
    }

    // Separate "Show more / Show less" row so the side bar only follows the description text.
    if (showToggle) {
      Spacer(Modifier.height(SpacingExtraSmall))

      Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.weight(1f))

        Text(
            text =
                if (isExpanded) textConfig.toggleLabels.collapse
                else textConfig.toggleLabels.expand,
            style = MaterialTheme.typography.labelMedium,
            modifier =
                Modifier.testTag(EventSummaryCardTags.TOGGLE_DESCRIPTION)
                    .clickable(onClick = onToggle))
      }

      // Keep vertical rhythm consistent whether the toggle is visible or not
      Spacer(Modifier.height(hasToggleSpacer))
    } else {
      Spacer(Modifier.height(noToggleSpacer))
    }
  }
}
