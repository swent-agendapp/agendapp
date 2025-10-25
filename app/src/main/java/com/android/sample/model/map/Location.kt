package com.android.sample.model.map

/**
 * Represents a simple geographic location with latitude and longitude and an optional label.
 *
 * Note: Latitude is typically in the range [-90.0, 90.0] and longitude in [-180.0, 180.0].
 * Validation is not enforced here; callers should ensure coordinates are valid if required.
 *
 * @property latitude Latitude of the location in decimal degrees.
 * @property longitude Longitude of the location in decimal degrees.
 * @property label Optional human-readable label or description for the location.
 */
data class Location(
    /** Latitude in decimal degrees (usually -90.0..90.0). */
    val latitude: Double,

    /** Longitude in decimal degrees (usually -180.0..180.0). */
    val longitude: Double,

    /** Optional short label for display purposes. Null when no label is provided. */
    val label: String? = null,
)
