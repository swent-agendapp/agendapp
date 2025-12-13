package com.android.sample.data.local.mappers

import com.android.sample.data.local.mappers.EventMapper.toEntity
import com.android.sample.data.local.mappers.EventMapper.toEvent
import com.android.sample.data.local.objects.EventEntity
import com.android.sample.model.calendar.CloudStorageStatus
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.category.EventCategory
import com.android.sample.ui.theme.EventPalette
import java.time.Instant
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class EventMapperTest {

  private lateinit var baseEvent: Event
  private lateinit var emptyEvent: Event
  private lateinit var eventWithMalformedPresence: EventEntity

  private lateinit var testCategory: EventCategory

  @Before
  fun setUp() {
    testCategory =
        EventCategory(
            id = "test-category-id",
            organizationId = "test-organization-id",
            label = "Work",
            color = EventPalette.Red,
            isDefault = false)

    baseEvent =
        Event(
            id = "event1",
            organizationId = "org1",
            title = "Meeting",
            description = "Team meeting",
            startDate = Instant.parse("2025-12-02T08:00:00Z"),
            endDate = Instant.parse("2025-12-02T10:00:00Z"),
            cloudStorageStatuses = setOf(CloudStorageStatus.FIRESTORE),
            locallyStoredBy = listOf("user1", "user2"),
            personalNotes = "Bring documents",
            participants = setOf("user1", "user2"),
            presence = mapOf("user1" to true, "user2" to false),
            version = 12345L,
            hasBeenDeleted = false,
            recurrenceStatus = RecurrenceStatus.OneTime,
            category = testCategory,
            location = null)

    emptyEvent =
        baseEvent.copy(
            cloudStorageStatuses = emptySet(),
            locallyStoredBy = emptyList(),
            participants = emptySet(),
            presence = emptyMap())

    eventWithMalformedPresence = baseEvent.toEntity().copy(presence = "malformed:string")
  }

  @Test
  fun `round-trip Event toEntity and back preserves all fields`() {
    val entity = baseEvent.toEntity()
    val mappedBack = entity.toEvent()

    assertEquals(baseEvent.id, mappedBack.id)
    assertEquals(baseEvent.organizationId, mappedBack.organizationId)
    assertEquals(baseEvent.title, mappedBack.title)
    assertEquals(baseEvent.description, mappedBack.description)
    assertEquals(baseEvent.startDate, mappedBack.startDate)
    assertEquals(baseEvent.endDate, mappedBack.endDate)
    assertEquals(baseEvent.version, mappedBack.version)
    assertEquals(baseEvent.hasBeenDeleted, mappedBack.hasBeenDeleted)
    assertEquals(baseEvent.recurrenceStatus, mappedBack.recurrenceStatus)
    assertEquals(baseEvent.category, mappedBack.category)

    assertEquals(baseEvent.cloudStorageStatuses, mappedBack.cloudStorageStatuses)
    assertEquals(baseEvent.locallyStoredBy, mappedBack.locallyStoredBy)
    assertEquals(baseEvent.participants, mappedBack.participants)
    assertEquals(baseEvent.presence, mappedBack.presence)
  }

  @Test
  fun `round-trip Event with empty collections and presence`() {
    val entity = emptyEvent.toEntity()
    val mappedBack = entity.toEvent()

    assertTrue(mappedBack.cloudStorageStatuses.isEmpty())
    assertTrue(mappedBack.locallyStoredBy.isEmpty())
    assertTrue(mappedBack.participants.isEmpty())
    assertTrue(mappedBack.presence.isEmpty())
  }

  @Test
  fun `malformed presence in EventEntity decodes to empty map`() {
    val mappedBack = eventWithMalformedPresence.toEvent()
    assertTrue(mappedBack.presence.isEmpty())
  }

  @Test
  fun `Event color is correctly mapped via toArgb and back`() {
    val entity = baseEvent.toEntity()
    val mappedBack = entity.toEvent()
    assertEquals(baseEvent.category, mappedBack.category)
    // Check ARGB integer representation
    assertEquals(baseEvent.category.color.value.toInt(), mappedBack.category.color.value.toInt())
  }

  @Test
  fun `Event with multiple cloud storage statuses round-trip`() {
    val multiStorageEvent =
        baseEvent.copy(cloudStorageStatuses = setOf(CloudStorageStatus.FIRESTORE))
    val entity = multiStorageEvent.toEntity()
    val mappedBack = entity.toEvent()
    assertEquals(multiStorageEvent.cloudStorageStatuses, mappedBack.cloudStorageStatuses)
  }

  @Test
  fun `round-trip handles participants and locallyStoredBy correctly`() {
    val entity = baseEvent.toEntity()
    val mappedBack = entity.toEvent()

    assertEquals(baseEvent.participants, mappedBack.participants)
    assertEquals(baseEvent.locallyStoredBy, mappedBack.locallyStoredBy)
  }

  @Test
  fun `round-trip handles personalNotes correctly`() {
    val entity = baseEvent.toEntity()
    val mappedBack = entity.toEvent()
    assertEquals(baseEvent.personalNotes, mappedBack.personalNotes)
  }
}
