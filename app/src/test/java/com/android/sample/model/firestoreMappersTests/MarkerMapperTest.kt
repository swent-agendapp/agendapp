package com.android.sample.model.firestoreMappersTests

import com.android.sample.model.firestoreMappers.MarkerMapper
import com.android.sample.model.map.Location
import com.android.sample.model.map.Marker
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.DocumentSnapshot
import org.junit.Test
import org.mockito.Mockito.*

class MarkerMapperTest {

  private val sampleLocation = Location(latitude = 10.0, longitude = 20.0, label = "Loc Label")
  private val sampleMapLocation =
      mapOf("latitude" to 10.0, "longitude" to 20.0, "label" to "Loc Label")

  private val sampleMarker =
      Marker(id = "marker123", label = "Marker Label", location = sampleLocation)

  private val sampleMap: Map<String, Any?> =
      mapOf("id" to "marker123", "label" to "Marker Label", "location" to sampleMapLocation)

  // --- fromDocument tests ---
  @Test
  fun fromDocument_withValidDocument_returnsMarker() {
    val locationDoc = mock(DocumentSnapshot::class.java)
    `when`(locationDoc.getDouble("latitude")).thenReturn(10.0)
    `when`(locationDoc.getDouble("longitude")).thenReturn(20.0)
    `when`(locationDoc.getString("label")).thenReturn("Loc Label")

    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.getString("id")).thenReturn("marker123")
    `when`(doc.getString("label")).thenReturn("Marker Label")
    `when`(doc.get("location")).thenReturn(locationDoc)
    `when`(doc.id).thenReturn("fallbackId")

    val marker = MarkerMapper.fromDocument(doc)
    assertThat(marker).isNotNull()
    assertThat(marker).isEqualTo(sampleMarker)
  }

  @Test
  fun fromDocument_missingLocation_returnsNull() {
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.getString("id")).thenReturn("marker123")
    `when`(doc.getString("label")).thenReturn("Marker Label")
    `when`(doc.get("location")).thenReturn(null)
    `when`(doc.id).thenReturn("fallbackId")

    val marker = MarkerMapper.fromDocument(doc)
    assertThat(marker).isNull()
  }

  @Test
  fun fromDocument_missingId_usesDocumentId() {
    val locationDoc = mock(DocumentSnapshot::class.java)
    `when`(locationDoc.getDouble("latitude")).thenReturn(10.0)
    `when`(locationDoc.getDouble("longitude")).thenReturn(20.0)

    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.getString("id")).thenReturn(null)
    `when`(doc.get("location")).thenReturn(locationDoc)
    `when`(doc.id).thenReturn("fallbackId")

    val marker = MarkerMapper.fromDocument(doc)
    assertThat(marker).isNotNull()
    assertThat(marker!!.id).isEqualTo("fallbackId")
  }

  // --- fromMap tests ---
  @Test
  fun fromMap_withValidMap_returnsMarker() {
    val marker = MarkerMapper.fromMap(sampleMap)
    assertThat(marker).isNotNull()
    assertThat(marker).isEqualTo(sampleMarker)
  }

  @Test
  fun fromMap_missingLocation_returnsNull() {
    val mapWithoutLocation = sampleMap - "location"
    val marker = MarkerMapper.fromMap(mapWithoutLocation)
    assertThat(marker).isNull()
  }

  @Test
  fun fromMap_missingId_returnsNull() {
    val mapWithoutId = sampleMap - "id"
    val marker = MarkerMapper.fromMap(mapWithoutId)
    assertThat(marker).isNull()
  }

  // --- fromAny tests ---
  @Test
  fun fromAny_withDocument_returnsMarker() {
    val locationDoc = mock(DocumentSnapshot::class.java)
    `when`(locationDoc.getDouble("latitude")).thenReturn(10.0)
    `when`(locationDoc.getDouble("longitude")).thenReturn(20.0)

    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.getString("id")).thenReturn("marker123")
    `when`(doc.get("location")).thenReturn(locationDoc)
    `when`(doc.id).thenReturn("fallbackId")

    val marker = MarkerMapper.fromAny(doc)
    assertThat(marker).isNotNull()
    assertThat(marker!!.id).isEqualTo("marker123")
  }

  @Test
  fun fromAny_withMap_returnsMarker() {
    val marker = MarkerMapper.fromAny(sampleMap)
    assertThat(marker).isNotNull()
    assertThat(marker).isEqualTo(sampleMarker)
  }

  @Test
  fun fromAny_withInvalidType_returnsNull() {
    val marker = MarkerMapper.fromAny("invalid")
    assertThat(marker).isNull()
  }

  // --- toMap tests ---
  @Test
  fun toMap_returnsCorrectMap() {
    val map = MarkerMapper.toMap(sampleMarker)
    assertThat(map["id"]).isEqualTo(sampleMarker.id)
    assertThat(map["label"]).isEqualTo(sampleMarker.label)

    val locationMap = map["location"] as Map<*, *>
    assertThat(locationMap["latitude"]).isEqualTo(sampleLocation.latitude)
    assertThat(locationMap["longitude"]).isEqualTo(sampleLocation.longitude)
    assertThat(locationMap["label"]).isEqualTo(sampleLocation.label)
  }
}
