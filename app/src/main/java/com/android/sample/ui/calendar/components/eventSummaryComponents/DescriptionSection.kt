package com.android.sample.ui.calendar.components.eventSummaryComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import com.android.sample.ui.calendar.components.EventSummaryCardTags
import com.android.sample.ui.calendar.components.ExpandableText
import com.android.sample.ui.calendar.style.EventSummaryCardDefaults
import com.android.sample.ui.calendar.style.EventSummaryCardStyle
import com.android.sample.ui.calendar.style.EventSummaryTextConfig
import com.android.sample.ui.theme.AlphaLow
import com.android.sample.ui.theme.BarWidthSmall
import com.android.sample.ui.theme.SpacingMedium
import com.android.sample.ui.theme.WeightVeryHeavy

/**
 * Renders the event description with expand/collapse and a left accent bar.
 *
 * The accent bar height tracks the measured text height to avoid a floating bar.
 * The section also reports whether the collapsed text overflows so the caller can decide
 * to show a "Show more / Show less" toggle.
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
    onToggle: () -> Unit = { },
    onOverflowChange: (Boolean) -> Unit = { },
    showToggle: Boolean = true,
    noToggleSpacer: Dp = EventSummaryCardStyle().descHasToggleSpacer,
    hasToggleSpacer: Dp = EventSummaryCardStyle().descNoToggleSpacer
) {
    if (descriptionText.isNotBlank()) {
        // Measured text height (px), drives the left accent bar height
        var descHeightPx by remember { mutableStateOf(0) }
        val descHeightDp = with(LocalDensity.current) { descHeightPx.toDp() }

        Row(modifier = Modifier.fillMaxWidth()) {
            // Left accent bar scaled to text height
            Box(
                modifier =
                    Modifier.width(BarWidthSmall)
                        .height(descHeightDp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AlphaLow)))
            Spacer(Modifier.width(SpacingMedium))
            Column(modifier = Modifier.weight(WeightVeryHeavy)) {
                // Delegate expansion, overflow detection, and edge-fade to the reusable component
                ExpandableText(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodyMedium,
                    collapsedMaxLines = collapsedMaxLines,
                    isExpanded = isExpanded,
                    onToggleExpand = onToggle,
                    onOverflowChange = onOverflowChange,
                    showToggle = showToggle,
                    toggleLabels = EventSummaryCardDefaults.texts.toggleLabels,
                    toggleTypography = MaterialTheme.typography.labelMedium,
                    onTextHeightChange = { descHeightPx = it },
                    modifier = Modifier.testTag(EventSummaryCardTags.DescriptionText),
                    toggleTestTag = EventSummaryCardTags.ToggleDescription)
            }
        }
    }

    // Keep vertical rhythm consistent whether the toggle is visible or not
    if (!showToggle) Spacer(Modifier.height(noToggleSpacer))
    else Spacer(Modifier.height(hasToggleSpacer))
}