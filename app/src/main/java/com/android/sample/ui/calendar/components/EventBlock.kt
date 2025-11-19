package com.android.sample.ui.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.createEvent
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.style.CalendarDefaults
import com.android.sample.ui.calendar.style.defaultGridContentDimensions
import com.android.sample.ui.calendar.utils.DateTimeUtils
import com.android.sample.ui.calendar.utils.EventOverlapCalculator
import com.android.sample.ui.calendar.utils.EventPositionUtil
import com.android.sample.ui.theme.AlphaExtraLow
import com.android.sample.ui.theme.AlphaHigh
import com.android.sample.ui.theme.AlphaLow
import com.android.sample.ui.theme.BorderWidthThin
import com.android.sample.ui.theme.CornerRadiusSmall
import com.android.sample.ui.theme.ElevationExtraLow
import com.android.sample.ui.theme.PaddingExtraSmall
import com.android.sample.ui.theme.widthLarge
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Filters the given list of events to only those visible within the current date and time range.
 *
 * @param events The list of events to filter.
 * @param currentDate The current date to consider for visibility.
 * @param startTime The inclusive start time of the visible grid.
 * @param endTime The exclusive end time of the visible grid.
 * @return A list of events visible within the specified date and time range.
 */
// Later : place this "filter" logic in "EventOverlapHandling", which will call this EventBlock
private fun filterVisibleEvents(
    events: List<Event>,
    currentDate: LocalDate,
    startTime: LocalTime,
    endTime: LocalTime
): List<Event> {
  val visibleStartInstant = DateTimeUtils.localDateTimeToInstant(currentDate, startTime)
  val visibleEndInstantExclusive = DateTimeUtils.localDateTimeToInstant(currentDate, endTime)
  return events.filter { event ->
    // Overlap on instants: [event.start, event.end[ AND [visibleStart, visibleEnd[ non-empty
    event.endDate > visibleStartInstant && event.startDate < visibleEndInstantExclusive
  }
}

/**
 * Draws visual blocks for a list of events within a single day column. Events are clipped to the
 * visible time window and positioned using their start/end times. Overlap handling is planned and
 * not yet implemented here.
 *
 * @param modifier [Modifier] applied to each event container.
 * @param events Events for a single day (or already filtered set) to render.
 * @param currentDate Used to compute the visible portion of events that may span multiple days.
 * @param startTime Inclusive start time of the visible grid.
 * @param endTime Exclusive end time of the visible grid.
 * @param columnWidthDp The width of the day column hosting these events.
 * @return Unit. This is a composable that renders UI side-effects only.
 */
@Composable
fun EventBlock(
    modifier: Modifier = Modifier,
    events: List<Event> = emptyList(),
    currentDate:
        LocalDate, // used to compute the visible portion of events that may span multiple days
    startTime: LocalTime = CalendarDefaults.DefaultStartTime,
    endTime: LocalTime = CalendarDefaults.DefaultEndTime,
    columnWidthDp: Dp = defaultGridContentDimensions().defaultColumnWidthDp,
    onEventClick: (Event) -> Unit = {}
) {
  // Filter events for the current day and time range using the helper
  val visibleEvents = filterVisibleEvents(events, currentDate, startTime, endTime)

  if (visibleEvents.isEmpty()) return

  val density = LocalDensity.current

  // Calculate horizontal overlap layouts for all visible events
  val eventLayouts = EventOverlapCalculator.calculateEventLayouts(visibleEvents)

  visibleEvents.forEach { event ->
    // Compute offsets for the visible segment of the event within this day's time window
    val (topOffset, eventHeight) =
        EventPositionUtil.calculateVerticalOffsets(
            event = event,
            currentDate = currentDate,
            startTime = startTime,
            endTime = endTime,
            density = density,
        )

    // Retrieve layout for this event (if any). If none, fall back to full width, no offset.
    val layout = eventLayouts[event]
    val eventWidthDp =
        if (layout != null) {
          columnWidthDp * layout.widthFraction
        } else {
          columnWidthDp
        }
    val horizontalOffsetDp =
        if (layout != null) {
          columnWidthDp * layout.offsetFraction
        } else {
          0.dp
        }

    DrawEventBlock(
        modifier = modifier,
        event = event,
        topOffset = topOffset,
        eventHeight = eventHeight,
        eventWidthDp = eventWidthDp,
        horizontalOffsetDp = horizontalOffsetDp,
        onEventClick = onEventClick,
    )
  }
}

/**
 * Draws the UI block for a single event within a day column.
 *
 * Positions the block using the given vertical and horizontal offsets, constrains it to the
 * provided width and height, and displays the event details inside the block.
 *
 * @param modifier [Modifier] applied to the event container.
 * @param event The event to render.
 * @param topOffset The vertical offset from the top of the column.
 * @param eventHeight The height of the event block.
 * @param eventWidthDp The width of the event block within the day column.
 * @param horizontalOffsetDp The horizontal offset from the left edge of the day column.
 * @param onEventClick Callback invoked when the event block is clicked.
 */
@Composable
private fun DrawEventBlock(
    modifier: Modifier = Modifier,
    event: Event,
    topOffset: Dp = 0.dp,
    eventHeight: Dp = widthLarge, // squared
    eventWidthDp: Dp = widthLarge,
    horizontalOffsetDp: Dp = 0.dp,
    onEventClick: (Event) -> Unit = {},
) {
  // Event styling
  val backgroundColor = event.color
  val textColor = Color.Black

  // Later : add logic to adapt the view when orientation (portrait or not)

  Box(
      modifier =
          modifier
              .offset(
                  x = horizontalOffsetDp,
                  y = topOffset,
              )
              .size(
                  width = eventWidthDp,
                  height = eventHeight,
              )
              .shadow(
                  elevation = ElevationExtraLow,
                  shape = RoundedCornerShape(CornerRadiusSmall),
                  clip = true)
              .clip(RoundedCornerShape(CornerRadiusSmall))
              .background(backgroundColor)
              .border(
                  width = BorderWidthThin,
                  color = Color.Black.copy(alpha = AlphaExtraLow),
                  shape = RoundedCornerShape(CornerRadiusSmall))
              .padding(start = PaddingExtraSmall, top = PaddingExtraSmall, end = PaddingExtraSmall)
              .clickable(onClick = { onEventClick(event) })
              .testTag("${CalendarScreenTestTags.EVENT_BLOCK}_${event.title}"),
      // Later : handle onTap and onLongPress
  ) {
    Column(
        modifier = Modifier.fillMaxSize().padding(start = PaddingExtraSmall),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
    ) {
      // Main title
      Text(
          text = event.title,
          color = textColor,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
      )

      // later : when 1-day view : Spacer(modifier.height(SpacingExtraSmall))

      // Time information
      val timeText =
          "${DateTimeUtils.formatInstantToTime(event.startDate)} — " +
              DateTimeUtils.formatInstantToTime(event.endDate)

      Text(
          text = timeText,
          color = textColor.copy(alpha = AlphaHigh),
          fontSize = 9.sp,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
      )

      // later : when 1-day view : Spacer(modifier.height(SpacingExtraSmall))

      // Participants (if any)
      if (event.participants.isNotEmpty()) {
        val participantsText = event.participants.joinToString(", ")
        Text(
            text = participantsText,
            color = textColor.copy(alpha = AlphaLow),
            fontSize = 10.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
      }

      // Later if needed : upperText and/or lowerText
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun DrawEventBlockPreview() {
  DrawEventBlock(
      event =
          createEvent(
              title = "Title !",
              description = "description...",
              startDate = Instant.now(),
              endDate = Instant.now().plusSeconds(60 * 60),
              cloudStorageStatuses = emptySet(),
              personalNotes = null,
              participants = setOf("Alice"),
          ))
}
