package com.android.sample.model.map

import java.util.UUID

/**
 * Represents a marker on a map.
 *
 * Each Marker is immutable (data class) and contains a unique identifier, position coordinates
 * (latitude and longitude) and an optional label for display.
 *
 * The id defaults to a randomly generated UUID string when not provided, ensuring each marker can
 * be referenced uniquely (for example when storing, updating, or removing markers).
 */
data class Marker(val id: String = UUID.randomUUID().toString(), val location: Location, val label: String? = null) {
  /**
   * Secondary constructor allowing to create a Marker directly from latitude and longitude without
   * having to manually construct a Location object.
   */
  constructor(
      id: String = UUID.randomUUID().toString(),
      latitude: Double,
      longitude: Double,
      label: String? = null,
  ) : this(id = id, location = Location(latitude, longitude), label = label)
}
