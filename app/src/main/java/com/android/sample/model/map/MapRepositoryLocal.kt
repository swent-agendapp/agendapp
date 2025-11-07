package com.android.sample.model.map

/** Represents a repository that manages a local list of markers and areas. */
class MapRepositoryLocal : MapRepository {

  // In-memory storage for markers (keyed by Marker.id).
  private val markers = mutableMapOf<String, Marker>()

  // In-memory storage for areas (keyed by Area.id).
  private val areas = mutableMapOf<String, Area>()

  override fun addMarker(marker: Marker) {
    markers[marker.id] = marker
  }

  override fun removeMarker(id: String) {
    markers.remove(key = id)
  }

  override fun getMarkerById(id: String): Marker? = markers[id]
  override fun getAllMarkers(): List<Marker> = markers.values.toList()

  override fun getAllMarkersIds(): List<String> = markers.keys.toList()

  override fun createArea(label: String?, markerIds: List<String>) {
    val selectedMarkers = markerIds.mapNotNull { markers[it] }
    val area = Area(label = label, markers = selectedMarkers)
    areas[area.id] = area
    markerIds.forEach { id -> removeMarker(id) }
  }

  override fun getAllAreas(): List<Area> = areas.values.toList()

  override fun getAllAreasIds(): List<String> = areas.keys.toList()

  override fun getAreaById(id: String): Area? = areas[id]
}
