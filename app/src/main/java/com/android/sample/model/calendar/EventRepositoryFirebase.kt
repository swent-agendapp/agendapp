package com.android.sample.model.calendar

import com.android.sample.model.constants.FirestoreConstants.EVENTS_COLLECTION_PATH
import com.android.sample.model.firestoreMappers.EventMapper
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import java.util.Date
import kotlinx.coroutines.tasks.await

class EventRepositoryFirebase(private val db: FirebaseFirestore) : EventRepository {
  override suspend fun getAllEvents(): List<Event> {
    val snapshot = db.collection(EVENTS_COLLECTION_PATH).get().await()
    return snapshot.mapNotNull { EventMapper.fromDocument(document = it) }
  }

  override suspend fun insertEvent(item: Event) {
    db.collection(EVENTS_COLLECTION_PATH)
        .document(item.id)
        .set(EventMapper.toMap(model = item))
        .await()
  }

  override suspend fun updateEvent(itemId: String, item: Event) {
    db.collection(EVENTS_COLLECTION_PATH)
        .document(itemId)
        .set(EventMapper.toMap(model = item))
        .await()
  }

  override suspend fun deleteEvent(itemId: String) {
    db.collection(EVENTS_COLLECTION_PATH).document(itemId).delete().await()
  }

  override suspend fun getEventById(itemId: String): Event? {
    val document = db.collection(EVENTS_COLLECTION_PATH).document(itemId).get().await()
    return EventMapper.fromDocument(document = document)
  }

  override suspend fun getEventsBetweenDates(startDate: Instant, endDate: Instant): List<Event> {
    require(startDate <= endDate) { "start date must be before or equal to end date" }

    val snapshot =
        db.collection(EVENTS_COLLECTION_PATH)
            // get all events that end on or after the start of the range
            .whereGreaterThanOrEqualTo("endDate", Timestamp(Date.from(startDate)))
            .get()
            .await()

    return snapshot
        .mapNotNull { EventMapper.fromDocument(document = it) }
        // keep only events whose start is on/before the queried end
        .filter { it.startDate <= endDate }
  }
}
