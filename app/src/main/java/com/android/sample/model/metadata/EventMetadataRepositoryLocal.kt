package com.android.sample.model.metadata

/**
 * Local in-memory implementation of EventMetadataRepository.
 *
 * Stores event types / locations / participants per-organization. Intended for testing and local
 * development.
 */
class EventMetadataRepositoryLocal : EventMetadataRepository {

  // Each orgId → metadata container
  private val data = mutableMapOf<String, Metadata>()

  private data class Metadata(
      val eventTypes: MutableSet<String> = mutableSetOf(),
      val locations: MutableSet<String> = mutableSetOf(),
      val participants: MutableSet<String> = mutableSetOf()
  )

  /** Ensures metadata exists for an organization */
  private fun ensureOrg(orgId: String): Metadata {
    return data.getOrPut(orgId) { Metadata() }
  }

  // ---------------------------
  // Event Types
  // ---------------------------
  override suspend fun getEventTypes(orgId: String): List<String> {
    return ensureOrg(orgId).eventTypes.toList()
  }

  override suspend fun addEventType(orgId: String, label: String) {
    val metadata = ensureOrg(orgId)
    require(label.isNotBlank()) { "Event type cannot be blank." }
    require(!metadata.eventTypes.contains(label)) { "Event type '$label' already exists." }
    metadata.eventTypes.add(label)
  }

  override suspend fun deleteEventType(orgId: String, label: String) {
    val metadata = ensureOrg(orgId)
    require(metadata.eventTypes.contains(label)) { "Event type '$label' does not exist." }
    metadata.eventTypes.remove(label)
  }

  // ---------------------------
  // Locations
  // ---------------------------
  override suspend fun getLocations(orgId: String): List<String> {
    return ensureOrg(orgId).locations.toList()
  }

  override suspend fun addLocation(orgId: String, label: String) {
    val metadata = ensureOrg(orgId)
    require(label.isNotBlank()) { "Location cannot be blank." }
    require(!metadata.locations.contains(label)) { "Location '$label' already exists." }
    metadata.locations.add(label)
  }

  override suspend fun deleteLocation(orgId: String, label: String) {
    val metadata = ensureOrg(orgId)
    require(metadata.locations.contains(label)) { "Location '$label' does not exist." }
    metadata.locations.remove(label)
  }

  // for the participants, it exists in other local repository
}
