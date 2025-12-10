package com.android.sample.model.firestoreMappersTests

import com.android.sample.model.authentication.User
import com.android.sample.model.calendar.createEvent
import com.android.sample.model.firestoreMappers.OrganizationMapper
import com.android.sample.model.map.Area
import com.android.sample.model.map.Location
import com.android.sample.model.map.Marker
import com.android.sample.model.organization.data.Organization
import com.google.common.truth.Truth.assertThat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.time.Instant
import java.util.Date
import org.junit.Test
import org.mockito.Mockito.*

class OrganizationMapperTest {

  @Test
  fun fromDocument_withValidData_returnsOrganization() {
    // Admins & members
    val adminDoc = mock(DocumentSnapshot::class.java)
    `when`(adminDoc.getString("id")).thenReturn("admin1")
    `when`(adminDoc.getString("displayName")).thenReturn("Admin One")
    `when`(adminDoc.getString("email")).thenReturn("admin1@example.com")

    val memberDoc = mock(DocumentSnapshot::class.java)
    `when`(memberDoc.getString("id")).thenReturn("member1")
    `when`(memberDoc.getString("displayName")).thenReturn("Member One")
    `when`(memberDoc.getString("email")).thenReturn("member1@example.com")

    // Marker locations
    val locDoc1 = mock(DocumentSnapshot::class.java)
    `when`(locDoc1.getDouble("latitude")).thenReturn(10.0)
    `when`(locDoc1.getDouble("longitude")).thenReturn(20.0)
    `when`(locDoc1.getString("label")).thenReturn("Loc1")

    // Marker documents
    val markerDoc1 = mock(DocumentSnapshot::class.java)
    `when`(markerDoc1.getString("id")).thenReturn("m1")
    `when`(markerDoc1.getString("label")).thenReturn("Marker 1")
    `when`(markerDoc1.get("location")).thenReturn(locDoc1)

    // Area document
    val areaDoc = mock(DocumentSnapshot::class.java)
    `when`(areaDoc.getString("id")).thenReturn("area1")
    `when`(areaDoc.getString("label")).thenReturn("Main Area")
    `when`(areaDoc.get("marker")).thenReturn(markerDoc1)
    `when`(areaDoc.get("radius")).thenReturn(10.0)

    // Event document
    val eventDoc = mock(DocumentSnapshot::class.java)
    `when`(eventDoc.id).thenReturn("event1")
    `when`(eventDoc.getString("organizationId")).thenReturn("org123")
    `when`(eventDoc.getString("title")).thenReturn("Meeting")
    `when`(eventDoc.getString("description")).thenReturn("Team meeting")
    `when`(eventDoc.getTimestamp("startDate"))
        .thenReturn(Timestamp(Date.from(Instant.parse("2025-01-01T10:00:00Z"))))
    `when`(eventDoc.getTimestamp("endDate"))
        .thenReturn(Timestamp(Date.from(Instant.parse("2025-01-01T11:00:00Z"))))
    `when`(eventDoc.get("participants")).thenReturn(listOf("admin1", "member1"))

    // Organization document
    val orgDoc = mock(DocumentSnapshot::class.java)
    `when`(orgDoc.id).thenReturn("org123")
    `when`(orgDoc.getString("name")).thenReturn("My Organization")
    `when`(orgDoc.getBoolean("geoCheckEnabled")).thenReturn(true)
    `when`(orgDoc["admins"]).thenReturn(listOf(adminDoc))
    `when`(orgDoc["members"]).thenReturn(listOf(memberDoc))
    `when`(orgDoc["events"]).thenReturn(listOf(eventDoc))

    val organization = OrganizationMapper.fromDocument(orgDoc)

    assertThat(organization).isNotNull()
    organization!!
    assertThat(organization.id).isEqualTo("org123")
    assertThat(organization.name).isEqualTo("My Organization")
    assertThat(organization.geoCheckEnabled).isTrue()

    assertThat(organization.admins).hasSize(1)
    assertThat(organization.admins[0].displayName).isEqualTo("Admin One")

    assertThat(organization.members).hasSize(1)
    assertThat(organization.members[0].email).isEqualTo("member1@example.com")

    assertThat(organization.events).hasSize(1)
    val event = organization.events[0]
    assertThat(event.title).isEqualTo("Meeting")
    assertThat(event.description).isEqualTo("Team meeting")
  }

  @Test
  fun fromDocument_missingName_returnsNull() {
    val orgDoc = mock(DocumentSnapshot::class.java)
    `when`(orgDoc.id).thenReturn("org123")
    `when`(orgDoc.getString("name")).thenReturn(null)

    val organization = OrganizationMapper.fromDocument(orgDoc)
    assertThat(organization).isNull()
  }

  @Test
  fun fromMap_withValidData_returnsOrganization() {
    val admins =
        listOf(
            mapOf("id" to "admin1", "displayName" to "Admin One", "email" to "admin1@example.com"))
    val members =
        listOf(
            mapOf(
                "id" to "member1", "displayName" to "Member One", "email" to "member1@example.com"))
    val events =
        listOf(
            mapOf(
                "id" to "event1",
                "organizationId" to "org123",
                "title" to "Meeting",
                "description" to "Team meeting",
                "startDate" to Timestamp(Date.from(Instant.parse("2025-01-01T10:00:00Z"))),
                "endDate" to Timestamp(Date.from(Instant.parse("2025-01-01T11:00:00Z"))),
                "participants" to listOf("admin1", "member1")))

    val data =
        mapOf(
            "id" to "org123",
            "name" to "My Organization",
            "geoCheckEnabled" to true,
            "admins" to admins,
            "members" to members,
            "events" to events)

    val organization = OrganizationMapper.fromMap(data)

    assertThat(organization).isNotNull()
    organization!!
    assertThat(organization.id).isEqualTo("org123")
    assertThat(organization.name).isEqualTo("My Organization")
    assertThat(organization.geoCheckEnabled).isTrue()
    assertThat(organization.admins).hasSize(1)
    assertThat(organization.admins[0].displayName).isEqualTo("Admin One")
    assertThat(organization.members).hasSize(1)
    assertThat(organization.members[0].email).isEqualTo("member1@example.com")
    assertThat(organization.events).hasSize(1)
    assertThat(organization.events[0].title).isEqualTo("Meeting")
  }

  @Test
  fun toMap_returnsCorrectMap() {
    val admins = listOf(User("admin1", "Admin One", "admin1@example.com"))
    val members = listOf(User("member1", "Member One", "member1@example.com"))
    val events =
        createEvent(
            organizationId = "org123",
            title = "Meeting",
            description = "Team meeting",
            startDate = Instant.parse("2025-01-01T10:00:00Z"),
            endDate = Instant.parse("2025-01-01T11:00:00Z"))
    val organization =
        Organization(
            id = "org123",
            name = "My Organization",
            admins = admins,
            members = members,
            events = events,
            geoCheckEnabled = true)

    val map = OrganizationMapper.toMap(organization)

    assertThat(map["id"]).isEqualTo("org123")
    assertThat(map["name"]).isEqualTo("My Organization")
    assertThat(map["geoCheckEnabled"]).isEqualTo(true)

    val adminsList = (map["admins"] as? List<*>)?.filterIsInstance<Map<String, Any?>>()
    val membersList = (map["members"] as? List<*>)?.filterIsInstance<Map<String, Any?>>()
    val eventsList = (map["events"] as? List<*>)?.filterIsInstance<Map<String, Any?>>()

    assertThat(adminsList!![0]["displayName"]).isEqualTo("Admin One")
    assertThat(membersList!![0]["email"]).isEqualTo("member1@example.com")
    assertThat(eventsList!![0]["title"]).isEqualTo("Meeting")
    assertThat(eventsList[0]["description"]).isEqualTo("Team meeting")
  }
}
