package com.android.sample.utils

import com.android.sample.model.map.Area

/** Utility functions for simple geographic calculations. */
object GeoUtils {

  fun isPointInCircle(latitude: Double, longitude: Double, area: Area): Boolean {
    val latDistance = latitude - area.marker.location.latitude
    val lonDistance = longitude - area.marker.location.longitude
    val distanceSquared = (latDistance * latDistance) + (lonDistance * lonDistance)

    return distanceSquared < area.radius * area.radius
  }
}
