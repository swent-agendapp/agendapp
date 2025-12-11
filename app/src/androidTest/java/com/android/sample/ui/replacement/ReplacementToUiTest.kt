package com.android.sample.ui.replacement

import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.category.EventCategory
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.ReplacementStatus
import com.android.sample.ui.replacement.mainPage.ReplacementRequestUi
import com.android.sample.ui.replacement.mainPage.toUi
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ReplacementToUiTest : RequiresSelectedOrganizationTestBase {

  override val organizationId = "ORG1"

  @Before
  fun setup() {
    setSelectedOrganization()
  }

  @Test
  fun replacementToUiMapsFieldsCorrectly() {
    // Arrange
    val start = Instant.parse("2025-02-10T09:00:00Z")
    val end = Instant.parse("2025-02-10T10:30:00Z")

    val event =
        Event(
            id = "E123",
            organizationId = organizationId,
            title = "Team Sync",
            description = "Daily standup",
            startDate = start,
            endDate = end,
            participants = setOf("A", "B"),
            recurrenceStatus = RecurrenceStatus.OneTime,
            hasBeenDeleted = false,
            category = EventCategory.defaultCategory(),
            version = 1L,
            locallyStoredBy = emptyList(),
            cloudStorageStatuses = emptySet(),
            personalNotes = null)

    val replacement =
        Replacement(
            id = "R001",
            absentUserId = "EMP001",
            substituteUserId = "EMP002",
            event = event,
            status = ReplacementStatus.ToProcess)

    // Convert using the extension
    val ui: ReplacementRequestUi = replacement.toUi()

    // Act
    val startZoned = start.atZone(ZoneId.systemDefault())
    val endZoned = end.atZone(ZoneId.systemDefault())

    // Assert
    assertEquals("R001", ui.id)

    // weekdayAndDay: simple LocalDate string
    assertEquals(startZoned.toLocalDate().toString(), ui.weekdayAndDay)

    // timeRange formatted as "HH:MM:SS - HH:MM:SS"
    val expectedRange = "${startZoned.toLocalTime()} - ${endZoned.toLocalTime()}"
    assertEquals(expectedRange, ui.timeRange)

    assertEquals("Team Sync", ui.title)
    assertEquals("Daily standup", ui.description)
  }
}
