package com.android.sample.model.calendar

import com.android.sample.data.firebase.mappers.EventMapper
import com.android.sample.model.constants.FirestoreConstants.EVENTS_COLLECTION_PATH
import com.android.sample.model.constants.FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import java.util.Date
import kotlinx.coroutines.tasks.await

open class EventRepositoryFirebase(private val db: FirebaseFirestore) : BaseEventRepository() {

  override fun getNewUid(): String {
    return db.collection(EVENTS_COLLECTION_PATH).document().id
  }

  override suspend fun getAllEvents(orgId: String): List<Event> {
    val snapshot =
        db.collection(ORGANIZATIONS_COLLECTION_PATH)
            .document(orgId)
            .collection(EVENTS_COLLECTION_PATH)
            .whereEqualTo("organizationId", orgId)
            .get()
            .await()
    return snapshot.mapNotNull { EventMapper.fromDocument(document = it) }
  }

  override suspend fun insertEvent(orgId: String, item: Event) {
    // Calls the interface check to ensure the organizationId matches
    super.insertEvent(orgId, item)

    val data = EventMapper.toMap(item.copy(version = System.currentTimeMillis())).toMutableMap()

    db.collection(ORGANIZATIONS_COLLECTION_PATH)
        .document(orgId)
        .collection(EVENTS_COLLECTION_PATH)
        .document(item.id)
        .set(data)
        .await()
  }

  override suspend fun updateEvent(orgId: String, itemId: String, item: Event) {
    // Calls the interface check to ensure the organizationId matches
    super.updateEvent(orgId, itemId, item)

    val data = EventMapper.toMap(item.copy(version = System.currentTimeMillis())).toMutableMap()

    db.collection(ORGANIZATIONS_COLLECTION_PATH)
        .document(orgId)
        .collection(EVENTS_COLLECTION_PATH)
        .document(itemId)
        .set(data)
        .await()
  }

  override suspend fun deleteEvent(orgId: String, itemId: String) {
    val retrievedItem =
        db.collection(ORGANIZATIONS_COLLECTION_PATH)
            .document(orgId)
            .collection(EVENTS_COLLECTION_PATH)
            .document(itemId)
            .get()
            .await()

    require(retrievedItem.getString("organizationId") == orgId) {
      "Event's organizationId ${retrievedItem.getString("organizationId")} does not match the provided orgId $orgId."
    }

    // Soft delete: set hasBeenDeleted to true and update version
    db.collection(ORGANIZATIONS_COLLECTION_PATH)
        .document(orgId)
        .collection(EVENTS_COLLECTION_PATH)
        .document(itemId)
        .update(mapOf("version" to System.currentTimeMillis(), "hasBeenDeleted" to true))
        .await()
  }

  override suspend fun getEventById(orgId: String, itemId: String): Event? {
    val document =
        db.collection(ORGANIZATIONS_COLLECTION_PATH)
            .document(orgId)
            .collection(EVENTS_COLLECTION_PATH)
            .document(itemId)
            .get()
            .await()

    // Return null if the document does not exist or has been deleted
    if (!document.exists() || document.getBoolean("hasBeenDeleted") == true) {
      return null
    }

    require(document.getString("organizationId") == orgId) {
      "Event's organizationId ${document.getString("organizationId")} does not match the provided orgId $orgId."
    }

    return EventMapper.fromDocument(document)
  }

  override suspend fun getEventsBetweenDates(
      orgId: String,
      startDate: Instant,
      endDate: Instant
  ): List<Event> {
    require(startDate <= endDate) { "start date must be before or equal to end date" }

    val snapshot =
        db.collection(ORGANIZATIONS_COLLECTION_PATH)
            .document(orgId)
            .collection(EVENTS_COLLECTION_PATH)
            // get all events that end on or after the start of the range
            .whereGreaterThanOrEqualTo("endDate", Timestamp(Date.from(startDate)))
            .get()
            .await()

    val documentList = snapshot.documents.filter { it.getString("organizationId") == orgId }

    require(documentList.all { it.getString("organizationId") == orgId }) {
      "Some events' organizationId does not match the provided orgId $orgId."
    }

    return snapshot
        .mapNotNull { EventMapper.fromDocument(document = it) }
        // keep only events whose start is on/before the queried end
        .filter { it.startDate <= endDate }
  }

  override suspend fun ensureOrganizationExists(orgId: String) {
    val orgExists =
        db.collection(ORGANIZATIONS_COLLECTION_PATH).document(orgId).get().await().exists()

    require(orgExists) { "Organization with id $orgId not found" }
  }
}
