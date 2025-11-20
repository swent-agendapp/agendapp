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
   * Add or replace a marker in the repository.
   *
   * If a marker with the same id already exists it will be overwritten.
   *
   * @param marker The Marker to store.
   */
  fun addMarker(marker: Marker)

  /**
   * Remove a marker by its id.
   *
   * If the id does not exist, this is a no-op.
   *
   * @param id The identifier of the marker to remove.
   */
  fun removeMarker(id: String)

  /**
   * Retrieve a marker by id.
   *
   * @param id The marker id to look up.
   * @return The Marker if found, or null otherwise.
   */
  fun getMarkerById(id: String): Marker?

  /**
   * Return all markers currently stored.
   *
   * A snapshot list is returned to avoid exposing the internal mutable collection.
   */
  fun getAllMarkers(): List<Marker>

  /**
   * Return the ids of all stored markers.
   *
   * Useful for clients that want to create areas by referencing marker ids.
   */
  fun getAllMarkersIds(): List<String>

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
  suspend fun createArea(label: String? = null, markerIds: List<String> = getAllMarkersIds())

  /**
   * Return all areas currently stored.
   *
   * A snapshot list is returned to avoid exposing internal mutable collection.
   */
  suspend fun getAllAreas(): List<Area>

  /** Return the ids of all stored areas. */
  fun getAllAreasIds(): List<String>

  /**
   * Retrieve an area by id.
   *
   * @param id The area id to look up.
   * @return The Area if found, or null otherwise.
   */
  fun getAreaById(id: String): Area?
}
