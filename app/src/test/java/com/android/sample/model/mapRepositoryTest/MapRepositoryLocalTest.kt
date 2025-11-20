package com.android.sample.model.mapRepositoryTest

import com.android.sample.model.map.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MapRepositoryLocalTest {

  private lateinit var repository: MapRepositoryLocal
  private lateinit var marker1: Marker
  private lateinit var marker2: Marker
  private lateinit var marker3: Marker
  private lateinit var markerDuplicate: Marker

  @Before
  fun setup() {
    repository = MapRepositoryLocal()

    // Distinct markers
    marker1 = Marker(latitude = 48.8566, longitude = 2.3522, label = "Marker1")
    marker2 = Marker(latitude = 48.8570, longitude = 2.3530, label = "Marker2")
    marker3 = Marker(latitude = 48.8580, longitude = 2.3540, label = "Marker3")
    markerDuplicate =
        Marker(
            latitude = marker1.location.latitude,
            longitude = marker1.location.longitude,
            label = "Duplicate (Marker 1)" // same as marker1
    )

    // Add markers to repository
    repository.addMarker(marker1)
    repository.addMarker(marker2)
    repository.addMarker(marker3)
    repository.addMarker(markerDuplicate)
  }

  @Test
  fun addMarker_shouldStoreMarker() {
    val fetched = repository.getMarkerById(marker1.id)
    assertNotNull(fetched)
    assertEquals(marker1.label, fetched?.label)
    assertEquals(marker1.location.latitude, fetched?.location?.latitude)
    assertEquals(marker1.location.longitude, fetched?.location?.longitude)
  }

  @Test
  fun removeMarker_shouldDeleteMarker() {
    repository.removeMarker(marker1.id)
    val fetched = repository.getMarkerById(marker1.id)
    assertNull(fetched)
  }

  @Test
  fun getAllMarkers_shouldReturnAllMarkers() {
    val all = repository.getAllMarkers()
    assertEquals(4, all.size)
  }

  @Test
  fun createArea_withLessThan3Markers_shouldThrowException() {
    runTest {
      try {
        repository.createArea(label = "InvalidArea", markerIds = listOf(marker1.id, marker2.id))
        fail("Expected IllegalArgumentException for less than 3 distinct markers")
      } catch (_: IllegalArgumentException) {}
    }
  }

  @Test
  fun createArea_withDuplicateMarkers_shouldThrowException() {
    runTest {
      try {
        repository.createArea(
            label = "InvalidArea", markerIds = listOf(marker1.id, markerDuplicate.id, marker1.id))
        fail("Expected IllegalArgumentException for non-distinct markers")
      } catch (_: IllegalArgumentException) {}
    }
  }

  @Test
  fun createArea_withValidDistinctMarkers_shouldSucceed() {
    runTest {
      repository.createArea(
          label = "ValidArea", markerIds = listOf(marker1.id, marker2.id, marker3.id))
      val allAreas = repository.getAllAreas()
      assertEquals(1, allAreas.size)
      val area = allAreas.first()
      assertEquals("ValidArea", area.label)
      assertEquals(3, area.getSortedMarkers().size)
      // All markers must be distinct
      val distinctLocations =
          area.getSortedMarkers().map { it.location.latitude to it.location.longitude }.distinct()
      assertEquals(3, distinctLocations.size)
    }
  }

  @Test
  fun getAreaById_shouldReturnCorrectArea() {
    runTest {
      repository.createArea(
          label = "ValidArea", markerIds = listOf(marker1.id, marker2.id, marker3.id))
      val area = repository.getAllAreas().first()
      val fetched = repository.getAreaById(area.id)
      assertNotNull(fetched)
      assertEquals(area.label, fetched?.label)
    }
  }

  @Test
  fun getAllAreas_shouldReturnMultipleAreas() {
    runTest {
      repository.createArea(label = "Area1", markerIds = listOf(marker1.id, marker2.id, marker3.id))
      repository.createArea(label = "Area2", markerIds = listOf(marker2.id, marker3.id, marker1.id))
      val allAreas = repository.getAllAreas()
      assertEquals(2, allAreas.size)
      assertTrue(allAreas.any { it.label == "Area1" })
      assertTrue(allAreas.any { it.label == "Area2" })
    }
  }
}
