package com.android.sample.model.metadata

/**
 * Simple in-memory fake implementation of EventMetadataRepository, used for unit tests and UI
 * tests.
 */
class FakeEventMetadataRepository : EventMetadataRepository {

  /** Internal storage for metadata, per organization */
  private val eventTypesMap = mutableMapOf<String, MutableSet<String>>()
  private val locationsMap = mutableMapOf<String, MutableSet<String>>()

  // -----------------------------
  // Event Types
  // -----------------------------
  override suspend fun getEventTypes(orgId: String): List<String> {
    return eventTypesMap[orgId]?.toList()?.sorted() ?: emptyList()
  }

  override suspend fun addEventType(orgId: String, label: String) {
    val set = eventTypesMap.getOrPut(orgId) { mutableSetOf() }
    set.add(label)
  }

  override suspend fun deleteEventType(orgId: String, label: String) {
    eventTypesMap[orgId]?.remove(label)
  }

  // -----------------------------
  // Locations
  // -----------------------------
  override suspend fun getLocations(orgId: String): List<String> {
    return locationsMap[orgId]?.toList()?.sorted() ?: emptyList()
  }

  override suspend fun addLocation(orgId: String, label: String) {
    val set = locationsMap.getOrPut(orgId) { mutableSetOf() }
    set.add(label)
  }

  override suspend fun deleteLocation(orgId: String, label: String) {
    locationsMap[orgId]?.remove(label)
  }
}
