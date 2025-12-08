package com.android.sample.model.firestoreMappersTests

import com.android.sample.data.firebase.mappers.InvitationMapper
import com.android.sample.model.authentication.User
import com.android.sample.model.organization.data.Organization
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

  // --- Create users ---
  private val admin = User(id = "adminA", displayName = "Admin A", email = "adminA@example.com")
  private val member = User(id = "memberA", displayName = "Member A", email = "memberA@example.com")

  // --- Create organization ---
  private val sampleOrg =
      Organization(
          id = "orgA", name = "Org A", admins = listOf(admin), members = listOf(member, admin))

  private val sampleInvitation =
      Invitation(
          id = "inv123",
          organizationId = sampleOrg.id,
          code = "ABCDEF",
          createdAt = createdAt,
          acceptedAt = acceptedAt,
          inviteeEmail = "john@example.com",
          status = InvitationStatus.Used)

  private val sampleMap: Map<String, Any?> =
      mapOf(
          InvitationMapper.ID_FIELD to "inv123",
          InvitationMapper.ORGANIZATION_ID_FIELD to sampleOrg.id,
          InvitationMapper.CODE_FIELD to "ABCDEF",
          InvitationMapper.CREATED_AT_FIELD to Timestamp(Date.from(createdAt)),
          InvitationMapper.ACCEPTED_AT_FIELD to Timestamp(Date.from(acceptedAt)),
          InvitationMapper.INVITEE_EMAIL_FIELD to "john@example.com",
          InvitationMapper.STATUS_FIELD to "Used")

  // --- fromDocument tests ---
  @Test
  fun fromDocument_withValidDocument_returnsInvitation() {
    val doc = mock(DocumentSnapshot::class.java)

    `when`(doc.getString(InvitationMapper.ID_FIELD)).thenReturn(sampleInvitation.id)
    `when`(doc.getString(InvitationMapper.ORGANIZATION_ID_FIELD)).thenReturn(sampleOrg.id)
    `when`(doc.getString(InvitationMapper.CODE_FIELD)).thenReturn(sampleInvitation.code)
    `when`(doc.getTimestamp(InvitationMapper.CREATED_AT_FIELD))
        .thenReturn(Timestamp(Date.from(createdAt)))
    `when`(doc.getTimestamp(InvitationMapper.ACCEPTED_AT_FIELD))
        .thenReturn(Timestamp(Date.from(acceptedAt)))
    `when`(doc.getString(InvitationMapper.INVITEE_EMAIL_FIELD))
        .thenReturn(sampleInvitation.inviteeEmail)
    `when`(doc.getString(InvitationMapper.STATUS_FIELD)).thenReturn(sampleInvitation.status.name)

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
    mapWithDate[InvitationMapper.CREATED_AT_FIELD] = Date.from(createdAt)
    mapWithDate[InvitationMapper.ACCEPTED_AT_FIELD] = Date.from(acceptedAt)

    val inv1 = InvitationMapper.fromMap(mapWithDate)
    assertThat(inv1).isEqualTo(sampleInvitation)

    // Long form
    val mapWithLong = sampleMap.toMutableMap()
    mapWithLong[InvitationMapper.CREATED_AT_FIELD] = createdAt.toEpochMilli()
    mapWithLong[InvitationMapper.ACCEPTED_AT_FIELD] = acceptedAt.toEpochMilli()

    val inv2 = InvitationMapper.fromMap(mapWithLong)
    assertThat(inv2).isEqualTo(sampleInvitation)
  }

  // --- fromAny tests ---
  @Test
  fun fromAny_withDocument_returnsInvitation() {
    val doc = mock(DocumentSnapshot::class.java)

    `when`(doc.getString(InvitationMapper.ID_FIELD)).thenReturn(sampleInvitation.id)
    `when`(doc.getString(InvitationMapper.ORGANIZATION_ID_FIELD)).thenReturn(sampleOrg.id)
    `when`(doc.getString(InvitationMapper.CODE_FIELD)).thenReturn(sampleInvitation.code)
    `when`(doc.getTimestamp(InvitationMapper.CREATED_AT_FIELD))
        .thenReturn(Timestamp(Date.from(createdAt)))
    `when`(doc.getTimestamp(InvitationMapper.ACCEPTED_AT_FIELD))
        .thenReturn(Timestamp(Date.from(acceptedAt)))
    `when`(doc.getString(InvitationMapper.INVITEE_EMAIL_FIELD))
        .thenReturn(sampleInvitation.inviteeEmail)
    `when`(doc.getString(InvitationMapper.STATUS_FIELD)).thenReturn(sampleInvitation.status.name)

    val invitation = InvitationMapper.fromAny(doc)
    assertThat(invitation).isNotNull()
    assertThat(invitation).isEqualTo(sampleInvitation)
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

    assertThat(map[InvitationMapper.ID_FIELD]).isEqualTo(sampleInvitation.id)
    assertThat(map[InvitationMapper.ORGANIZATION_ID_FIELD])
        .isEqualTo(sampleInvitation.organizationId)
    assertThat(map[InvitationMapper.CODE_FIELD]).isEqualTo(sampleInvitation.code)
    assertThat((map[InvitationMapper.CREATED_AT_FIELD] as Timestamp).toDate().toInstant())
        .isEqualTo(createdAt)
    assertThat((map[InvitationMapper.ACCEPTED_AT_FIELD] as Timestamp).toDate().toInstant())
        .isEqualTo(acceptedAt)
    assertThat(map[InvitationMapper.INVITEE_EMAIL_FIELD]).isEqualTo("john@example.com")
    assertThat(map[InvitationMapper.STATUS_FIELD]).isEqualTo("Used")
  }
}
