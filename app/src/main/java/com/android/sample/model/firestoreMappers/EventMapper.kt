package com.android.sample.model.firestoreMappers

import androidx.compose.ui.graphics.toArgb
import com.android.sample.model.calendar.CloudStorageStatus
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.ui.theme.EventPalette
import com.android.sample.ui.theme.Palette
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.time.Instant
import java.util.Date

/** Maps Firestore documents to [Event] objects and vice versa. */
object EventMapper : FirestoreMapper<Event> {

  override fun fromDocument(document: DocumentSnapshot): Event? {
    val id = document.id
    val organizationId = document.getString("organizationId") ?: return null
    val title = document.getString("title") ?: return null
    val description = document.getString("description") ?: ""
    val startDate = document.getTimestamp("startDate")?.toDate()?.toInstant() ?: return null
    val endDate = document.getTimestamp("endDate")?.toDate()?.toInstant() ?: return null
    val personalNotes = document.getString("personalNotes")

    val participants =
        (document["participants"] as? List<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet()

    val storageStatusList =
        (document["storageStatus"] as? List<*>)
            ?.mapNotNull { runCatching { CloudStorageStatus.valueOf(it.toString()) }.getOrNull() }
            ?.toSet() ?: setOf(CloudStorageStatus.FIRESTORE)

    val recurrenceStatus =
        runCatching {
              RecurrenceStatus.valueOf(document.getString("recurrenceStatus") ?: "OneTime")
            }
            .getOrDefault(RecurrenceStatus.OneTime)

    val presence = parsePresence(document.get("presence"))

    val version = document.getLong("version") ?: 0L
    val colorLong = document.getLong("eventColor") ?: EventPalette.Blue.toArgb().toLong()
    val color = Palette.fromLong(colorLong)

    return Event(
        id = id,
        organizationId = organizationId,
        title = title,
        description = description,
        startDate = startDate,
        endDate = endDate,
        cloudStorageStatuses = storageStatusList,
        personalNotes = personalNotes,
        participants = participants,
        version = version,
        presence = presence,
        recurrenceStatus = recurrenceStatus,
        color = color)
  }

  override fun fromMap(data: Map<String, Any?>): Event? {
    val id = data["id"] as? String ?: return null
    val organizationId = data["organizationId"] as? String ?: return null
    val title = data["title"] as? String ?: return null
    val description = data["description"] as? String ?: ""

    val startDate =
        (data["startDate"] as? Timestamp)?.toDate()?.toInstant()
            ?: (data["startDate"] as? Date)?.toInstant()
            ?: (data["startDate"] as? Long)?.let { Instant.ofEpochMilli(it) }
            ?: return null

    val endDate =
        (data["endDate"] as? Timestamp)?.toDate()?.toInstant()
            ?: (data["endDate"] as? Date)?.toInstant()
            ?: (data["endDate"] as? Long)?.let { Instant.ofEpochMilli(it) }
            ?: return null

    val personalNotes = data["personalNotes"] as? String
    val participants =
        (data["participants"] as? List<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet()

    val storageStatusList =
        (data["storageStatus"] as? List<*>)
            ?.mapNotNull { runCatching { CloudStorageStatus.valueOf(it.toString()) }.getOrNull() }
            ?.toSet() ?: setOf(CloudStorageStatus.FIRESTORE)

    val recurrenceStatus =
        runCatching { RecurrenceStatus.valueOf(data["recurrenceStatus"] as? String ?: "OneTime") }
            .getOrDefault(RecurrenceStatus.OneTime)

    val presence = parsePresence(data["presence"])

    val version = (data["version"] as? Number)?.toLong() ?: 0L
    val colorLong = (data["eventColor"] as? Number)?.toLong() ?: EventPalette.Blue.toArgb().toLong()
    val color = Palette.fromLong(colorLong)

    return Event(
        id = id,
        organizationId = organizationId,
        title = title,
        description = description,
        startDate = startDate,
        endDate = endDate,
        cloudStorageStatuses = storageStatusList,
        personalNotes = personalNotes,
        participants = participants,
        version = version,
        presence = presence,
        recurrenceStatus = recurrenceStatus,
        color = color)
  }

  override fun toMap(model: Event): Map<String, Any?> {
    return mapOf(
        "id" to model.id,
        "organizationId" to model.organizationId,
        "title" to model.title,
        "description" to model.description,
        "startDate" to Timestamp(Date.from(model.startDate)),
        "endDate" to Timestamp(Date.from(model.endDate)),
        "storageStatus" to model.cloudStorageStatuses.map { it.name },
        "personalNotes" to model.personalNotes,
        "participants" to model.participants.toList(),
        "version" to model.version,
        "presence" to model.presence,
        "recurrenceStatus" to model.recurrenceStatus.name,
        "eventColor" to model.color.toArgb().toLong())
  }

  private fun parsePresence(rawPresence: Any?): Map<String, Boolean> {
    return (rawPresence as? Map<*, *>)
        ?.mapNotNull { (key, value) ->
          val userId = key as? String ?: return@mapNotNull null
          val isPresent = value as? Boolean ?: return@mapNotNull null
          userId to isPresent
        }
        ?.toMap() ?: emptyMap()
  }
}
