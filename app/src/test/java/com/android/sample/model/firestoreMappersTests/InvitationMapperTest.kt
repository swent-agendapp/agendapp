package com.android.sample.model.firestoreMappersTests

import com.android.sample.model.firestoreMappers.InvitationMapper
import com.android.sample.model.organization.invitation.Invitation
import com.android.sample.model.organization.invitation.InvitationStatus
import com.google.common.truth.Truth.assertThat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.time.Instant
import java.util.Date
import org.junit.Test
import org.mockito.Mockito.*

class InvitationMapperTest {

  private val createdAt = Instant.parse("2025-10-29T10:00:00Z")
  private val acceptedAt = Instant.parse("2025-10-29T12:00:00Z")

  private val sampleInvitation =
      Invitation(
          id = "inv123",
          organizationId = "org456",
          code = "ABCDEF",
          createdAt = createdAt,
          acceptedAt = acceptedAt,
          inviteeEmail = "john@example.com",
          status = InvitationStatus.Used)

  private val sampleMap: Map<String, Any?> =
      mapOf(
          "id" to "inv123",
          "organizationId" to "org456",
          "code" to "ABCDEF",
          "createdAt" to Timestamp(Date.from(createdAt)),
          "acceptedAt" to Timestamp(Date.from(acceptedAt)),
          "inviteeEmail" to "john@example.com",
          "status" to "Used")

  // --- fromDocument tests ---
  @Test
  fun fromDocument_withValidDocument_returnsInvitation() {
    val doc = mock(DocumentSnapshot::class.java)

    `when`(doc.getString("id")).thenReturn("inv123")
    `when`(doc.getString("organizationId")).thenReturn("org456")
    `when`(doc.getString("code")).thenReturn("ABCDEF")
    `when`(doc.getTimestamp("createdAt")).thenReturn(Timestamp(Date.from(createdAt)))
    `when`(doc.getTimestamp("acceptedAt")).thenReturn(Timestamp(Date.from(acceptedAt)))
    `when`(doc.getString("inviteeEmail")).thenReturn("john@example.com")
    `when`(doc.getString("status")).thenReturn("Used")

    val invitation = InvitationMapper.fromDocument(doc)

    assertThat(invitation).isNotNull()
    assertThat(invitation).isEqualTo(sampleInvitation)
  }

  @Test
  fun fromDocument_missingRequiredFields_returnsNull() {
    val doc = mock(DocumentSnapshot::class.java)

    `when`(doc.getString("id")).thenReturn(null)

    val result = InvitationMapper.fromDocument(doc)
    assertThat(result).isNull()
  }

  // --- fromMap tests ---
  @Test
  fun fromMap_withValidData_returnsInvitation() {
    val invitation = InvitationMapper.fromMap(sampleMap)
    assertThat(invitation).isNotNull()
    assertThat(invitation).isEqualTo(sampleInvitation)
  }

  @Test
  fun fromMap_missingRequiredFields_returnsNull() {
    val invalidMap = sampleMap - "code"
    val result = InvitationMapper.fromMap(invalidMap)
    assertThat(result).isNull()
  }

  @Test
  fun fromMap_withDifferentDateTypes_parsesCorrectly() {
    // Date form
    val mapWithDate = sampleMap.toMutableMap()
    mapWithDate["createdAt"] = Date.from(createdAt)
    mapWithDate["acceptedAt"] = Date.from(acceptedAt)

    val inv1 = InvitationMapper.fromMap(mapWithDate)
    assertThat(inv1).isEqualTo(sampleInvitation)

    // Long form
    val mapWithLong = sampleMap.toMutableMap()
    mapWithLong["createdAt"] = createdAt.toEpochMilli()
    mapWithLong["acceptedAt"] = acceptedAt.toEpochMilli()

    val inv2 = InvitationMapper.fromMap(mapWithLong)
    assertThat(inv2).isEqualTo(sampleInvitation)
  }

  // --- fromAny tests ---
  @Test
  fun fromAny_withDocument_returnsInvitation() {
    val doc = mock(DocumentSnapshot::class.java)

    `when`(doc.getString("id")).thenReturn("inv123")
    `when`(doc.getString("organizationId")).thenReturn("org456")
    `when`(doc.getString("code")).thenReturn("ABCDEF")
    `when`(doc.getTimestamp("createdAt")).thenReturn(Timestamp(Date.from(createdAt)))
    `when`(doc.getTimestamp("acceptedAt")).thenReturn(Timestamp(Date.from(acceptedAt)))
    `when`(doc.getString("inviteeEmail")).thenReturn("john@example.com")
    `when`(doc.getString("status")).thenReturn("Used")

    val invitation = InvitationMapper.fromAny(doc)
    assertThat(invitation).isNotNull()
    assertThat(invitation!!.id).isEqualTo("inv123")
  }

  @Test
  fun fromAny_withMap_returnsInvitation() {
    val result = InvitationMapper.fromAny(sampleMap)
    assertThat(result).isNotNull()
    assertThat(result).isEqualTo(sampleInvitation)
  }

  @Test
  fun fromAny_withInvalidType_returnsNull() {
    val result = InvitationMapper.fromAny(42)
    assertThat(result).isNull()
  }

  // --- toMap tests ---
  @Test
  fun toMap_returnsCorrectMap() {
    val map = InvitationMapper.toMap(sampleInvitation)

    assertThat(map["id"]).isEqualTo(sampleInvitation.id)
    assertThat(map["organizationId"]).isEqualTo(sampleInvitation.organizationId)
    assertThat(map["code"]).isEqualTo(sampleInvitation.code)
    assertThat((map["createdAt"] as Timestamp).toDate().toInstant()).isEqualTo(createdAt)
    assertThat((map["acceptedAt"] as Timestamp).toDate().toInstant()).isEqualTo(acceptedAt)
    assertThat(map["inviteeEmail"]).isEqualTo("john@example.com")
    assertThat(map["status"]).isEqualTo("Used")
  }
}
