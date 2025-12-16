package com.android.sample.data.hybrid.repositories

import com.android.sample.data.firebase.repositories.EventRepositoryFirebase
import com.android.sample.data.global.repositories.EventRepository
import com.android.sample.data.hybrid.utils.RemoteSyncError
import com.android.sample.data.local.repositories.EventRepositoryLocal
import com.android.sample.model.calendar.CloudStorageStatus
import com.android.sample.model.calendar.Event
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.time.Instant

/**
 * Hybrid implementation of [EventRepository] that combines local and remote (Firebase) storage.
 *
 * This repository attempts to perform operations on the remote Firebase repository first. If the
 * remote operation fails (e.g., due to network issues), it falls back to the local repository for
 * read operations. For write operations (insert, update, delete), if the remote operation fails, it
 * does not perform the local operation to avoid data inconsistency, but it notifies about the sync
 * error via the [onSyncError] callback.
 *
 * So concerning data consistency, the remote repository is considered the source of truth at all
 * times.
 *
 * @property local The local event repository instance. If not provided, defaults to
 *   [EventRepositoryLocal].
 * @property remote The remote Firebase event repository instance. If not provided, defaults to
 *   [EventRepositoryFirebase].
 */
class EventRepositoryHybrid(
    private val local: EventRepository = EventRepositoryLocal(),
    private val remote: EventRepository = EventRepositoryFirebase(db = Firebase.firestore),
) : EventRepository {
  // Callback to notify about remote sync errors
  var onSyncError: ((RemoteSyncError, Throwable?) -> Unit)? = null

  override fun getNewUid(): String {
    // IDs come from remote to ensure compatibility since Firebase needs a specific format (20
    // characters) and local supports any String.
    return remote.getNewUid()
  }

  override suspend fun getAllEvents(orgId: String): List<Event> {
    // Try getting Events from remote first
    return try {
      remote.getAllEvents(orgId = orgId)
    }
    // If remote fails, take from local and notify sync error
    catch (e: Exception) {
      onSyncError?.invoke(RemoteSyncError.REMOTE_GET_FAILED, e)
      local.getAllEvents(orgId = orgId)
    }
  }

  override suspend fun insertEvent(orgId: String, item: Event) {
    // Calls super to perform organizationId check
    super.insertEvent(orgId = orgId, item = item)

    // Initial assumption: both local and remote will succeed
    val cloudStorageStatuses = setOf(CloudStorageStatus.FIRESTORE, CloudStorageStatus.LOCAL)
    val updatedItem = item.copy(cloudStorageStatuses = cloudStorageStatuses)

    // Try inserting remotely
    try {
      remote.insertEvent(orgId = orgId, item = updatedItem)

      // If successful, insert also locally to keep both in sync
      local.insertEvent(orgId = orgId, item = updatedItem)
    }
    // If remote fails, update impossible, notify sync error
    catch (e: Exception) {
      onSyncError?.invoke(RemoteSyncError.REMOTE_INSERT_FAILED, e)
    }
  }

  override suspend fun updateEvent(orgId: String, itemId: String, item: Event) {
    // Calls super to perform organizationId check
    super.updateEvent(orgId, itemId, item)

    // Initial assumption: both local and remote will succeed
    val cloudStatus = setOf(CloudStorageStatus.FIRESTORE, CloudStorageStatus.LOCAL)
    val updatedItem = item.copy(cloudStorageStatuses = cloudStatus)

    // Try remote update
    try {
      remote.updateEvent(orgId = orgId, itemId = itemId, item = updatedItem)

      // If successful, update also locally to keep both in sync
      local.updateEvent(orgId = orgId, itemId = itemId, item = updatedItem)
    }
    // If remote fails, update local with only impossible, notify sync error
    catch (e: Exception) {
      onSyncError?.invoke(RemoteSyncError.REMOTE_UPDATE_FAILED, e)
    }
  }

  override suspend fun deleteEvent(orgId: String, itemId: String) {
    // Try deleting remotely first
    try {
      remote.deleteEvent(orgId = orgId, itemId = itemId)

      // If successful, delete also locally to keep both in sync
      local.deleteEvent(orgId = orgId, itemId = itemId)
    }
    // If remote fails, notify sync error
    catch (e: Exception) {
      onSyncError?.invoke(RemoteSyncError.REMOTE_DELETE_FAILED, e)
    }
  }

  override suspend fun getEventById(orgId: String, itemId: String): Event? {
    // Get from remote first
    return try {
      remote.getEventById(orgId = orgId, itemId = itemId)
    }
    // If remote fails, get from local and notify sync error
    catch (e: Exception) {
      onSyncError?.invoke(RemoteSyncError.REMOTE_GET_FAILED, e)
      local.getEventById(orgId = orgId, itemId = itemId)
    }
  }

  override suspend fun getEventsBetweenDates(
      orgId: String,
      startDate: Instant,
      endDate: Instant
  ): List<Event> {
    // Try getting from remote first
    return try {
      remote.getEventsBetweenDates(orgId = orgId, startDate = startDate, endDate = endDate)
    }
    // If remote fails, get from local and notify sync error
    catch (e: Exception) {
      onSyncError?.invoke(RemoteSyncError.REMOTE_GET_FAILED, e)
      return local.getEventsBetweenDates(orgId = orgId, startDate = startDate, endDate = endDate)
    }
  }

  override suspend fun calculateWorkedHoursPastEvents(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    // Try getting from remote first
    return try {
      remote.calculateWorkedHoursPastEvents(orgId = orgId, start = start, end = end)
    }
    // If remote fails, get from local and notify sync error
    catch (e: Exception) {
      onSyncError?.invoke(RemoteSyncError.REMOTE_GET_FAILED, e)
      local.calculateWorkedHoursPastEvents(orgId = orgId, start = start, end = end)
    }
  }

  override suspend fun calculateWorkedHoursFutureEvents(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    // Try getting from remote first
    return try {
      remote.calculateWorkedHoursFutureEvents(orgId = orgId, start = start, end = end)
    }
    // If remote fails, get from local and notify sync error
    catch (e: Exception) {
      onSyncError?.invoke(RemoteSyncError.REMOTE_GET_FAILED, e)
      return local.calculateWorkedHoursFutureEvents(orgId = orgId, start = start, end = end)
    }
  }

  override suspend fun calculateWorkedHours(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    // Try getting from remote first
    return try {
      remote.calculateWorkedHours(orgId = orgId, start = start, end = end)
    }
    // If remote fails, get from local and notify sync error
    catch (e: Exception) {
      onSyncError?.invoke(RemoteSyncError.REMOTE_GET_FAILED, e)
      return local.calculateWorkedHours(orgId = orgId, start = start, end = end)
    }
  }
}
