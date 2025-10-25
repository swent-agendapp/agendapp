package com.android.sample.model.map

import com.android.sample.utils.GeoUtils
import java.util.UUID
import kotlin.math.atan2

/**
 * Represents a polygonal area defined by a list of map markers.
 *
 * An Area groups multiple Marker instances to form a polygonal region on a map.
 *
 * @property id Unique identifier for the area. Defaults to a randomly generated UUID string.
 * @property label Optional human-readable label or name for the area.
 * @property markers List of Marker objects that define the vertices of the area polygon. The
 *   constructor guarantees that the markers are sorted in a consistent order around the polygon, so
 *   the user does not need to provide them in a specific order.
 *
 * Note: The class enforces that an area must have at least three different markers to form a valid
 * polygon.
 */
data class Area(
    val id: String = UUID.randomUUID().toString(),
    val label: String? = null,
    val markers: List<Marker>
) {
  private val _sortedMarkers: List<Marker>

  init {
    // Check there are at least 3 distinct markers by comparing their locations
    val distinctMarkers =
        markers
            .map { it.location }
            .distinctBy { it.latitude to it.longitude }
            .map { loc -> markers.first { it.location == loc } }

    require(distinctMarkers.size >= 3) {
      "An Area must have at least 3 distinct markers with unique coordinates"
    }

    // Compute centroid
    val centroidLat = distinctMarkers.map { it.location.latitude }.average()
    val centroidLon = distinctMarkers.map { it.location.longitude }.average()

    // Sort markers around centroid in counter-clockwise order
    this._sortedMarkers =
        distinctMarkers.sortedBy { marker ->
          atan2(marker.location.latitude - centroidLat, marker.location.longitude - centroidLon)
        }
  }

  /**
   * Returns the list of markers defining this Area, sorted in counter-clockwise order around the
   * polygon.
   *
   * @return List of Marker objects in sorted order.
   */
  fun getSortedMarkers(): List<Marker> = _sortedMarkers

  /**
   * Checks whether the given marker is located inside this Area.
   *
   * @param marker The Marker to check.
   * @return true if the marker is inside the area polygon, false otherwise.
   */
  fun contains(marker: Marker): Boolean =
      GeoUtils.isPointInPolygon(
          latitude = marker.location.latitude, longitude = marker.location.longitude, area = this)
}
