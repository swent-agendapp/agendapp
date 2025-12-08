package com.android.sample.model.firestoreMappers

import androidx.compose.ui.graphics.Color
import com.android.sample.model.calendar.CloudStorageStatus
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.category.EventCategory
import com.android.sample.ui.theme.EventPalette
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.time.Instant
import java.util.Date
import java.util.UUID

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

    val presence = parsePresence(document["presence"])
    val version = document.getLong("version") ?: 0L
    val category = parseCategory(document["eventCategory"])

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
        category = category)
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
    val category = parseCategory(data["eventCategory"])

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
        category = category)
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
        "eventCategory" to
            mapOf(
                "id" to model.category.id,
                "organizationId" to model.category.organizationId,
                "label" to model.category.label,
                "color" to model.category.color.value.toLong(), // Color -> Long
                "isDefault" to model.category.isDefault,
            ))
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

  private fun parseCategory(rawCategory: Any?): EventCategory {
    val map = rawCategory as? Map<*, *> ?: return EventCategory.defaultCategory()

    val id = map["id"] as? String ?: UUID.randomUUID().toString()
    val organizationId = map["organizationId"] as? String ?: return EventCategory.defaultCategory()
    val label = map["label"] as? String ?: "Uncategorized"
    val colorLong = (map["color"] as? Number)?.toLong() ?: EventPalette.NoCategory.value.toLong()
    val isDefault = map["isDefault"] as? Boolean ?: false

    return EventCategory(
        id = id,
        organizationId = organizationId,
        label = label,
        color = Color(colorLong.toULong()),
        isDefault = isDefault,
    )
  }
}
