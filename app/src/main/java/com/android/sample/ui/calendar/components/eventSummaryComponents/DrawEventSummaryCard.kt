package com.android.sample.ui.calendar.components.eventSummaryComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.sample.ui.calendar.style.EventSummaryCardStyle
import com.android.sample.ui.calendar.style.EventSummaryTextConfig
import com.android.sample.ui.calendar.utils.DatePresentation


@Composable
fun DrawEventSummaryCard(
    modifier: Modifier,
    style: EventSummaryCardStyle,
    textConfig: EventSummaryTextConfig,
    sideColor: Color,
    shape: RoundedCornerShape,
    // Title
    titleText: String,
    isTitleExpanded: Boolean,
    onTitleToggle: () -> Unit,
    onTitleOverflowChange: (Boolean) -> Unit,
    showTitleToggle: Boolean,
    // Dates
    datePresentation: DatePresentation,
    // Recurrence
    recurrenceText: String?,
    // Description
    descriptionText: String,
    isDescriptionExpanded: Boolean,
    onDescriptionToggle: () -> Unit,
    onDescriptionOverflowChange: (Boolean) -> Unit,
    showDescriptionToggle: Boolean,
    // Participants
    participantNames: List<String>,
) {
    val overlayColor = sideColor.copy(alpha = 0.1f) // translucent event tint drawn above base
    val baseContainerColor = MaterialTheme.colorScheme.surface // stable background for light/dark

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = baseContainerColor)) {
        // Draw a translucent overlay tinted by the event color above the stable base
        Box(modifier = Modifier.fillMaxWidth().clip(shape).background(overlayColor)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                ColoredSideBar(width = style.leftBarWidthDp, color = sideColor, shape = shape)

                Column(
                    modifier =
                        Modifier.padding(horizontal = style.paddingH, vertical = style.paddingV)
                            .fillMaxWidth()) {
                    // 1) Title
                    TitleSection(
                        text = titleText,
                        isExpanded = isTitleExpanded,
                        onToggle = onTitleToggle,
                        onOverflowChange = onTitleOverflowChange,
                        showToggle = showTitleToggle,
                        collapsedMaxLines = textConfig.titleCollapsedMaxLines,
                        toggleLabels = textConfig.toggleLabels,
                        afterNoToggleSpacer = style.titleSpacer)

                    // 2) Dates
                    DateSection(datePresentation)

                    // 3) Recurrence
                    RecurrenceSection(recurrenceText)

                    Spacer(Modifier.height(style.sectionGapLarge))

                    // 4) Description
                    DescriptionSection(
                        descriptionText = descriptionText,
                        collapsedMaxLines = textConfig.descriptionCollapsedMaxLines,
                        isExpanded = isDescriptionExpanded,
                        onToggle = onDescriptionToggle,
                        onOverflowChange = onDescriptionOverflowChange,
                        showToggle = showDescriptionToggle,
                        noToggleSpacer = style.descNoToggleSpacer,
                        hasToggleSpacer = style.descHasToggleSpacer
                    )

                    // 5) Participants
                    ParticipantsSection(
                        participantNames = participantNames,
                        rowHeight = style.participantsRowHeight,
                        visibleRows = style.participantsVisibleRows,
                        borderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    )
                }
            }
        }
    }
}