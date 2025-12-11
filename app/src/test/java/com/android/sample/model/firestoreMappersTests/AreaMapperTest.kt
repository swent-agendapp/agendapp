package com.android.sample.model.firestoreMappersTests

import com.android.sample.model.firestoreMappers.AreaMapper
import com.android.sample.model.map.Area
import com.android.sample.model.map.Location
import com.android.sample.model.map.Marker
import com.google.common.truth.Truth.assertThat
import com.google.firebase.firestore.DocumentSnapshot
import org.junit.Test
import org.mockito.Mockito.*

class AreaMapperTest {

  @Test
  fun fromDocument_withValidDocument_returnsArea() {
    val markers = createMockMarkers() // should match the expected type
    val doc = mock(DocumentSnapshot::class.java)

    `when`(doc.getString("id")).thenReturn("area123")
    `when`(doc.getString("label")).thenReturn("My Area")
    `when`(doc.get("marker")).thenReturn(markers)
    `when`(doc.getDouble("radius")).thenReturn(10.0)

    val area = AreaMapper.fromDocument(doc)

    assertValidArea(area)
  }

  @Test
  fun fromMap_withValidData_returnsArea() {
    val marker = Marker("marker1", Location(10.0, 20.0), "Marker 1")

    val markerData =
        mapOf(
            "id" to marker.id,
            "label" to marker.label,
            "location" to
                mapOf(
                    "latitude" to marker.location.latitude,
                    "longitude" to marker.location.longitude))

    val data =
        mapOf("id" to "area123", "label" to "My Area", "marker" to markerData, "radius" to 10.0)

    val area = AreaMapper.fromMap(data)
    assertValidArea(area)
  }

  @Test
  fun fromDocument_withInvalidMarkers_returnsNull() {
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.getString("id")).thenReturn("area123")
    `when`(doc.getString("label")).thenReturn("My Area")
    `when`(doc.get("markers")).thenReturn(listOf("not a valid marker"))

    val area = AreaMapper.fromDocument(doc)
    assertThat(area).isNull()
  }

  @Test
  fun fromMap_withInvalidMarkers_returnsNull() {
    val data = mapOf("id" to "area123", "label" to "My Area", "markers" to listOf("invalid marker"))

    val area = AreaMapper.fromMap(data)
    assertThat(area).isNull()
  }

  @Test
  fun toMap_returnsCorrectMap() {
    val marker = Marker(id = "m1", location = Location(10.0, 20.0), label = "Marker 1")

    val area = Area(id = "area123", label = "My Area", marker = marker, radius = 10.0)
    val map = AreaMapper.toMap(area)

    assertThat(map["id"]).isEqualTo("area123")
    assertThat(map["label"]).isEqualTo("My Area")
    assertThat(map["radius"]).isEqualTo(10.0)
  }

  // --- Helpers ---
  private fun createMockMarkers(): DocumentSnapshot {
    val loc1 =
        mock(DocumentSnapshot::class.java).apply {
          `when`(getDouble("latitude")).thenReturn(10.0)
          `when`(getDouble("longitude")).thenReturn(20.0)
          `when`(getString("label")).thenReturn("Loc 1")
        }

    return mock(DocumentSnapshot::class.java).apply {
      `when`(getString("id")).thenReturn("marker1")
      `when`(getString("label")).thenReturn("Marker 1")
      `when`(get("location")).thenReturn(loc1)
    }
  }

  private fun assertValidArea(area: Area?) {
    assertThat(area).isNotNull()
    area!!
    assertThat(area.id).isEqualTo("area123")
    assertThat(area.label).isEqualTo("My Area")
    assertThat(area.marker.label).isEqualTo("Marker 1")
    assertThat(area.radius).isEqualTo(10.0)
  }
}
