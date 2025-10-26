package com.android.sample.utils

import com.android.sample.model.map.Area
import com.android.sample.model.map.Marker
import kotlin.math.atan2

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

  /**
   * Sort a list of markers in counter-clockwise order around their centroid using the ear clipping
   * algorithm for triangulation.
   *
   * @param markers List of Marker objects to sort. Must contain at least 3 points.
   * @return List of Marker objects sorted in counter-clockwise order.
   * @throws IllegalArgumentException if less than 3 markers are provided.
   */
  fun sortMarkersCounterClockwise(markers: List<Marker>): List<Marker> {
    require(markers.size >= 3) { "At least 3 points are required." }

    // Computation of the centroid
    val centerX = markers.map { it.location.longitude }.average()
    val centerY = markers.map { it.location.latitude }.average()

    // Compute angles and sort points around the centroid by these angles
    val sortedByAngle =
        markers
            .sortedWith(
                compareBy {
                  atan2(it.location.latitude - centerY, it.location.longitude - centerX)
                })
            .toList()

    // Triangulation using ear clipping

    val remaining = sortedByAngle.toMutableList()
    val result = mutableListOf<Marker>()

    // While more than 3 points remain, find and clip ears
    while (remaining.size > 3) {
      var earFound = false

      // Iterate through the remaining points to find an ear
      for (i in remaining.indices) {
        val prev = remaining[(i - 1 + remaining.size) % remaining.size]
        val curr = remaining[i]
        val next = remaining[(i + 1) % remaining.size]

        // Check if the angle formed by prev-curr-next is convex
        if (cross(prev, curr, next) <= 0) continue

        // Check if any other point is inside the triangle formed by prev, curr, next
        if (remaining.any {
          it != prev && it != curr && it != next && pointInTriangle(prev, curr, next, it)
        })
            continue

        // If we reach here, we found an ear; add curr to result and remove it from remaining
        result.add(curr)
        remaining.removeAt(i)
        earFound = true
        break
      }

      // If no ear was found, the polygon may be malformed; break to avoid infinite loop
      if (!earFound) break
    }

    // Add the last three remaining points
    result.addAll(remaining)
    return result
  }

  /** Calculate the cross product of vectors AB and AC. */
  fun cross(a: Marker, b: Marker, c: Marker): Double {
    val (x1, y1) = a.location.longitude to a.location.latitude
    val (x2, y2) = b.location.longitude to b.location.latitude
    val (x3, y3) = c.location.longitude to c.location.latitude
    return (x2 - x1) * (y3 - y1) - (y2 - y1) * (x3 - x1)
  }

  /** Check if point P is inside triangle ABC using barycentric coordinates. */
  fun pointInTriangle(a: Marker, b: Marker, c: Marker, p: Marker): Boolean {
    val d1 = cross(p, a, b)
    val d2 = cross(p, b, c)
    val d3 = cross(p, c, a)
    val hasNeg = listOf(d1, d2, d3).any { it < 0 }
    val hasPos = listOf(d1, d2, d3).any { it > 0 }
    return !(hasNeg && hasPos)
  }
}
