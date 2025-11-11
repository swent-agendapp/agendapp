package com.android.sample.ui.calendar.components.eventSummaryComponents

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import com.android.sample.ui.calendar.components.EventSummaryCardTags
import com.android.sample.ui.calendar.components.ExpandableText
import com.android.sample.ui.calendar.components.ToggleLabels
import com.android.sample.ui.calendar.style.EventSummaryCardDefaults
import com.android.sample.ui.calendar.style.EventSummaryCardStyle
import com.android.sample.ui.calendar.style.EventSummaryTextConfig

/**
 * Displays the event title with expand/collapse behavior for long titles.
 *
 * Overflow detection, last-line fade, and the toggle button are delegated to `ExpandableText`. When
 * the toggle is not visible, a spacer preserves vertical rhythm with neighboring sections.
 *
 * @param text Title to display.
 * @param isExpanded Whether the title is expanded.
 * @param onToggle Invoked to toggle expanded/collapsed state.
 * @param onOverflowChange Reports whether the collapsed title overflows.
 * @param showToggle Whether the "Show more/less" toggle is visible.
 * @param collapsedMaxLines Maximum number of lines in the collapsed state.
 * @param toggleLabels Localized labels for the toggle button.
 * @param afterNoToggleSpacer Spacer height used when the toggle is absent.
 */
@Composable
fun TitleSection(
    text: String = "No title provided...",
    isExpanded: Boolean = false,
    onToggle: () -> Unit = {},
    onOverflowChange: (Boolean) -> Unit = {},
    showToggle: Boolean = true,
    collapsedMaxLines: Int = EventSummaryTextConfig().titleCollapsedMaxLines,
    toggleLabels: ToggleLabels = EventSummaryCardDefaults.texts.toggleLabels,
    afterNoToggleSpacer: Dp = EventSummaryCardStyle().titleSpacer
) {
  ExpandableText(
      text = text,
      style = MaterialTheme.typography.titleLarge,
      collapsedMaxLines = collapsedMaxLines,
      isExpanded = isExpanded,
      onToggleExpand = onToggle,
      onOverflowChange = onOverflowChange,
      showToggle = showToggle,
      toggleLabels = toggleLabels,
      toggleTypography = MaterialTheme.typography.labelMedium,
      modifier = Modifier.testTag(EventSummaryCardTags.TitleText),
      toggleTestTag = EventSummaryCardTags.ToggleTitle)
  // Preserve spacing when the toggle is absent
  if (!showToggle) Spacer(Modifier.height(afterNoToggleSpacer))
}
