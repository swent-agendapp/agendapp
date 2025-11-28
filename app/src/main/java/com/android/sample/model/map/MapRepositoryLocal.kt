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

  override fun addMarker(orgId: String, marker: Marker) {
    getOrCreate(orgId).markers[marker.id] = marker
  }

  override fun removeMarker(orgId: String, id: String) {
    getOrCreate(orgId).markers.remove(id)
  }

  override fun getMarkerById(orgId: String, id: String): Marker? = getOrCreate(orgId).markers[id]

  override fun getAllMarkers(orgId: String): List<Marker> =
      getOrCreate(orgId).markers.values.toList()

  override fun getAllMarkersIds(orgId: String): List<String> =
      getOrCreate(orgId).markers.keys.toList()

  override suspend fun createArea(orgId: String, label: String?, markerIds: List<String>) {
    val org = getOrCreate(orgId)
    val selectedMarkers = markerIds.mapNotNull { org.markers[it] }
    val area = Area(label = label, markers = selectedMarkers)
    org.areas[area.id] = area
  }

  override suspend fun getAllAreas(orgId: String): List<Area> =
      getOrCreate(orgId).areas.values.toList()

  override fun getAllAreasIds(orgId: String): List<String> = getOrCreate(orgId).areas.keys.toList()

  override fun getAreaById(orgId: String, id: String): Area? = getOrCreate(orgId).areas[id]
}
