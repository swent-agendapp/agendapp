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
    if (!showToggle) Spacer(Modifier.height(afterNoToggleSpacer))
}