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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.android.sample.R
import com.android.sample.model.authentication.User
import com.android.sample.model.category.EventCategory
import com.android.sample.ui.calendar.components.EventSummaryCardTags
import com.android.sample.ui.calendar.components.ExpandableText
import com.android.sample.ui.calendar.style.EventSummaryCardDefaults
import com.android.sample.ui.calendar.style.EventSummaryCardStyle
import com.android.sample.ui.calendar.style.EventSummaryTextConfig
import com.android.sample.ui.calendar.utils.DatePresentation
import com.android.sample.ui.calendar.utils.NO_DATA_DEFAULT_VALUE
import com.android.sample.ui.theme.AlphaExtraLow
import com.android.sample.ui.theme.ElevationExtraLow
import com.android.sample.ui.theme.EventPalette
import java.time.ZonedDateTime

@Composable
fun DrawEventSummaryCard(
    modifier: Modifier = Modifier,
    style: EventSummaryCardStyle = EventSummaryCardDefaults.style,
    textConfig: EventSummaryTextConfig = EventSummaryCardDefaults.texts,
    sideColor: Color = EventPalette.Blue,
    shape: RoundedCornerShape = RoundedCornerShape(style.cornerRadiusDp),
    // Title
    titleText: String = "No title provided...",
    isTitleExpanded: Boolean = false,
    onTitleToggle: () -> Unit = {},
    onTitleOverflowChange: (Boolean) -> Unit = {},
    showTitleToggle: Boolean = true,
    // Category
    category: EventCategory = EventCategory.defaultCategory(),
    // Dates
    datePresentation: DatePresentation =
        DatePresentation(
            isMultiDay = false,
            dateLine1 = NO_DATA_DEFAULT_VALUE,
            dateLine2 = NO_DATA_DEFAULT_VALUE,
            startDateShort = NO_DATA_DEFAULT_VALUE,
            endDateShort = NO_DATA_DEFAULT_VALUE,
            startTimeStr = NO_DATA_DEFAULT_VALUE,
            endTimeStr = NO_DATA_DEFAULT_VALUE,
            startZdt = ZonedDateTime.now(),
            endZdt = ZonedDateTime.now()),
    // Recurrence
    recurrenceText: String? = null,
    // Extra event indicator
    isExtra: Boolean = false,
    showExtraInfo: Boolean = false,
    onExtraToggle: () -> Unit = {},
    extraInfoText: String? = null,
    // Description
    descriptionText: String = "No description provided...",
    isDescriptionExpanded: Boolean = false,
    onDescriptionToggle: () -> Unit = {},
    onDescriptionOverflowChange: (Boolean) -> Unit = {},
    showDescriptionToggle: Boolean = true,
    // Participants
    participantNames: List<User> = emptyList(),
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
                      modifier = Modifier.testTag(EventSummaryCardTags.TITLE_TEXT),
                      toggleTestTag = EventSummaryCardTags.TOGGLE_TITLE)
                  // Preserve spacing when the toggle is absent
                  // if (!showTitleToggle) Spacer(Modifier.height(style.titleSpacer))

                  // 2) Category
                  CategorySection(category = category)
                  if (isExtra) {
                    Spacer(Modifier.height(style.sectionGapSmall))
                    ExtraEventSection(
                        onToggle = onExtraToggle,
                        showInfo = showExtraInfo,
                        infoText = extraInfoText)
                  }
                  Spacer(Modifier.height(style.sectionGapLarge))

                  // 3) Dates
                  DateSection(datePresentation)

                  // 4) Recurrence
                  RecurrenceSection(recurrenceText)

                  Spacer(Modifier.height(style.sectionGapLarge))

                  // 5) Description
                  DescriptionSection(
                      descriptionText = descriptionText,
                      collapsedMaxLines = textConfig.descriptionCollapsedMaxLines,
                      isExpanded = isDescriptionExpanded,
                      onToggle = onDescriptionToggle,
                      onOverflowChange = onDescriptionOverflowChange,
                      showToggle = showDescriptionToggle,
                      noToggleSpacer = style.descNoToggleSpacer,
                      hasToggleSpacer = style.descHasToggleSpacer)

                  // 6) Participants
                  ParticipantsSection(
                      participantNames = participantNames,
                      rowHeight = style.participantsRowHeight,
                      visibleRows = style.participantsVisibleRows,
                  )
                }
          }
        }
      }
}

@Composable
private fun ExtraEventSection(onToggle: () -> Unit, showInfo: Boolean, infoText: String?) {
  Column(modifier = Modifier.fillMaxWidth()) {
    IconButton(
        onClick = onToggle,
        modifier = Modifier.testTag(EventSummaryCardTags.EXTRA_BADGE)) {
          Icon(
              imageVector = Icons.Filled.Star,
              contentDescription = stringResource(id = R.string.extra_event_label),
              tint = MaterialTheme.colorScheme.primary)
        }

    if (showInfo && !infoText.isNullOrBlank()) {
      Surface(
          modifier =
              Modifier.fillMaxWidth()
                  .padding(top = EventSummaryCardStyle().sectionGapSmall)
                  .testTag(EventSummaryCardTags.EXTRA_INFO),
          color = MaterialTheme.colorScheme.secondaryContainer,
          shape = MaterialTheme.shapes.medium) {
            Text(
                text = infoText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = EventSummaryCardStyle().paddingH))
          }
    }
  }
}
