package com.android.sample.model.calendar

import com.android.sample.utils.EventColor
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import java.util.Date
import kotlinx.coroutines.tasks.await

const val EVENTS_COLLECTION_PATH = "events"

class EventRepositoryFirebase(private val db: FirebaseFirestore) : EventRepository {

  override suspend fun getAllEvents(): List<Event> {
    val snapshot = db.collection(EVENTS_COLLECTION_PATH).get().await()
    return snapshot.mapNotNull { documentToEvent(it) }
  }

  override suspend fun insertEvent(item: Event) {
    db.collection(EVENTS_COLLECTION_PATH).document(item.id).set(eventToMap(item)).await()
  }

  override suspend fun updateEvent(itemId: String, item: Event) {
    db.collection(EVENTS_COLLECTION_PATH).document(itemId).set(eventToMap(item)).await()
  }

  override suspend fun deleteEvent(itemId: String) {
    db.collection(EVENTS_COLLECTION_PATH).document(itemId).delete().await()
  }

  override suspend fun getEventById(itemId: String): Event? {
    val document = db.collection(EVENTS_COLLECTION_PATH).document(itemId).get().await()
    return documentToEvent(document)
  }

  override suspend fun getEventsBetweenDates(startDate: Instant, endDate: Instant): List<Event> {
    require(startDate <= endDate) { "start date must be before or equal to end date" }

    val snapshot =
        db.collection(EVENTS_COLLECTION_PATH)
            .whereGreaterThanOrEqualTo("startDate", Timestamp(Date.from(startDate)))
            .whereLessThanOrEqualTo("endDate", Timestamp(Date.from(endDate)))
            .get()
            .await()

    return snapshot.mapNotNull { documentToEvent(document = it) }
  }

  /**
   * Converts a Firestore document to an Event object.
   *
   * @param document The Firestore document.
   * @return The Event object, or null if conversion fails.
   */
  private fun documentToEvent(document: DocumentSnapshot): Event? {

    val id = document.id
    val title = document.getString("title") ?: return null
    val description = document.getString("description") ?: ""
    val startDate = document.getTimestamp("startDate")?.toDate()?.toInstant() ?: return null
    val endDate = document.getTimestamp("endDate")?.toDate()?.toInstant() ?: return null
    val personalNotes = document.getString("personalNotes")

    val owners = (document["owners"] as? List<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet()
    val participants =
        (document["participants"] as? List<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet()

    val storageStatusList =
        (document["storageStatus"] as? List<*>)
            ?.mapNotNull { runCatching { CloudStorageStatus.valueOf(it.toString()) }.getOrNull() }
            ?.toSet() ?: setOf(CloudStorageStatus.FIRESTORE)

    val recurrenceStatus =
        runCatching {
              RecurrenceStatus.valueOf(value = document.getString("recurrenceStatus") ?: "OneTime")
            }
            .getOrDefault(defaultValue = RecurrenceStatus.OneTime)

    val version = document.getLong("version") ?: 0L

    val colorLong = document.getLong("eventColor") ?: EventColor.Blue.value

    return Event(
        id = id,
        title = title,
        description = description,
        startDate = startDate,
        endDate = endDate,
        cloudStorageStatuses = storageStatusList,
        personalNotes = personalNotes,
        participants = participants,
        version = version,
        recurrenceStatus = recurrenceStatus,
        color = EventColor(colorLong))
  }

  /**
   * Converts an Event object to a Map for storing in Firestore.
   *
   * @param event The Event object.
   * @return Map representation of the event.
   */
  private fun eventToMap(event: Event): Map<String, Any?> {
    return mapOf(
        "title" to event.title,
        "description" to event.description,
        "startDate" to Timestamp(Date.from(event.startDate)),
        "endDate" to Timestamp(Date.from(event.endDate)),
        "storageStatus" to event.cloudStorageStatuses.map { it.name },
        "personalNotes" to event.personalNotes,
        "participants" to event.participants.toList(),
        "version" to event.version,
        "recurrenceStatus" to event.recurrenceStatus.name,
        "eventColor" to event.color.value)
  }
}
