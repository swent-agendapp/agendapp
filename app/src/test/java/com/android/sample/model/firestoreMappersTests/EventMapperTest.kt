package com.android.sample.model.firestoreMappersTests

import com.android.sample.model.calendar.CloudStorageStatus
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.category.EventCategory
import com.android.sample.model.firestoreMappers.EventMapper
import com.android.sample.ui.theme.EventPalette
import com.google.common.truth.Truth.assertThat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.time.Instant
import java.util.Date
import org.junit.Test
import org.mockito.Mockito.*

class EventMapperTest {

  private val start = Instant.parse("2025-10-29T10:00:00Z")
  private val end = Instant.parse("2025-10-29T12:00:00Z")

  private val sampleCategory =
      EventCategory(
          id = "category123",
          organizationId = "testOrg",
          label = "Test Category",
          color = EventPalette.Blue,
          isDefault = false,
      )

  private val sampleEvent =
      Event(
          id = "event123",
          organizationId = "testOrg",
          title = "Event Title",
          description = "Event Description",
          startDate = start,
          endDate = end,
          cloudStorageStatuses = setOf(CloudStorageStatus.FIRESTORE),
          personalNotes = "Some notes",
          participants = setOf("participant1", "participant2"),
          version = 5L,
          presence = mapOf("participant1" to true, "participant2" to false),
          recurrenceStatus = RecurrenceStatus.OneTime,
          category = sampleCategory,
      )

  private val sampleMap: Map<String, Any?> =
      mapOf(
          "id" to "event123",
          "organizationId" to "testOrg",
          "title" to "Event Title",
          "description" to "Event Description",
          "startDate" to Timestamp(Date.from(start)),
          "endDate" to Timestamp(Date.from(end)),
          "storageStatus" to listOf("FIRESTORE"),
          "personalNotes" to "Some notes",
          "participants" to listOf("participant1", "participant2"),
          "version" to 5L,
          "presence" to mapOf("participant1" to true, "participant2" to false),
          "recurrenceStatus" to "OneTime",
          "eventCategory" to
              mapOf(
                  "id" to sampleCategory.id,
                  "organizationId" to sampleCategory.organizationId,
                  "label" to sampleCategory.label,
                  "color" to sampleCategory.color.value.toLong(),
                  "isDefault" to sampleCategory.isDefault,
              ),
      )

  // --- fromDocument tests ---
  @Test
  fun fromDocument_withValidDocument_returnsEvent() {
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.id).thenReturn("event123")
    `when`(doc.getString("organizationId")).thenReturn("testOrg")
    `when`(doc.getString("title")).thenReturn("Event Title")
    `when`(doc.getString("description")).thenReturn("Event Description")
    `when`(doc.getTimestamp("startDate")).thenReturn(Timestamp(Date.from(start)))
    `when`(doc.getTimestamp("endDate")).thenReturn(Timestamp(Date.from(end)))
    `when`(doc.getString("personalNotes")).thenReturn("Some notes")
    `when`(doc.get("participants")).thenReturn(listOf("participant1", "participant2"))
    `when`(doc.get("storageStatus")).thenReturn(listOf("FIRESTORE"))
    `when`(doc.getString("recurrenceStatus")).thenReturn("OneTime")
    `when`(doc.getLong("version")).thenReturn(5L)
    `when`(doc.get("presence")).thenReturn(mapOf("participant1" to true, "participant2" to false))
    `when`(doc.get("eventCategory"))
        .thenReturn(
            mapOf(
                "id" to sampleCategory.id,
                "organizationId" to sampleCategory.organizationId,
                "label" to sampleCategory.label,
                "color" to sampleCategory.color.value.toLong(),
                "isDefault" to sampleCategory.isDefault,
            ))

    val event = EventMapper.fromDocument(doc)
    assertThat(event).isNotNull()
    assertThat(event).isEqualTo(sampleEvent)
  }

  @Test
  fun fromDocument_missingRequiredFields_returnsNull() {
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.id).thenReturn("event123")
    `when`(doc.getString("title")).thenReturn(null)
    val event = EventMapper.fromDocument(doc)
    assertThat(event).isNull()
  }

  // --- fromMap tests ---
  @Test
  fun fromMap_withValidData_returnsEvent() {
    val event = EventMapper.fromMap(sampleMap)
    assertThat(event).isNotNull()
    assertThat(event).isEqualTo(sampleEvent)
  }

  @Test
  fun fromMap_missingRequiredFields_returnsNull() {
    val invalidMap = sampleMap - "title"
    val event = EventMapper.fromMap(invalidMap)
    assertThat(event).isNull()
  }

  @Test
  fun fromMap_withDifferentDateTypes_parsesCorrectly() {
    val mapWithDate = sampleMap.toMutableMap()
    mapWithDate["startDate"] = Date.from(start)
    mapWithDate["endDate"] = Date.from(end)
    val event = EventMapper.fromMap(mapWithDate)
    assertThat(event).isEqualTo(sampleEvent)

    val mapWithLong = sampleMap.toMutableMap()
    mapWithLong["startDate"] = start.toEpochMilli()
    mapWithLong["endDate"] = end.toEpochMilli()
    val event2 = EventMapper.fromMap(mapWithLong)
    assertThat(event2).isEqualTo(sampleEvent)
  }

  // --- fromAny tests ---
  @Test
  fun fromAny_withDocument_returnsEvent() {
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.id).thenReturn("event123")
    `when`(doc.getString("organizationId")).thenReturn("testOrg")
    `when`(doc.getString("title")).thenReturn("Event Title")
    `when`(doc.getTimestamp("startDate")).thenReturn(Timestamp(Date.from(start)))
    `when`(doc.getTimestamp("endDate")).thenReturn(Timestamp(Date.from(end)))
    `when`(doc.getString("description")).thenReturn("Event Description")
    val event = EventMapper.fromAny(doc)
    assertThat(event).isNotNull()
    assertThat(event!!.id).isEqualTo("event123")
  }

  @Test
  fun fromAny_withMap_returnsEvent() {
    val event = EventMapper.fromAny(sampleMap)
    assertThat(event).isNotNull()
    assertThat(event).isEqualTo(sampleEvent)
  }

  @Test
  fun fromAny_withInvalidType_returnsNull() {
    val event = EventMapper.fromAny("invalid")
    assertThat(event).isNull()
  }

  // --- toMap tests ---
  @Test
  fun toMap_returnsCorrectMap() {
    val map = EventMapper.toMap(sampleEvent)
    assertThat(map["id"]).isEqualTo(sampleEvent.id)
    assertThat(map["organizationId"]).isEqualTo(sampleEvent.organizationId)
    assertThat(map["title"]).isEqualTo(sampleEvent.title)
    assertThat(map["description"]).isEqualTo(sampleEvent.description)
    assertThat((map["startDate"] as Timestamp).toDate().toInstant()).isEqualTo(start)
    assertThat((map["endDate"] as Timestamp).toDate().toInstant()).isEqualTo(end)
    assertThat(map["storageStatus"]).isEqualTo(listOf("FIRESTORE"))
    assertThat(map["personalNotes"]).isEqualTo(sampleEvent.personalNotes)
    assertThat(map["participants"]).isEqualTo(listOf("participant1", "participant2"))
    assertThat(map["version"]).isEqualTo(sampleEvent.version)
    assertThat(map["presence"]).isEqualTo(sampleEvent.presence)
    assertThat(map["recurrenceStatus"]).isEqualTo(sampleEvent.recurrenceStatus.name)

    val categoryMap = map["eventCategory"] as Map<*, *>
    assertThat(categoryMap["id"]).isEqualTo(sampleEvent.category.id)
    assertThat(categoryMap["label"]).isEqualTo(sampleEvent.category.label)
    assertThat(categoryMap["color"]).isEqualTo(sampleEvent.category.color.value.toLong())
    assertThat(categoryMap["isDefault"]).isEqualTo(sampleEvent.category.isDefault)
  }
}
