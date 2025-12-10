package com.android.sample.model.mapRepositoryTest

import com.android.sample.model.map.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MapRepositoryLocalTest {

  private lateinit var repository: MapRepositoryLocal
  private lateinit var marker1: Marker

  private val orgId = "testOrg"

  @Before
  fun setup() {
    repository = MapRepositoryLocal()

    marker1 = Marker(latitude = 48.8566, longitude = 2.3522, label = "Marker1")
  }

  @Test
  fun createArea_with_negative_area_shouldThrowException() {
    runTest {
      try {
        repository.createArea(
            orgId = orgId, label = "InvalidArea", marker = marker1, radius = -10.0)
        fail("Expected IllegalArgumentException for non-distinct markers")
      } catch (_: IllegalArgumentException) {}
    }
  }

  @Test
  fun createArea_shouldSucceed() {
    runTest {
      repository.createArea(orgId = orgId, label = "ValidArea", marker = marker1, radius = 10.0)
      val allAreas = repository.getAllAreas(orgId)
      assertEquals(1, allAreas.size)
      val area = allAreas.first()
      assertEquals("ValidArea", area.label)
      assertEquals("Marker1", area.marker.label)
      assertEquals(10.0, area.radius, 0.00)
    }
  }

  @Test
  fun getAllAreas_shouldReturnMultipleAreas() {
    runTest {
      repository.createArea(orgId = orgId, label = "Area1", marker = marker1, radius = 10.0)
      repository.createArea(orgId = orgId, label = "Area2", marker = marker1, radius = 10.0)
      val allAreas = repository.getAllAreas(orgId)
      assertEquals(2, allAreas.size)
      assertTrue(allAreas.any { it.label == "Area1" })
      assertTrue(allAreas.any { it.label == "Area2" })
    }
  }

  @Test
  fun deleteArea_shouldReturnNoArea() {
    runTest {
      repository.createArea(orgId = orgId, label = "Area1", marker = marker1, radius = 10.0)
      val areaId = repository.getAllAreas(orgId).first().id
      repository.deleteArea(orgId, areaId)
      val allAreas = repository.getAllAreas(orgId)

      assertEquals(0, allAreas.size)
    }
  }

  @Test
  fun modifyArea_shouldReturnNewArea() {
    runTest {
      repository.createArea(orgId = orgId, label = "Area1", marker = marker1, radius = 10.0)
      val newArea = repository.getAllAreas(orgId).first()
      repository.updateArea(newArea.id, orgId, label = "AreaNew", newArea.marker, newArea.radius)
      val allAreas = repository.getAllAreas(orgId)

      assertEquals(1, allAreas.size)
      assertTrue(allAreas.any { it.label == "AreaNew" })
    }
  }
}
