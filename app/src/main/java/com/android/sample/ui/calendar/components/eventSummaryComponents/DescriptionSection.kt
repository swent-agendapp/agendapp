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
import androidx.compose.ui.unit.dp
import com.android.sample.ui.calendar.components.EventSummaryCardTags
import com.android.sample.ui.calendar.components.ExpandableText
import com.android.sample.ui.calendar.style.EventSummaryCardDefaults

@Composable
fun DescriptionSection(
    descriptionText: String,
    collapsedMaxLines: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onOverflowChange: (Boolean) -> Unit,
    showToggle: Boolean,
    noToggleSpacer: Dp,
    hasToggleSpacer: Dp
) {
    if (descriptionText.isNotBlank()) {
        var descHeightPx by remember { mutableStateOf(0) }
        val descHeightDp = with(LocalDensity.current) { descHeightPx.toDp() }

        Row(modifier = Modifier.fillMaxWidth()) {
            // Left accent bar scaled to text height
            Box(
                modifier =
                    Modifier.width(4.dp)
                        .height(descHeightDp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
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

    if (!showToggle) Spacer(Modifier.height(noToggleSpacer))
    else Spacer(Modifier.height(hasToggleSpacer))
}
