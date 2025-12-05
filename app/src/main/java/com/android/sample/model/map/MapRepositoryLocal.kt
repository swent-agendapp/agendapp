package com.android.sample.model.map

/** Represents a repository that manages a local list of markers and areas. */
class MapRepositoryLocal : MapRepository {

  // In-memory storage for markers and areas by organization ID
  private val dataByOrganization = mutableMapOf<String, OrgData>()

  // Helper properties to access markers and areas for the current organization
  private data class OrgData(
      val markers: MutableMap<String, Marker> = mutableMapOf(),
      val areas: MutableMap<String, Area> = mutableMapOf()
  )

  // Helper function to get or create OrgData for a given organization ID
  private fun getOrCreate(orgId: String): OrgData = dataByOrganization.getOrPut(orgId) { OrgData() }

  override suspend fun createArea(orgId: String, label: String, marker: Marker, radius: Double) {
    val org = getOrCreate(orgId)
    val area = Area(label = label, marker = marker, radius = radius)
    org.areas[area.id] = area
  }

  override suspend fun updateArea(
    areaId: String,
    orgId: String,
    label: String,
    marker: Marker,
    radius: Double
  ) {
    val org = getOrCreate(orgId)
    val area = Area(id = areaId, label = label, marker = marker, radius = radius)
    org.areas[area.id] = area
  }

  override suspend fun getAllAreas(orgId: String): List<Area> =
      getOrCreate(orgId).areas.values.toList()

  override suspend fun deleteArea(orgId: String, itemId: String) {
    getOrCreate(orgId).areas.remove(itemId)
  }
}
