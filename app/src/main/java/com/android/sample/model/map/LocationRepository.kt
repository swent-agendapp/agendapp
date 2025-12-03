package com.android.sample.model.map

/**
 * Repository interface for location-related operations.
 *
 * Provides operations to fetch user location and perform location-based checks.
 */
interface LocationRepository {

  /**
   * Fetches the user's current location.
   *
   * This method will attempt to get the user's location using the device's location services. It
   * first tries to get the last known location for efficiency. If no cached location is available,
   * it requests a fresh location from the GPS.
   *
   * @param askNewLocation if we force the provider to fetch a new location
   * @return The user's current Location.
   * @throws SecurityException if location permissions are not granted.
   * @throws Exception if location cannot be fetched.
   */
  suspend fun getUserLocation(askNewLocation: Boolean = false): Location

  /**
   * Fetches the user's current location and checks if it is inside any of the provided areas.
   *
   * This method will attempt to get the user's location using the device's location services. It
   * first tries to get the last known location for efficiency. If no cached location is available,
   * it requests a fresh location from the GPS.
   *
   * @param areas List of Area objects to check against the user's location.
   * @return true if the user's location is inside any of the provided areas, false otherwise.
   * @throws SecurityException if location permissions are not granted.
   * @throws Exception if location cannot be fetched or other errors occur.
   */
  suspend fun isUserLocationInAreas(areas: List<Area>, askNewLocation: Boolean = false): Boolean
}
