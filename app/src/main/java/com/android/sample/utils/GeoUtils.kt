package com.android.sample.utils

import com.android.sample.model.map.Area

/** Utility functions for simple geographic calculations. */
object GeoUtils {

  /**
   * Determine if the given point (latitude, longitude) is inside the provided Area.
   *
   * Uses the ray-casting algorithm (even-odd rule). A horizontal ray is cast to the right from the
   * test point; the number of times it intersects the polygon edges of the area determines if the
   * point is inside (odd = inside, even = outside).
   *
   * Important notes:
   * - The area must have at least 3 markers to form a valid polygon; otherwise the function returns
   *   false.
   * - The polygon is closed by appending the first marker to the end of the list.
   * - Coordinates are handled as (latitude, longitude).
   *
   * @param latitude Latitude of the test point in decimal degrees.
   * @param longitude Longitude of the test point in decimal degrees.
   * @param area The Area to test against.
   * @return true if the point is inside the area's polygon, false otherwise.
   */
  fun isPointInPolygon(latitude: Double, longitude: Double, area: Area): Boolean {
    val polygon = area.getSortedMarkers()

    var inside = false

    // Close the polygon by adding the first point at the end
    // This allows to handle the edge connecting the last point to the first
    val points = polygon + polygon.first()

    // For each edge of the polygon
    for (i in 0 until points.size - 1) {
      // Coordinates of the current edge's endpoints
      val (lat1, lon1) = points[i].location.latitude to points[i].location.longitude
      val (lat2, lon2) = points[i + 1].location.latitude to points[i + 1].location.longitude

      // Check if a horizontal ray from the test point crosses this edge
      // ((lon1 > longitude) != (lon2 > longitude)) :
      //    true if the point's longitude is between the longitudes of the edge endpoints
      // (latitude < (lat2 - lat1) * (longitude - lon1) / (lon2 - lon1) + lat1) :
      //    compute the latitude of the edge at the longitude of the test point
      //    and check if the test point is below this latitude (intersection)
      val intersect =
          ((lon1 > longitude) != (lon2 > longitude)) &&
              (latitude < (lat2 - lat1) * (longitude - lon1) / (lon2 - lon1) + lat1)

      // If the ray crosses the edge, flip the "inside" flag
      // This is the core idea of the ray-casting algorithm:
      //    an odd number of intersections means the point is inside
      if (intersect) inside = !inside
    }

    return inside
  }
}
