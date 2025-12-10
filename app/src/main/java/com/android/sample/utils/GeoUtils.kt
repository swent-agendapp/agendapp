package com.android.sample.utils

import com.android.sample.model.map.Area
import kotlin.math.*

// AI help
/** Utility functions for simple geographic calculations. */
object GeoUtils {
  private const val EARTH_RADIUS_METERS = 6371000.0

  /**
   * Calculates the distance in meters between two points using the Haversine formula.
   *
   * @param lat1 Latitude of the first point in degrees
   * @param lon1 Longitude of the first point in degrees
   * @param lat2 Latitude of the second point in degrees
   * @param lon2 Longitude of the second point in degrees
   * @return Distance in meters
   */
  fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a =
        sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return EARTH_RADIUS_METERS * c
  }

  /**
   * Checks if a point is within a circular area.
   *
   * @param latitude Latitude of the point to check
   * @param longitude Longitude of the point to check
   * @param area The circular area (radius should be in meters)
   * @return true if the point is inside the circle, false otherwise
   */
  fun isPointInCircle(latitude: Double, longitude: Double, area: Area): Boolean {
    val distance =
        haversineDistance(
            latitude, longitude, area.marker.location.latitude, area.marker.location.longitude)
    return distance <= area.radius
  }
}
