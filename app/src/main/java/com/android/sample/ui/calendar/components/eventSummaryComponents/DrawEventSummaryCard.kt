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
import androidx.compose.ui.platform.testTag
import com.android.sample.ui.calendar.components.EventSummaryCardTags
import com.android.sample.ui.calendar.components.ExpandableText
import com.android.sample.ui.calendar.style.EventSummaryCardDefaults
import com.android.sample.ui.calendar.style.EventSummaryCardStyle
import com.android.sample.ui.calendar.style.EventSummaryTextConfig
import com.android.sample.ui.calendar.utils.DatePresentation
import com.android.sample.ui.theme.AlphaExtraLow
import com.android.sample.ui.theme.AlphaLow
import com.android.sample.ui.theme.ElevationExtraLow
import com.android.sample.utils.EventColor
import java.time.ZonedDateTime

@Composable
fun DrawEventSummaryCard(
    modifier: Modifier = Modifier,
    style: EventSummaryCardStyle = EventSummaryCardDefaults.style,
    textConfig: EventSummaryTextConfig = EventSummaryCardDefaults.texts,
    sideColor: Color = EventColor.Blue.toComposeColor(),
    shape: RoundedCornerShape = RoundedCornerShape(style.cornerRadiusDp),
    // Title
    titleText: String = "No title provided...",
    isTitleExpanded: Boolean = false,
    onTitleToggle: () -> Unit = {},
    onTitleOverflowChange: (Boolean) -> Unit = {},
    showTitleToggle: Boolean = true,
    // Dates
    datePresentation: DatePresentation =
        DatePresentation(
            isMultiDay = false,
            dateLine1 = "No date provided...",
            dateLine2 = "No date provided...",
            startDateShort = "No date provided...",
            endDateShort = "No date provided...",
            startTimeStr = "No date provided...",
            endTimeStr = "No date provided...",
            startZdt = ZonedDateTime.now(),
            endZdt = ZonedDateTime.now()),
    // Recurrence
    recurrenceText: String? = null,
    // Description
    descriptionText: String = "No description provided...",
    isDescriptionExpanded: Boolean = false,
    onDescriptionToggle: () -> Unit = {},
    onDescriptionOverflowChange: (Boolean) -> Unit = {},
    showDescriptionToggle: Boolean = true,
    // Participants
    participantNames: List<String> = emptyList(),
) {
  val overlayColor =
      sideColor.copy(alpha = AlphaExtraLow) // translucent event tint drawn above base
  val baseContainerColor = MaterialTheme.colorScheme.surface // stable background for light/dark

  Card(
      modifier = modifier.fillMaxWidth(),
      shape = shape,
      elevation = CardDefaults.cardElevation(defaultElevation = ElevationExtraLow),
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
                  ExpandableText(
                      text = titleText,
                      style = MaterialTheme.typography.titleLarge,
                      collapsedMaxLines = textConfig.titleCollapsedMaxLines,
                      isExpanded = isTitleExpanded,
                      onToggleExpand = onTitleToggle,
                      onOverflowChange = onTitleOverflowChange,
                      showToggle = showTitleToggle,
                      toggleLabels = textConfig.toggleLabels,
                      toggleTypography = MaterialTheme.typography.labelMedium,
                      modifier = Modifier.testTag(EventSummaryCardTags.TitleText),
                      toggleTestTag = EventSummaryCardTags.ToggleTitle)
                  // Preserve spacing when the toggle is absent
                  if (!showTitleToggle) Spacer(Modifier.height(style.titleSpacer))

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
                      hasToggleSpacer = style.descHasToggleSpacer)

                  // 5) Participants
                  ParticipantsSection(
                      participantNames = participantNames,
                      rowHeight = style.participantsRowHeight,
                      visibleRows = style.participantsVisibleRows,
                      borderColor =
                          MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AlphaLow),
                  )
                }
          }
        }
      }
}
