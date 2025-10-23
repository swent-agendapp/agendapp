package com.android.sample.model.map

import com.android.sample.utils.GeoUtils
import java.util.UUID

/**
 * Represents a polygonal area defined by a list of map markers.
 *
 * An Area groups multiple Marker instances to form a polygonal region on a map.
 *
 * @property id Unique identifier for the area. Defaults to a randomly generated UUID string.
 * @property label Optional human-readable label or name for the area.
 * @property markers List of Marker objects that define the vertices of the area polygon. The
 *   polygon is expected to be formed in the order provided by this list.
 *
 * Note: The class enforces that an area must have at least three different markers to form a valid
 * polygon.
 */
data class Area(
    val id: String = UUID.randomUUID().toString(),
    val label: String? = null,
    val markers: List<Marker>
) {
  init {
    // Check there are at least 3 distinct markers by comparing their locations
    val distinctMarkers = markers.map { it.location }.distinctBy { it.latitude to it.longitude }

    require(value = distinctMarkers.size >= 3) {
      "An Area must have at least 3 distinct markers with unique coordinates"
    }
  }

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
