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

  const val ID_FIELD = "id"
  const val ORGANIZATION_ID_FIELD = "organizationId"
  const val TITLE_FIELD = "title"
  const val DESCRIPTION_FIELD = "description"
  const val START_DATE_FIELD = "startDate"
  const val END_DATE_FIELD = "endDate"
  const val PERSONAL_NOTES_FIELD = "personalNotes"
  const val LOCATION_FIELD = "location"
  const val PARTICIPANTS_FIELD = "participants"
  const val ASSIGNED_USERS_FIELD = "assignedUsers"
  const val STORAGE_STATUS_FIELD = "storageStatus"
  const val RECURRENCE_STATUS_FIELD = "recurrenceStatus"
  const val PRESENCE_FIELD = "presence"
  const val VERSION_FIELD = "version"
  const val HAS_BEEN_DELETED_FIELD = "hasBeenDeleted"
  const val EVENT_CATEGORY_FIELD = "eventCategory"

  override fun fromDocument(document: DocumentSnapshot): Event? {
    val id = document.id
    val organizationId = document.getString(ORGANIZATION_ID_FIELD) ?: return null
    val title = document.getString(TITLE_FIELD) ?: return null
    val description = document.getString(DESCRIPTION_FIELD) ?: ""
    val startDate = document.getTimestamp(START_DATE_FIELD)?.toDate()?.toInstant() ?: return null
    val endDate = document.getTimestamp(END_DATE_FIELD)?.toDate()?.toInstant() ?: return null
    val personalNotes = document.getString(PERSONAL_NOTES_FIELD)
    val location = document.getString(LOCATION_FIELD)

    val participants =
        (document[PARTICIPANTS_FIELD] as? List<*>)?.filterIsInstance<String>()?.toSet()
            ?: emptySet()

    val assignedUsers =
        (document[ASSIGNED_USERS_FIELD] as? List<*>)?.filterIsInstance<String>()?.toSet()
            ?: participants

    val storageStatusList =
        (document[STORAGE_STATUS_FIELD] as? List<*>)
            ?.mapNotNull { runCatching { CloudStorageStatus.valueOf(it.toString()) }.getOrNull() }
            ?.toSet() ?: setOf(CloudStorageStatus.FIRESTORE)

    val recurrenceStatus =
        runCatching {
              RecurrenceStatus.valueOf(document.getString(RECURRENCE_STATUS_FIELD) ?: "OneTime")
            }
            .getOrDefault(RecurrenceStatus.OneTime)

    val presence = parsePresence(document[PRESENCE_FIELD])
    val version = document.getLong(VERSION_FIELD) ?: 0L
    val hasBeenDeleted = document.getBoolean(HAS_BEEN_DELETED_FIELD) ?: false
    val category = parseCategory(document[EVENT_CATEGORY_FIELD])

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
        assignedUsers = assignedUsers,
        version = version,
        presence = presence,
        recurrenceStatus = recurrenceStatus,
        hasBeenDeleted = hasBeenDeleted,
        category = category,
        location = location)
  }

  override fun fromMap(data: Map<String, Any?>): Event? {
    val id = data[ID_FIELD] as? String ?: return null
    val organizationId = data[ORGANIZATION_ID_FIELD] as? String ?: return null
    val title = data[TITLE_FIELD] as? String ?: return null
    val description = data[DESCRIPTION_FIELD] as? String ?: ""

    val startDate =
        (data[START_DATE_FIELD] as? Timestamp)?.toDate()?.toInstant()
            ?: (data[START_DATE_FIELD] as? Date)?.toInstant()
            ?: (data[START_DATE_FIELD] as? Long)?.let { Instant.ofEpochMilli(it) }
            ?: return null

    val endDate =
        (data[END_DATE_FIELD] as? Timestamp)?.toDate()?.toInstant()
            ?: (data[END_DATE_FIELD] as? Date)?.toInstant()
            ?: (data[END_DATE_FIELD] as? Long)?.let { Instant.ofEpochMilli(it) }
            ?: return null

    val personalNotes = data[PERSONAL_NOTES_FIELD] as? String
    val location = data[LOCATION_FIELD] as? String
    val participants =
        (data[PARTICIPANTS_FIELD] as? List<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet()

    val assignedUsers =
        (data[ASSIGNED_USERS_FIELD] as? List<*>)?.filterIsInstance<String>()?.toSet()
            ?: participants

    val storageStatusList =
        (data[STORAGE_STATUS_FIELD] as? List<*>)
            ?.mapNotNull { runCatching { CloudStorageStatus.valueOf(it.toString()) }.getOrNull() }
            ?.toSet() ?: setOf(CloudStorageStatus.FIRESTORE)

    val recurrenceStatus =
        runCatching {
              RecurrenceStatus.valueOf(data[RECURRENCE_STATUS_FIELD] as? String ?: "OneTime")
            }
            .getOrDefault(RecurrenceStatus.OneTime)

    val presence = parsePresence(data[PRESENCE_FIELD])

    val version = (data[VERSION_FIELD] as? Number)?.toLong() ?: 0L
    val hasBeenDeleted = data[HAS_BEEN_DELETED_FIELD] as? Boolean ?: false
    val category = parseCategory(data[EVENT_CATEGORY_FIELD])

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
        assignedUsers = assignedUsers,
        version = version,
        presence = presence,
        recurrenceStatus = recurrenceStatus,
        location = location,
        hasBeenDeleted = hasBeenDeleted,
        category = category)
  }

  override fun toMap(model: Event): Map<String, Any?> {
    return mapOf(
        ID_FIELD to model.id,
        ORGANIZATION_ID_FIELD to model.organizationId,
        TITLE_FIELD to model.title,
        DESCRIPTION_FIELD to model.description,
        START_DATE_FIELD to Timestamp(Date.from(model.startDate)),
        END_DATE_FIELD to Timestamp(Date.from(model.endDate)),
        STORAGE_STATUS_FIELD to model.cloudStorageStatuses.map { it.name },
        PERSONAL_NOTES_FIELD to model.personalNotes,
        LOCATION_FIELD to model.location,
        PARTICIPANTS_FIELD to model.participants.toList(),
        ASSIGNED_USERS_FIELD to model.assignedUsers.toList(),
        VERSION_FIELD to model.version,
        PRESENCE_FIELD to model.presence,
        RECURRENCE_STATUS_FIELD to model.recurrenceStatus.name,
        HAS_BEEN_DELETED_FIELD to model.hasBeenDeleted,
        EVENT_CATEGORY_FIELD to
            mapOf(
                EventCategoryMapper.ID_FIELD to model.category.id,
                EventCategoryMapper.ORGANIZATION_ID_FIELD to model.category.organizationId,
                EventCategoryMapper.LABEL_FIELD to model.category.label,
                EventCategoryMapper.COLOR_FIELD to
                    model.category.color.value.toLong(), // Color -> Long
                EventCategoryMapper.IS_DEFAULT_FIELD to model.category.isDefault,
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

    val id = map[EventCategoryMapper.ID_FIELD] as? String ?: UUID.randomUUID().toString()
    val organizationId =
        map[EventCategoryMapper.ORGANIZATION_ID_FIELD] as? String
            ?: return EventCategory.defaultCategory()
    val label = map[EventCategoryMapper.LABEL_FIELD] as? String ?: "Uncategorized"
    val colorLong =
        (map[EventCategoryMapper.COLOR_FIELD] as? Number)?.toLong()
            ?: EventPalette.NoCategory.value.toLong()
    val isDefault = map[EventCategoryMapper.IS_DEFAULT_FIELD] as? Boolean ?: false

    return EventCategory(
        id = id,
        organizationId = organizationId,
        label = label,
        color = Color(colorLong.toULong()),
        isDefault = isDefault,
    )
  }
}
