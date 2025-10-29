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
    val markers = createMockMarkers()
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.getString("id")).thenReturn("area123")
    `when`(doc.getString("label")).thenReturn("My Area")
    `when`(doc.get("markers")).thenReturn(markers)

    val area = AreaMapper.fromDocument(doc)
    assertValidArea(area)
  }

  @Test
  fun fromMap_withValidData_returnsArea() {
    val markers =
        listOf(
            Marker("marker1", Location(10.0, 20.0, "Loc 1"), "Marker 1"),
            Marker("marker2", Location(15.0, 25.0, "Loc 2"), "Marker 2"),
            Marker("marker3", Location(12.0, 22.0, "Loc 3"), "Marker 3"))

    val markersData =
        markers.map { marker ->
          mapOf(
              "id" to marker.id,
              "label" to marker.label,
              "location" to
                  mapOf(
                      "latitude" to marker.location.latitude,
                      "longitude" to marker.location.longitude,
                      "label" to marker.location.label))
        }

    val data = mapOf("id" to "area123", "label" to "My Area", "markers" to markersData)

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
    val markers =
        listOf(
            Marker(id = "m1", location = Location(10.0, 20.0), label = "Marker 1"),
            Marker(id = "m2", location = Location(15.0, 25.0), label = "Marker 2"),
            Marker(id = "m3", location = Location(12.0, 22.0), label = "Marker 3"))

    val area = Area(id = "area123", label = "My Area", markers = markers)
    val map = AreaMapper.toMap(area)

    assertThat(map["id"]).isEqualTo("area123")
    assertThat(map["label"]).isEqualTo("My Area")
    val markersList =
        (map["markers"] as? List<*>)?.filterIsInstance<Map<String, Any?>>() ?: emptyList()
    assertThat(markersList.size).isEqualTo(3)
    assertThat(markersList.map { it["label"] }).containsExactly("Marker 1", "Marker 2", "Marker 3")
  }

  // --- Helpers ---
  private fun createMockMarkers(): List<DocumentSnapshot> {
    val loc1 =
        mock(DocumentSnapshot::class.java).apply {
          `when`(getDouble("latitude")).thenReturn(10.0)
          `when`(getDouble("longitude")).thenReturn(20.0)
          `when`(getString("label")).thenReturn("Loc 1")
        }
    val loc2 =
        mock(DocumentSnapshot::class.java).apply {
          `when`(getDouble("latitude")).thenReturn(15.0)
          `when`(getDouble("longitude")).thenReturn(25.0)
          `when`(getString("label")).thenReturn("Loc 2")
        }
    val loc3 =
        mock(DocumentSnapshot::class.java).apply {
          `when`(getDouble("latitude")).thenReturn(12.0)
          `when`(getDouble("longitude")).thenReturn(22.0)
          `when`(getString("label")).thenReturn("Loc 3")
        }

    return listOf(
        mock(DocumentSnapshot::class.java).apply {
          `when`(getString("id")).thenReturn("marker1")
          `when`(getString("label")).thenReturn("Marker 1")
          `when`(get("location")).thenReturn(loc1)
        },
        mock(DocumentSnapshot::class.java).apply {
          `when`(getString("id")).thenReturn("marker2")
          `when`(getString("label")).thenReturn("Marker 2")
          `when`(get("location")).thenReturn(loc2)
        },
        mock(DocumentSnapshot::class.java).apply {
          `when`(getString("id")).thenReturn("marker3")
          `when`(getString("label")).thenReturn("Marker 3")
          `when`(get("location")).thenReturn(loc3)
        })
  }

  private fun assertValidArea(area: Area?) {
    assertThat(area).isNotNull()
    area!!
    assertThat(area.id).isEqualTo("area123")
    assertThat(area.label).isEqualTo("My Area")
    assertThat(area.getSortedMarkers().size).isEqualTo(3)
    assertThat(area.getSortedMarkers().map { it.label })
        .containsExactly("Marker 1", "Marker 2", "Marker 3")
  }
}
