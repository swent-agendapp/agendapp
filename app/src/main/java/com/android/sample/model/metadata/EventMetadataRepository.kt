package com.android.sample.model.metadata

interface EventMetadataRepository {

  // -----------------------------
  // Event Types
  // -----------------------------

  /** Returns all event type labels for a given organization */
  suspend fun getEventTypes(orgId: String): List<String>

  /** Add a new event type label */
  suspend fun addEventType(orgId: String, label: String)

  /** Delete an existing event type label */
  suspend fun deleteEventType(orgId: String, label: String)

  // -----------------------------
  // Locations
  // -----------------------------

  /** Returns all location labels for a given organization */
  suspend fun getLocations(orgId: String): List<String>

  /** Add a new location label */
  suspend fun addLocation(orgId: String, label: String)

  /** Delete an existing location label */
  suspend fun deleteLocation(orgId: String, label: String)
}
