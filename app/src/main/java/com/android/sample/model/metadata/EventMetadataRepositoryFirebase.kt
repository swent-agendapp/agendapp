package com.android.sample.model.metadata

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Firebase implementation of EventMetadataRepository.
 *
 * Stores event type labels and location labels under:
 *
 * organizations/{orgId}/metadata/eventTypes/{label}
 * organizations/{orgId}/metadata/locations/{label}
 */
class EventMetadataRepositoryFirebase(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : EventMetadataRepository {

  private fun eventTypeCollection(orgId: String) =
      db.collection("organizations")
          .document(orgId)
          .collection("metadata")
          .document("eventTypes")
          .collection("labels")

  private fun locationCollection(orgId: String) =
      db.collection("organizations")
          .document(orgId)
          .collection("metadata")
          .document("locations")
          .collection("labels")

  // -----------------------------
  // Event Types
  // -----------------------------

  override suspend fun getEventTypes(orgId: String): List<String> {
    val snapshot = eventTypeCollection(orgId).get().await()
    return snapshot.documents.map { it.id }.sorted()
  }

  override suspend fun addEventType(orgId: String, label: String) {
    eventTypeCollection(orgId).document(label).set(mapOf("exists" to true)).await()
  }

  override suspend fun deleteEventType(orgId: String, label: String) {
    eventTypeCollection(orgId).document(label).delete().await()
  }

  // -----------------------------
  // Locations
  // -----------------------------

  override suspend fun getLocations(orgId: String): List<String> {
    val snapshot = locationCollection(orgId).get().await()
    return snapshot.documents.map { it.id }.sorted()
  }

  override suspend fun addLocation(orgId: String, label: String) {
    locationCollection(orgId).document(label).set(mapOf("exists" to true)).await()
  }

  override suspend fun deleteLocation(orgId: String, label: String) {
    locationCollection(orgId).document(label).delete().await()
  }
}
