package com.android.sample.data.local.mappers

import com.android.sample.data.local.objects.EventEntity
import com.android.sample.data.local.utils.decodeBooleanMap
import com.android.sample.data.local.utils.decodeList
import com.android.sample.data.local.utils.decodeSet
import com.android.sample.data.local.utils.encodeBooleanMap
import com.android.sample.data.local.utils.encodeList
import com.android.sample.data.local.utils.encodeSet
import com.android.sample.model.calendar.CloudStorageStatus
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.RecurrenceStatus

/** Maps between [Event] domain model and [EventEntity] for local database storage. */
object EventMapper {

  /* Maps an Event domain model to an EventEntity for local database storage. */
  fun Event.toEntity(): EventEntity {
    return EventEntity(
        id = id,
        organizationId = organizationId,
        title = title,
        description = description,
        startDate = startDate,
        endDate = endDate,
        cloudStorageStatuses = encodeSet(cloudStorageStatuses.map { it.name }.toSet()),
        locallyStoredBy = encodeList(locallyStoredBy),
        personalNotes = personalNotes,
        participants = encodeSet(participants),
        presence = encodeBooleanMap(presence),
        version = version,
        hasBeenDeleted = hasBeenDeleted,
        recurrenceStatus = recurrenceStatus.name,
        category = category)
  }

  /* Maps an EventEntity from local database storage to an Event domain model. */
  fun EventEntity.toEvent(): Event {
    return Event(
        id = id,
        organizationId = organizationId,
        title = title,
        description = description,
        startDate = startDate,
        endDate = endDate,
        cloudStorageStatuses =
            decodeSet(cloudStorageStatuses)
                .mapNotNull { runCatching { CloudStorageStatus.valueOf(it) }.getOrNull() }
                .toSet(),
        locallyStoredBy = decodeList(locallyStoredBy),
        personalNotes = personalNotes,
        participants = decodeSet(participants),
        presence = decodeBooleanMap(presence),
        version = version,
        hasBeenDeleted = hasBeenDeleted,
        recurrenceStatus =
            runCatching { RecurrenceStatus.valueOf(recurrenceStatus) }
                .getOrDefault(RecurrenceStatus.OneTime),
        category = category)
  }
}
