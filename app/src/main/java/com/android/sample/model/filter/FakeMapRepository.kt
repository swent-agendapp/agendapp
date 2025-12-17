package com.android.sample.model.filter

import com.android.sample.model.map.Area
import com.android.sample.model.map.MapRepository
import com.android.sample.model.map.Marker

// Assisted by AI

/**
 * In-memory fake implementation of [MapRepository] for unit tests.
 *
 * This fake behaves like a real repository:
 * - Stores areas in memory
 * - Supports create, update, delete
 * - Returns deterministic results
 *
 * It is suitable for ViewModel and domain-level tests.
 */
class FakeMapRepository : MapRepository {

  private val areasByOrg = mutableMapOf<String, MutableList<Area>>()

  override suspend fun getAllAreas(orgId: String): List<Area> {
    return areasByOrg[orgId]?.toList() ?: emptyList()
  }

  override suspend fun createArea(orgId: String, label: String, marker: Marker, radius: Double) {
    val newArea =
        Area(id = "area-${System.nanoTime()}", label = label, marker = marker, radius = radius)

    val list = areasByOrg.getOrPut(orgId) { mutableListOf() }
    list.add(newArea)
  }

  override suspend fun updateArea(
      areaId: String,
      orgId: String,
      label: String,
      marker: Marker,
      radius: Double
  ) {
    val list = areasByOrg[orgId] ?: return
    val index = list.indexOfFirst { it.id == areaId }
    require(index != -1) { "Area with id $areaId not found" }

    list[index] = list[index].copy(label = label, marker = marker, radius = radius)
  }

  override suspend fun deleteArea(orgId: String, itemId: String) {
    val list = areasByOrg[orgId] ?: return
    list.removeIf { it.id == itemId }
  }

  // Optional helper for tests
  fun seedAreas(orgId: String, areas: List<Area>) {
    areasByOrg[orgId] = areas.toMutableList()
  }

  fun fakeMarker(label: String): Marker =
      Marker(
          location = com.android.sample.model.map.Location(latitude = 46.5191, longitude = 6.5668),
          label = label)
}
