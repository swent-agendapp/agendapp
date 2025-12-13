package com.android.sample.model.filter

import com.android.sample.model.map.Area
import com.android.sample.model.map.MapRepository
import com.android.sample.model.map.Marker

class FakeMapRepository : MapRepository {

  override suspend fun getAllAreas(orgId: String): List<Area> {
    return listOf(
        Area(id = "area1", label = "Salle 1", marker = fakeMarker("Salle 1"), radius = 10.0),
        Area(id = "area2", label = "Salle 2", marker = fakeMarker("Salle 2"), radius = 10.0))
  }

  override suspend fun createArea(orgId: String, label: String, marker: Marker, radius: Double) {
    // no-op for fake
  }

  override suspend fun updateArea(
      areaId: String,
      orgId: String,
      label: String,
      marker: Marker,
      radius: Double
  ) {
    // no-op for fake
  }

  override suspend fun deleteArea(orgId: String, itemId: String) {
    // no-op for fake
  }

  private fun fakeMarker(label: String): Marker =
      Marker(
          location = com.android.sample.model.map.Location(latitude = 46.5191, longitude = 6.5668),
          label = label)
}
