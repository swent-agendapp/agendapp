package com.android.sample.model.event

import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.calendar.createEvent
import com.android.sample.ui.theme.EventPalette
import java.time.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/** Unit tests for the Event data class and its related functions. */
class EventTest {

  // Event instance used for testing
  private lateinit var event: Event

  @Before
  fun setUp() {
    // Initialize an Event with specific start and end times for testing
    val startLocal = LocalDateTime.of(2025, 10, 15, 9, 30)
    val endLocal = LocalDateTime.of(2025, 10, 15, 11, 0)

    event =
        createEvent(
            title = "Meeting",
            description = "Team sync",
            startDate = startLocal.atZone(ZoneId.systemDefault()).toInstant(),
            endDate = endLocal.atZone(ZoneId.systemDefault()).toInstant(),
            color = EventPalette.Green)[0]
  }

  @Test
  fun startLocalDate_shouldReturnCorrectLocalDate() {
    val expectedDate = LocalDate.of(2025, 10, 15)
    assertEquals(expectedDate, event.startLocalDate)
  }

  @Test
  fun endLocalDate_shouldReturnCorrectLocalDate() {
    val expectedDate = LocalDate.of(2025, 10, 15)
    assertEquals(expectedDate, event.endLocalDate)
  }

  @Test
  fun startLocalTime_shouldReturnCorrectLocalTime() {
    val expectedTime = LocalTime.of(9, 30)
    assertEquals(expectedTime, event.startLocalTime)
  }

  @Test
  fun endLocalTime_shouldReturnCorrectLocalTime() {
    val expectedTime = LocalTime.of(11, 0)
    assertEquals(expectedTime, event.endLocalTime)
  }

  @Test
  fun defaultColor_shouldBeUsedWhenNotSpecified() {
    val defaultEvent = createEvent()[0]
    assertEquals(EventPalette.Blue, defaultEvent.color)
  }

  @Test
  fun customColor_shouldBeUsedWhenSpecified() {
    assertEquals(EventPalette.Green, event.color)
  }

  @Test
  fun copyEvent_shouldPreserveAllFields() {
    val copy = event.copy(title = "Updated Meeting")
    assertEquals("Updated Meeting", copy.title)
    assertEquals(event.description, copy.description)
    assertEquals(event.startDate, copy.startDate)
    assertEquals(event.endDate, copy.endDate)
    assertEquals(event.color, copy.color)
  }

  @Test
  fun `create A Daily Event For 1 Weeks Create 7 Event`() {
    val startLocal = LocalDateTime.of(2025, 11, 24, 9, 0)
    val endLocal = LocalDateTime.of(2025, 11, 24, 9, 30)
    val endRecurrenceLocal = LocalDateTime.of(2025, 11, 30, 9, 30)

    val events =
        createEvent(
            title = "Meeting",
            description = "Team sync",
            startDate = startLocal.atZone(ZoneId.systemDefault()).toInstant(),
            endDate = endLocal.atZone(ZoneId.systemDefault()).toInstant(),
            color = EventPalette.Green,
            endRecurrence = endRecurrenceLocal.atZone(ZoneId.systemDefault()).toInstant(),
            recurrence = RecurrenceStatus.Daily)
    assertEquals(events.size, 7)
  }

  @Test
  fun `create A Weekly Event For 4 Weeks + 1 days Create 5 Event`() {
    val startLocal = LocalDateTime.of(2025, 10, 15, 9, 30)
    val endLocal = LocalDateTime.of(2025, 10, 15, 11, 0)
    val endRecurrenceLocal = startLocal.plusWeeks(4).plusDays(1)

    val events =
        createEvent(
            title = "Meeting",
            description = "Team sync",
            startDate = startLocal.atZone(ZoneId.systemDefault()).toInstant(),
            endDate = endLocal.atZone(ZoneId.systemDefault()).toInstant(),
            color = EventPalette.Green,
            endRecurrence = endRecurrenceLocal.atZone(ZoneId.systemDefault()).toInstant(),
            recurrence = RecurrenceStatus.Weekly)
    assertEquals(events.size, 5)
  }

  @Test
  fun `create A Weekly Event For 4 Weeks - 1 days Create 4 Event`() {
    val startLocal = LocalDateTime.of(2025, 10, 15, 9, 30)
    val endLocal = LocalDateTime.of(2025, 10, 15, 11, 0)
    val endRecurrenceLocal = startLocal.plusWeeks(4).plusDays(-1)

    val events =
        createEvent(
            title = "Meeting",
            description = "Team sync",
            startDate = startLocal.atZone(ZoneId.systemDefault()).toInstant(),
            endDate = endLocal.atZone(ZoneId.systemDefault()).toInstant(),
            color = EventPalette.Green,
            endRecurrence = endRecurrenceLocal.atZone(ZoneId.systemDefault()).toInstant(),
            recurrence = RecurrenceStatus.Weekly)
    assertEquals(events.size, 4)
  }

  @Test
  fun `create A Monthly Event For 1 Year Create 13 Event`() {
    val startLocal = LocalDateTime.of(2025, 10, 15, 9, 30)
    val endLocal = LocalDateTime.of(2025, 10, 15, 11, 0)
    val endRecurrenceLocal = LocalDateTime.of(2026, 10, 15, 11, 0)

    val events =
        createEvent(
            title = "Meeting",
            description = "Team sync",
            startDate = startLocal.atZone(ZoneId.systemDefault()).toInstant(),
            endDate = endLocal.atZone(ZoneId.systemDefault()).toInstant(),
            color = EventPalette.Green,
            endRecurrence = endRecurrenceLocal.atZone(ZoneId.systemDefault()).toInstant(),
            recurrence = RecurrenceStatus.Monthly)
    assertEquals(events.size, 13)
  }

  @Test
  fun `create A Yearly Event For 1 Year and half Create 2 Event`() {
    val startLocal = LocalDateTime.of(2025, 10, 15, 9, 30)
    val endLocal = LocalDateTime.of(2025, 10, 15, 11, 0)
    val endRecurrenceLocal = LocalDateTime.of(2026, 12, 15, 11, 0)

    val events =
        createEvent(
            title = "Meeting",
            description = "Team sync",
            startDate = startLocal.atZone(ZoneId.systemDefault()).toInstant(),
            endDate = endLocal.atZone(ZoneId.systemDefault()).toInstant(),
            color = EventPalette.Green,
            endRecurrence = endRecurrenceLocal.atZone(ZoneId.systemDefault()).toInstant(),
            recurrence = RecurrenceStatus.Yearly)
    assertEquals(events.size, 2)
  }

  @Test
  fun invalidDates_shouldReturnIllegalArgument() {
    val start = Instant.now()
    val end = start.minusSeconds(3600)

    val exception =
        assertThrows(IllegalArgumentException::class.java) {
          createEvent(title = "Invalid Event", startDate = start, endDate = end)
        }

    assertEquals("End date cannot be before start date", exception.message)
  }
}
