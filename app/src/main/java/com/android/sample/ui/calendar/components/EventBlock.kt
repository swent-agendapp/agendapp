package com.android.sample.ui.calendar.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.sample.model.calendar.Event
import com.android.sample.ui.calendar.CalendarScreenTestTags
import com.android.sample.ui.calendar.style.CalendarDefaults
import com.android.sample.ui.calendar.style.defaultGridContentDimensions
import com.android.sample.ui.calendar.utils.EventPositionUtil
import java.time.LocalDate
import java.time.LocalTime
import com.android.sample.ui.calendar.utils.DateTimeUtils

/**
 * Draws visual blocks for a list of events within a single day column. Events are clipped to the
 * visible time window and positioned using their start/end times. Overlap handling is planned and
 * not yet implemented here.
 *
 * @param modifier [Modifier] applied to each event container.
 * @param events Events for a single day (or already filtered set) to render.
 * @param startTime Inclusive start time of the visible grid.
 * @param endTime Exclusive end time of the visible grid.
 * @param columnWidthDp The width of the day column hosting these events.
 * @return Unit. This is a composable that renders UI side-effects only.
 */
@Composable
fun EventBlock(
    modifier: Modifier = Modifier,
    events: List<Event> = listOf(),
    currentDate: LocalDate, // used to compute the visible portion of events that may span multiple days
    startTime: LocalTime = CalendarDefaults.DefaultStartTime,
    endTime: LocalTime = CalendarDefaults.DefaultEndTime,
    columnWidthDp: Dp = defaultGridContentDimensions().defaultColumnWidthDp
) {
  // Later : place this "filter" logic in "EventOverlapHandling", which will call this EventBlock
  // Filter events for the current day and time range
  val visibleEvents =
      run {
        val visibleStartInstant = DateTimeUtils.localDateTimeToInstant(currentDate, startTime)
        val visibleEndInstantExclusive = DateTimeUtils.localDateTimeToInstant(currentDate, endTime)
        events.filter { event ->
          // Overlap on instants: [event.start, event.end[ AND [visibleStart, visibleEnd[ non-empty
          event.endDate > visibleStartInstant && event.startDate < visibleEndInstantExclusive
        }
      }

  if (visibleEvents.isEmpty()) return

  visibleEvents.forEach { event ->
    Box(modifier = modifier.testTag("${CalendarScreenTestTags.EVENT_BLOCK}_${event.title}")) {
      val density = LocalDensity.current

      // Compute offsets for the visible segment of the event within this day's time window
      val (topOffset, eventHeight) =
          EventPositionUtil.calculateVerticalOffsets(
              event = event,
              currentDate = currentDate,
              startTime = startTime,
              endTime = endTime,
              density = density,
          )

      // Event styling
      val backgroundColor = event.color.toComposeColor()
      val textColor = Color.Black
      val cornerRadius = 4.dp

      // Later : add logic to adapt the view when orientation (portrait or not)

      Box(
          modifier =
              modifier
                  .offset(x = 0.dp, y = topOffset) // Later when overlap : x = columnWidth *
                  // eventLayout.offsetFraction
                  .size(
                      width = columnWidthDp,
                      height = eventHeight) // Later when overlap : width = columnWidth *
                  // eventLayout.widthFraction
                  .clip(RoundedCornerShape(cornerRadius))
                  .background(backgroundColor)
                  .padding(start = 4.dp, top = 4.dp, end = 4.dp),
          // Later for testing : .testTag("EventView_${event.id}")
          // Later : handle onTap and onLongPress
      ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            // Later for testing : .testTag("EventViewInner_${event.id}"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
          // Main title
          Text(
              text = event.title,
              color = textColor,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
          )

          // Participants (if any)
          if (event.participants.isNotEmpty()) {
            val participantsText = event.participants.joinToString(", ")
            Text(
                text = participantsText,
                color = textColor.copy(alpha = 0.8f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
          }

          // Time information
          val timeText =
            "${DateTimeUtils.formatInstantToTime(event.startDate)} - " +
               DateTimeUtils.formatInstantToTime(event.endDate)

          Text(
              text = timeText,
              color = textColor.copy(alpha = 0.7f),
              fontSize = 9.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
          )

          // Later if needed : upperText and/or lowerText
        }
      }
    }
  }
}
