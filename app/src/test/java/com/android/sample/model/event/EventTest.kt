package com.android.sample.model.event

import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.createEvent
import com.android.sample.utils.EventColor
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
            color = EventColor.Green)
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
    val defaultEvent = createEvent()
    assertEquals(EventColor.Blue, defaultEvent.color)
  }

  @Test
  fun customColor_shouldBeUsedWhenSpecified() {
    assertEquals(EventColor.Green, event.color)
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
}
