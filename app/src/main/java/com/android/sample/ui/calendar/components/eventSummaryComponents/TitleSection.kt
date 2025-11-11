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


/**
 * Displays the event title with expand/collapse behavior for long titles.
 *
 * Overflow detection, last-line fade, and the toggle button are delegated to `ExpandableText`.
 * When the toggle is not visible, a spacer preserves vertical rhythm with neighboring sections.
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
    text: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onOverflowChange: (Boolean) -> Unit,
    showToggle: Boolean,
    collapsedMaxLines: Int,
    toggleLabels: ToggleLabels,
    afterNoToggleSpacer: Dp
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