package com.android.sample.model.map

/**
 * Repository interface for map-related domain objects.
 *
 * Provides operations to store, retrieve, and manage Marker and Area instances. Implementations are
 * responsible for the actual storage and may be in-memory, persistent, or backed by a remote
 * source.
 */
interface MapRepository {
  /**
   * Create a new Area from a list of marker ids and add it in the repository.
   *
   * The parameter markerIds defaults to the current set of all marker ids at the time of the call.
   * Only marker ids that exist in the repository are used (missing ids are ignored).
   *
   * The Area constructor enforces a minimum of three distinct markers (a valid polygon).
   *
   * @param label Optional human-readable label for the area.
   * @param markerIds List of marker ids to include in the area. Defaults to all markers currently
   *   stored.
   */
  suspend fun createArea(orgId: String, label: String, marker: Marker, radius: Double)
  suspend fun updateArea(areaId: String, orgId: String, label: String, marker: Marker, radius: Double)

  /**
   * Return all areas currently stored.
   *
   * A snapshot list is returned to avoid exposing internal mutable collection.
   */
  suspend fun getAllAreas(orgId: String): List<Area>

  /**
   * delete a currently stored Area.
   *
   * @param itemId the item to remove from database
   */
  suspend fun deleteArea(orgId: String, itemId: String)
}
