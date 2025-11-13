package com.android.sample.model.firestoreMappersTests

import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.firestoreMappers.EventMapper
import com.android.sample.model.firestoreMappers.ReplacementMapper
import com.android.sample.model.replacement.Replacement
import com.android.sample.model.replacement.ReplacementStatus
import com.android.sample.utils.EventColor
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.DocumentSnapshot
import java.time.Instant
import org.junit.Test
import org.mockito.Mockito.*

// Assisted by AI

class ReplacementMapperTest {

  private val sampleEvent =
      Event(
          id = "event1",
          title = "Meeting",
          description = "Team meeting",
          startDate = Instant.parse("2025-11-12T10:00:00Z"),
          endDate = Instant.parse("2025-11-12T11:00:00Z"),
          cloudStorageStatuses = emptySet(),
          personalNotes = null,
          participants = emptySet(),
          version = 0L,
          recurrenceStatus = RecurrenceStatus.OneTime,
          color = EventColor.Blue)

  private val sampleReplacement =
      Replacement(
          id = "replacement1",
          absentUserId = "user123",
          substituteUserId = "user456",
          event = sampleEvent,
          status = ReplacementStatus.Pending)

  private val sampleMap: Map<String, Any?> =
      mapOf(
          "id" to "replacement1",
          "absentUserId" to "user123",
          "substituteUserId" to "user456",
          "status" to "Pending",
          "event" to EventMapper.toMap(sampleEvent))

  // --- fromDocument tests ---
  @Test
  fun fromDocument_withValidDocument_returnsReplacement() {
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.id).thenReturn("replacement1")
    `when`(doc.getString("absentUserId")).thenReturn("user123")
    `when`(doc.getString("substituteUserId")).thenReturn("user456")
    `when`(doc.getString("status")).thenReturn("Pending")
    `when`(doc.get("event")).thenReturn(EventMapper.toMap(sampleEvent))

    val replacement = ReplacementMapper.fromDocument(doc)
    assertThat(replacement).isNotNull()
    assertThat(replacement).isEqualTo(sampleReplacement)
  }

  @Test
  fun fromDocument_missingRequiredFields_returnsNull() {
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.id).thenReturn("replacement1")
    `when`(doc.getString("absentUserId")).thenReturn(null) // required field missing

    val replacement = ReplacementMapper.fromDocument(doc)
    assertThat(replacement).isNull()
  }

  // --- fromMap tests ---
  @Test
  fun fromMap_withValidData_returnsReplacement() {
    val replacement = ReplacementMapper.fromMap(sampleMap)
    assertThat(replacement).isNotNull()
    assertThat(replacement).isEqualTo(sampleReplacement)
  }

  @Test
  fun fromMap_missingRequiredFields_returnsNull() {
    val invalidMap = sampleMap - "absentUserId"
    val replacement = ReplacementMapper.fromMap(invalidMap)
    assertThat(replacement).isNull()
  }

  @Test
  fun fromMap_withInvalidStatus_defaultsToPending() {
    val map = sampleMap.toMutableMap()
    map["status"] = "INVALID_STATUS"
    val replacement = ReplacementMapper.fromMap(map)
    assertThat(replacement).isNotNull()
    assertThat(replacement!!.status).isEqualTo(ReplacementStatus.Pending)
  }

  // --- fromAny tests ---
  @Test
  fun fromAny_withDocument_returnsReplacement() {
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.id).thenReturn("replacement1")
    `when`(doc.getString("absentUserId")).thenReturn("user123")
    `when`(doc.getString("substituteUserId")).thenReturn("user456")
    `when`(doc.getString("status")).thenReturn("Pending")
    `when`(doc.get("event")).thenReturn(EventMapper.toMap(sampleEvent))

    val replacement = ReplacementMapper.fromAny(doc)
    assertThat(replacement).isNotNull()
    assertThat(replacement!!.id).isEqualTo("replacement1")
  }

  @Test
  fun fromAny_withMap_returnsReplacement() {
    val replacement = ReplacementMapper.fromAny(sampleMap)
    assertThat(replacement).isNotNull()
    assertThat(replacement).isEqualTo(sampleReplacement)
  }

  @Test
  fun fromAny_withInvalidType_returnsNull() {
    val replacement = ReplacementMapper.fromAny("invalid")
    assertThat(replacement).isNull()
  }

  // --- toMap tests ---
  @Test
  fun toMap_returnsCorrectMap() {
    val map = ReplacementMapper.toMap(sampleReplacement)
    assertThat(map["id"]).isEqualTo("replacement1")
    assertThat(map["absentUserId"]).isEqualTo("user123")
    assertThat(map["substituteUserId"]).isEqualTo("user456")
    assertThat(map["status"]).isEqualTo("Pending")
    assertThat(map["event"]).isEqualTo(EventMapper.toMap(sampleEvent))
  }
}
