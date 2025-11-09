package com.android.sample.ui.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.utils.EventColor
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * EventSummaryCard (public API)
 *
 * Renders a summary card for an [Event].
 * - This composable is responsible for deriving all values needed for the UI (dates, flags,
 *   labels).
 * - It then delegates pure drawing to [DrawEventSummaryCard], which composes small, focused
 *   sections.
 */
@Composable
fun EventSummaryCard(
    event: Event,
    modifier: Modifier = Modifier,
    participantNames: List<String> = emptyList(),
) {
  // --- Default style and text configuration ---
  val style = EventSummaryCardDefaults.style
  val textConfig = EventSummaryCardDefaults.texts
  val zone = ZoneId.systemDefault()
  val loc = Locale.getDefault()

  // --- UI state (expand/collapse) ---
  var isTitleExpanded by remember { mutableStateOf(false) }
  var didTitleOverflow by remember { mutableStateOf(false) }
  var isDescriptionExpanded by remember { mutableStateOf(false) }
  var didDescriptionOverflow by remember { mutableStateOf(false) }

  // --- Derived temporal content for rendering (single source of truth for date strings) ---
  val dateModel = remember(event, zone, loc) { buildDatePresentation(event, zone, loc) }

  // --- Colors / shapes / participants ---
  val shape = RoundedCornerShape(style.cornerRadiusDp)
  val sideColor = event.color.toComposeColor()

  // Recurrence text (hidden if no recurrence, meaning if the recurrence is OneTime)
  val recurrenceText: String? =
      if (event.recurrenceStatus != RecurrenceStatus.OneTime)
          recurrenceLabel(event.recurrenceStatus, dateModel.startZdt, loc)
      else null

  DrawEventSummaryCard(
      modifier = modifier,
      style = style,
      textConfig = textConfig,
      sideColor = sideColor,
      shape = shape,
      // Title
      titleText = event.title,
      isTitleExpanded = isTitleExpanded,
      onTitleToggle = { isTitleExpanded = !isTitleExpanded },
      onTitleOverflowChange = { didTitleOverflow = it },
      showTitleToggle = didTitleOverflow || isTitleExpanded,
      // Dates
      datePresentation = dateModel,
      // Recurrence
      recurrenceText = recurrenceText,
      // Description
      descriptionText = event.description,
      isDescriptionExpanded = isDescriptionExpanded,
      onDescriptionToggle = { isDescriptionExpanded = !isDescriptionExpanded },
      onDescriptionOverflowChange = { didDescriptionOverflow = it },
      showDescriptionToggle = didDescriptionOverflow || isDescriptionExpanded,
      // Participants
      participantNames = participantNames,
  )
}

/* =======================================================================================
 *                                  Drawing composable
 * ======================================================================================= */

@Composable
private fun DrawEventSummaryCard(
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
                      hasToggleSpacer = style.descHasToggleSpacer)

                  // 5) Participants — preserves current behavior: full list, custom scrollbar
                  // disabled
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

/* =======================================================================================
 *                                    Sub-composables
 * ======================================================================================= */

@Composable
private fun ColoredSideBar(width: Dp, color: Color, shape: RoundedCornerShape) {
  Box(
      modifier =
          Modifier.width(width)
              .fillMaxHeight()
              .background(
                  color,
                  shape =
                      RoundedCornerShape(
                          topStart = shape.topStart,
                          bottomStart = shape.bottomStart,
                          topEnd = CornerSize(0.dp),
                          bottomEnd = CornerSize(0.dp)))
              .testTag(EventSummaryCardTags.SideBar))
}

@Composable
private fun TitleSection(
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

@Composable
private fun DateSection(model: DatePresentation) {
  // Preserves exact previous UI, including labels and spacings.
  if (model.isMultiDay) {
    // === Structure for multi-day ===
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      // Column 1: Labels (From / To) with calendar icon on first row
      Column(modifier = Modifier.padding(end = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
              imageVector = Icons.Filled.DateRange,
              contentDescription = "From date",
              modifier = Modifier.size(16.dp),
              tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
          Spacer(Modifier.width(8.dp))
          Text(
              text = "From",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
              modifier = Modifier.testTag(EventSummaryCardTags.Multi_FromLabel))
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Spacer(Modifier.width(24.dp))
          Text(
              text = "To",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
              modifier = Modifier.testTag(EventSummaryCardTags.Multi_ToLabel))
        }
      }

      // Column 2: Start/End dates
      Column {
        Text(
            text = model.startDateShort,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.testTag(EventSummaryCardTags.Multi_StartDate))
        Spacer(Modifier.height(8.dp))
        Text(
            text = model.endDateShort,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.testTag(EventSummaryCardTags.Multi_EndDate))
      }

      // Column 3: Times
      Column(horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Spacer(Modifier.width(6.dp))
          Text(
              text = "at " + model.startTimeStr,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
              modifier = Modifier.testTag(EventSummaryCardTags.Multi_StartTime))
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Spacer(Modifier.width(6.dp))
          Text(
              text = "at " + model.endTimeStr,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
              modifier = Modifier.testTag(EventSummaryCardTags.Multi_EndTime))
        }
      }
    }
  } else {
    // === Structure for single-day ===
    Column(modifier = Modifier.fillMaxWidth()) {
      // Row 1: Date
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.DateRange,
            contentDescription = "Date",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        Spacer(Modifier.width(8.dp))
        Text(
            text = model.dateLine1,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.testTag(EventSummaryCardTags.DateLine1))
      }
      Spacer(Modifier.height(8.dp))
      // Row 2: Time
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.AccessTime,
            contentDescription = "Time",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        Spacer(Modifier.width(8.dp))
        Text(
            text = model.dateLine2,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.testTag(EventSummaryCardTags.DateLine2))
      }
    }
  }
}

@Composable
private fun RecurrenceSection(recurrenceText: String?) {
  if (!recurrenceText.isNullOrBlank()) {
    Spacer(Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
          imageVector = Icons.Filled.Repeat,
          contentDescription = "Recurrence",
          modifier = Modifier.size(16.dp),
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
      Spacer(Modifier.width(8.dp))
      Text(
          text = recurrenceText,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier.testTag(EventSummaryCardTags.Recurrence))
    }
  }
}

@Composable
private fun DescriptionSection(
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

@Composable
private fun ParticipantsSection(
    participantNames: List<String>,
    rowHeight: Dp,
    visibleRows: Int,
    borderColor: Color,
) {
  if (participantNames.isNotEmpty()) {
    // Section Title "Participants"
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
          imageVector = Icons.Filled.Group,
          contentDescription = "Participants",
          modifier = Modifier.size(16.dp),
          tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
      Spacer(Modifier.width(8.dp))
      Text(
          text = "Participants",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
    }
    Spacer(Modifier.height(4.dp))

    val listState = rememberLazyListState()
    val totalItems = participantNames.size
    val containerHeight = rowHeight * visibleRows + rowHeight * 3 / 5

    Box(
        modifier =
            Modifier.fillMaxWidth()
                .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))) {
          LazyColumn(
              state = listState,
              modifier =
                  Modifier.height(if (totalItems > visibleRows) containerHeight else Dp.Unspecified)
                      .fillMaxWidth()
                      .testTag(EventSummaryCardTags.ParticipantsList)) {
                itemsIndexed(participantNames) { idx, name ->
                  val bg =
                      if (idx % 2 == 0) Color.Transparent
                      else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                  Box(
                      modifier = Modifier.fillMaxWidth().height(rowHeight).background(bg),
                      contentAlignment = Alignment.CenterStart) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp))
                      }
                }
              }
        }
  }
}

/* =======================================================================================
 *                                  Logic / helpers
 * ======================================================================================= */

/** Aggregates all date strings and flags needed by the UI. */
private data class DatePresentation(
    val isMultiDay: Boolean,
    val dateLine1: String, // single-day line 1
    val dateLine2: String, // single-day line 2
    val startDateShort: String, // multi-day Column 2
    val endDateShort: String, // multi-day Column 2
    val startTimeStr: String,
    val endTimeStr: String,
    val startZdt: ZonedDateTime,
    val endZdt: ZonedDateTime
)

/** Builds a stable, reusable description of how dates should be rendered on the card. */
private fun buildDatePresentation(event: Event, zoneId: ZoneId, locale: Locale): DatePresentation {
  val startZdt = event.startDate.atZone(zoneId)
  val endZdt = event.endDate.atZone(zoneId)
  val isMulti = startZdt.toLocalDate() != endZdt.toLocalDate()

  val dayFull = DateTimeFormatter.ofPattern("EEEE d MMM yyyy", locale)
  val dayShort = DateTimeFormatter.ofPattern("EEE d MMM yyyy", locale)
  val hm = DateTimeFormatter.ofPattern("HH:mm", locale)

  val dateLine1: String
  val dateLine2: String
  if (!isMulti) {
    // "Monday 1 Dec 2025"
    dateLine1 = startZdt.format(dayFull)
    // "10:00 — 12:00"
    dateLine2 = "${startZdt.format(hm)} — ${endZdt.format(hm)}"
  } else {
    // Keep text parity with previous version (even though single-day lines are not used here)
    dateLine1 = "From ${startZdt.format(dayShort)} at ${startZdt.format(hm)}"
    dateLine2 = "${startZdt.format(hm)} — ${endZdt.format(hm)}"
  }

  return DatePresentation(
      isMultiDay = isMulti,
      dateLine1 = dateLine1,
      dateLine2 = dateLine2,
      startDateShort = startZdt.format(dayShort),
      endDateShort = endZdt.format(dayShort),
      startTimeStr = startZdt.format(hm),
      endTimeStr = endZdt.format(hm),
      startZdt = startZdt,
      endZdt = endZdt)
}

/** Human-readable recurrence label in English (for now), with localized weekday short name. */
private fun recurrenceLabel(
    status: RecurrenceStatus,
    start: ZonedDateTime,
    locale: Locale
): String =
    when (status) {
      RecurrenceStatus.OneTime -> ""
      RecurrenceStatus.Weekly -> "every week (${weekdayShortLocalized(start.dayOfWeek, locale)})"
      RecurrenceStatus.Monthly -> "every month"
      RecurrenceStatus.Yearly -> "every year"
    }

/** Locale-aware short weekday, e.g. "Mon", "Tue", etc. */
private fun weekdayShortLocalized(d: DayOfWeek, locale: Locale): String =
    d.getDisplayName(TextStyle.SHORT, locale)

/* =======================================================================================
 *                              Building blocks & defaults
 * ======================================================================================= */

/** Test tags for UI testing */
object EventSummaryCardTags {
  const val TitleText = "EventCard_Title"
  const val ToggleTitle = "EventCard_ToggleTitle"
  const val DateLine1 = "EventCard_DateLine1"
  const val DateLine2 = "EventCard_DateLine2"
  const val Multi_FromLabel = "EventCard_Multi_FromLabel"
  const val Multi_ToLabel = "EventCard_Multi_ToLabel"
  const val Multi_StartDate = "EventCard_Multi_StartDate"
  const val Multi_EndDate = "EventCard_Multi_EndDate"
  const val Multi_StartTime = "EventCard_Multi_StartTime"
  const val Multi_EndTime = "EventCard_Multi_EndTime"
  const val Recurrence = "EventCard_Recurrence"
  const val DescriptionText = "EventCard_Description"
  const val ToggleDescription = "EventCard_ToggleDescription"
  const val ParticipantsList = "EventCard_ParticipantsList"
  const val SideBar = "EventCard_SideBar"
}

/**
 * Reusable expandable text with optional edge fade and toggle button.
 * - Reports overflow via [onOverflowChange].
 * - Keeps vertical rhythm when toggle is absent by adding a small spacer.
 */
@Composable
private fun ExpandableText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    collapsedMaxLines: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onOverflowChange: (Boolean) -> Unit,
    showToggle: Boolean,
    toggleLabels: ToggleLabels,
    toggleTypography: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
    onTextHeightChange: (Int) -> Unit = {},
    toggleTestTag: String? = null
) {
  Column(modifier = modifier.fillMaxWidth()) {
    // Measurements derived from the provided text style
    val density = LocalDensity.current
    val fontSizePx = with(density) { style.fontSize.toPx() }
    val computedLineHeight =
        if (style.lineHeight.isUnspecified) style.fontSize * 1.2f else style.lineHeight
    val lineHeightPx = with(density) { computedLineHeight.toPx() }
    // Reserve space for an explicit ellipsis glyph rendered outside the fade
    val ellipsisPx = fontSizePx * 0.9f // ~width of one glyph
    val ellipsisGapPx = fontSizePx * 0.12f
    val ellipsisPaddingDp = with(density) { (ellipsisPx + ellipsisGapPx).toDp() }
    // Fade width scales with font size (roughly 6ch)
    val fadeWidthPx = fontSizePx * 12f

    Box(modifier = Modifier.fillMaxWidth()) {
      Text(
          text = text,
          style = style,
          maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLines,
          overflow = TextOverflow.Clip,
          softWrap = isExpanded || collapsedMaxLines > 1,
          onTextLayout = { result ->
            // When expanded, keep the toggle visible as "Show less"
            if (!isExpanded) onOverflowChange(result.hasVisualOverflow) else onOverflowChange(true)
          },
          color = MaterialTheme.colorScheme.onSurface,
          modifier =
              Modifier.onSizeChanged { onTextHeightChange(it.height) }
                  .then(
                      if (!isExpanded && showToggle) {
                        Modifier.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                            .drawWithContent {
                              drawContent()
                              // Apply fade aligned to the actual end of the text content (exclude
                              // padding).
                              val endX = size.width
                              val startX = (endX - fadeWidthPx).coerceAtLeast(0f)
                              val topY = (size.height - lineHeightPx).coerceAtLeast(0f)
                              val width = (endX - startX).coerceAtLeast(0f)
                              if (width > 0f && lineHeightPx > 0f) {
                                drawRect(
                                    brush =
                                        Brush.horizontalGradient(
                                            colors = listOf(Color.Black, Color.Transparent),
                                            startX = startX,
                                            endX = endX),
                                    topLeft = Offset(startX, topY),
                                    size = Size(width, lineHeightPx),
                                    blendMode = BlendMode.DstIn)
                              }
                            }
                      } else {
                        Modifier
                      })
                  .padding(end = ellipsisPaddingDp))
      if (!isExpanded && showToggle) {
        Box(modifier = Modifier.matchParentSize(), contentAlignment = Alignment.BottomEnd) {
          Text(
              text = "…", // single ellipsis glyph
              style = style,
              color = MaterialTheme.colorScheme.onSurface)
        }
      }
    }

    if (showToggle) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(
            onClick = onToggleExpand,
            contentPadding = PaddingValues(0.dp),
            modifier = if (toggleTestTag != null) Modifier.testTag(toggleTestTag) else Modifier) {
              Text(
                  text = if (isExpanded) toggleLabels.collapse else toggleLabels.expand,
                  style = toggleTypography,
                  color = MaterialTheme.colorScheme.primary)
            }
      }
    } else {
      Spacer(Modifier.height(8.dp))
    }
  }
}

/** Tiny struct to group toggle labels. */
data class ToggleLabels(val expand: String, val collapse: String)

/** Style knobs extracted from magic numbers to one place. */
data class EventSummaryCardStyle(
    val cornerRadiusDp: Dp = 24.dp,
    val leftBarWidthDp: Dp = 18.dp,
    val paddingH: Dp = 24.dp,
    val paddingV: Dp = 18.dp,
    val titleSpacer: Dp = 18.dp,
    val sectionGapLarge: Dp = 32.dp,
    val sectionGapSmall: Dp = 12.dp,
    val descNoToggleSpacer: Dp = 24.dp,
    val descHasToggleSpacer: Dp = 8.dp,
    val participantsRowHeight: Dp = 32.dp,
    val participantsVisibleRows: Int = 5,
)

/** Labels and collapsed line limits. */
data class EventSummaryTextConfig(
    val titleCollapsedMaxLines: Int = 2,
    val descriptionCollapsedMaxLines: Int = 3,
    val toggleLabels: ToggleLabels = ToggleLabels("Show more", "Show less")
)

/** Consolidated defaults for callers. */
object EventSummaryCardDefaults {
  val style = EventSummaryCardStyle()
  val texts = EventSummaryTextConfig()
}

// -------------------------------- Preview --------------------------------

private val previewEvents: List<Event> by lazy {
  val base = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES)
  val day = Duration.ofDays(1)
  val hour = Duration.ofHours(1)
  listOf(
      // e0) Simple event
      Event(
          id = "e0",
          title = "Short title",
          description = "A simple single-day event.",
          startDate = base.plusSeconds(10 * 60 * 60),
          endDate = base.plusSeconds(12 * 60 * 60),
          cloudStorageStatuses = emptySet(),
          locallyStoredBy = emptyList(),
          personalNotes = null,
          participants = setOf("u1", "u2"),
          version = 1L,
          recurrenceStatus = RecurrenceStatus.OneTime,
          hasBeenDeleted = false,
          color = EventColor.Blue),
      // e1) Recurring (weekly)
      Event(
          id = "e1",
          title = "Weekly stand-up",
          description = "Short, recurring meeting.",
          startDate = base.plus(day).plus(hour.multipliedBy(9)),
          endDate = base.plus(day).plus(hour.multipliedBy(10)),
          cloudStorageStatuses = emptySet(),
          locallyStoredBy = emptyList(),
          personalNotes = null,
          participants = setOf("u3", "u4"),
          version = 1L,
          recurrenceStatus = RecurrenceStatus.Weekly,
          hasBeenDeleted = false,
          color = EventColor.Green),
      // e2) 3-day event
      Event(
          id = "e2",
          title = "Offsite (3 days)",
          description = "Team offsite spanning three days.",
          startDate = base.plus(day).plus(hour.multipliedBy(18)),
          endDate = base.plus(day.multipliedBy(4)).plus(hour.multipliedBy(18)),
          cloudStorageStatuses = emptySet(),
          locallyStoredBy = emptyList(),
          personalNotes = null,
          participants = setOf("u2", "u5", "u6"),
          version = 1L,
          recurrenceStatus = RecurrenceStatus.OneTime,
          hasBeenDeleted = false,
          color = EventColor.Orange),
      // e3) 3-day recurring
      Event(
          id = "e3",
          title = "Workshop series (3 days)",
          description = "Three-day recurring workshop.",
          startDate = base.plus(day.multipliedBy(2)).plus(hour.multipliedBy(8)),
          endDate = base.plus(day.multipliedBy(5)).plus(hour.multipliedBy(8)),
          cloudStorageStatuses = emptySet(),
          locallyStoredBy = emptyList(),
          personalNotes = null,
          participants = setOf("u1", "u7"),
          version = 1L,
          recurrenceStatus = RecurrenceStatus.Weekly,
          hasBeenDeleted = false,
          color = EventColor.Purple),
      // e4) Long title
      Event(
          id = "e4",
          title =
              "Very long workshop title here with a lot of words making it intentionally too long for the card layout",
          description = "Long title showcase.",
          startDate = base.plus(day.multipliedBy(3)).plus(hour.multipliedBy(14)),
          endDate = base.plus(day.multipliedBy(3)).plus(hour.multipliedBy(16)),
          cloudStorageStatuses = emptySet(),
          locallyStoredBy = emptyList(),
          personalNotes = null,
          participants = setOf("u2", "u8"),
          version = 1L,
          recurrenceStatus = RecurrenceStatus.OneTime,
          hasBeenDeleted = false,
          color = EventColor.Black),
      // e5) Long description
      Event(
          id = "e5",
          title = "Deep-dive session",
          description =
              ("This is a very long description — " +
                      "it keeps going to test the collapse/expand behavior. ".repeat(8))
                  .trim(),
          startDate = base.plus(day.multipliedBy(4)).plus(hour.multipliedBy(10)),
          endDate = base.plus(day.multipliedBy(4)).plus(hour.multipliedBy(12)),
          cloudStorageStatuses = emptySet(),
          locallyStoredBy = emptyList(),
          personalNotes = null,
          participants = setOf("u3", "u9"),
          version = 1L,
          recurrenceStatus = RecurrenceStatus.OneTime,
          hasBeenDeleted = false,
          color = EventColor.Red),
      // e6) Many participants
      Event(
          id = "e6",
          title = "Large meeting",
          description = "A meeting with many attendees.",
          startDate = base.plus(hour.multipliedBy(13)),
          endDate = base.plus(hour.multipliedBy(15)),
          cloudStorageStatuses = emptySet(),
          locallyStoredBy = emptyList(),
          personalNotes = null,
          participants = (1..10).map { "u$it" }.toSet(),
          version = 1L,
          recurrenceStatus = RecurrenceStatus.OneTime,
          hasBeenDeleted = false,
          color = EventColor.Orange),
      // e7) Minimal
      Event(
          id = "e7",
          title = "Smallest event",
          description = "",
          startDate = base.plus(hour.multipliedBy(16)),
          endDate = base.plus(hour.multipliedBy(17)),
          cloudStorageStatuses = emptySet(),
          locallyStoredBy = emptyList(),
          personalNotes = null,
          participants = emptySet(),
          version = 1L,
          recurrenceStatus = RecurrenceStatus.OneTime,
          hasBeenDeleted = false,
          color = EventColor.Green))
}

@Preview(showBackground = true)
@Composable
private fun EventSummaryCardPreview_Weekly() {
  val names =
      mapOf(
          "u1" to "Alice",
          "u2" to "Bob",
          "u3" to "Charlie",
          "u4" to "David",
          "u5" to "Elvis",
          "u6" to "Franck",
          "u7" to "Gondulphe",
          "u8" to "Harry",
          "u9" to "Igor",
          "u10" to "Johan")

  // Change here the preview index for quick testing
  val event = previewEvents[5]
  // Event index correspondence :
  // 0: Simple single-day             (should display correctly)
  // 1: Weekly recurring              (should display the recurrence)
  // 2: 3-day event                   (should adapt the 2 lines of the date)
  // 3: 3-day event, weekly recurring (should combine both)
  // 4: Very long title               (should fade the last letters
  //                                        + show a working "show more/less" button)
  // 5: Very long description         (same : shade + responsive button)
  // 6: Many participants (10)        (should make the list scrollable with, and display only half
  //                                          of the last name visible to improve ux)
  // 7: Minimal                       (should render without crash, no description/participants)

  val participantDisplayNames = event.participants.mapNotNull { names[it] }

  EventSummaryCard(
      event = event, participantNames = participantDisplayNames, modifier = Modifier.fillMaxWidth())
}
