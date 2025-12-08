package com.android.sample.data.hybrid.repositories

import com.android.sample.data.firebase.repositories.EventRepositoryFirebase
import com.android.sample.data.global.repositories.EventRepository
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
 * remote operation fails (e.g., due to network issues), it falls back to the local repository. It
 * also ensures synchronization between local and remote data when possible.
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

  override fun getNewUid(): String {
    // IDs come from remote to ensure compatibility since Firebase needs a specific format (20
    // characters) and local supports any String.
    return remote.getNewUid()
  }

  override suspend fun getAllEvents(orgId: String): List<Event> {
    // Try getting Events from remote first
    try {
      val remoteEvents = remote.getAllEvents(orgId = orgId)

      // Update local cache to match remote
      synchronizeLocal(orgId = orgId, remoteEvents = remoteEvents)

      return remoteEvents
    }
    // If remote fails, take from local
    catch (_: Exception) {
      val localEvents = local.getAllEvents(orgId = orgId)
      return localEvents
    }
  }

  override suspend fun insertEvent(orgId: String, item: Event) {
    // Calls super to perform organizationId check
    super.insertEvent(orgId = orgId, item = item)

    // Initial assumption: both local and remote will succeed
    var cloudStorageStatuses = setOf(CloudStorageStatus.FIRESTORE, CloudStorageStatus.LOCAL)
    var updatedItem = item.copy(cloudStorageStatuses = cloudStorageStatuses)

    // Try inserting remotely
    try {
      remote.insertEvent(orgId = orgId, item = updatedItem)
    }
    // If remote fails, mark as LOCAL only
    catch (_: Exception) {
      cloudStorageStatuses = setOf(CloudStorageStatus.LOCAL)
    }

    // Insert locally
    updatedItem = updatedItem.copy(cloudStorageStatuses = cloudStorageStatuses)
    local.insertEvent(orgId = orgId, item = updatedItem)
  }

  override suspend fun updateEvent(orgId: String, itemId: String, item: Event) {
    // Calls super to perform organizationId check
    super.updateEvent(orgId, itemId, item)

    // Initial assumption: both local and remote will succeed
    var cloudStatus = setOf(CloudStorageStatus.FIRESTORE, CloudStorageStatus.LOCAL)
    var updatedItem = item.copy(cloudStorageStatuses = cloudStatus)

    // Try remote update
    try {
      remote.updateEvent(orgId = orgId, itemId = itemId, item = updatedItem)
    } catch (_: Exception) {
      cloudStatus = setOf(CloudStorageStatus.LOCAL)
    }

    // Always update local
    updatedItem = updatedItem.copy(cloudStorageStatuses = cloudStatus)
    local.updateEvent(orgId = orgId, itemId = itemId, item = updatedItem)
  }

  override suspend fun deleteEvent(orgId: String, itemId: String) {
    // Try deleting remotely first
    try {
      remote.deleteEvent(orgId = orgId, itemId = itemId)
    } catch (_: Exception) {
      // Ignore remote deletion failure
    }

    // Always delete locally
    local.deleteEvent(orgId = orgId, itemId = itemId)
  }

  override suspend fun getEventById(orgId: String, itemId: String): Event? {
    // Get from remote first
    try {
      val remoteEvent = remote.getEventById(orgId = orgId, itemId = itemId)
      if (remoteEvent != null) {
        // Sync local for this item only
        local.updateEvent(orgId = orgId, itemId = itemId, item = remoteEvent)
      }
      return remoteEvent
    } catch (_: Exception) {
      // Try getting from local if remote fails
      return local.getEventById(orgId = orgId, itemId = itemId)
    }
  }

  override suspend fun getEventsBetweenDates(
      orgId: String,
      startDate: Instant,
      endDate: Instant
  ): List<Event> {
    // Try getting from remote first
    try {
      val remoteEvents =
          remote.getEventsBetweenDates(orgId = orgId, startDate = startDate, endDate = endDate)

      // Update local cache for all retrieved events
      synchronizeLocal(orgId = orgId, remoteEvents = remoteEvents)

      return remoteEvents
    } catch (_: Exception) {
      // If remote fails, get from local
      return local.getEventsBetweenDates(orgId = orgId, startDate = startDate, endDate = endDate)
    }
  }

  override suspend fun calculateWorkedHoursPastEvents(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    // Try getting from remote first
    try {
      val hours = remote.calculateWorkedHoursPastEvents(orgId = orgId, start = start, end = end)

      // Sync local with all remote events
      synchronizeLocal(orgId = orgId, remoteEvents = remote.getAllEvents(orgId = orgId))

      return hours
    } catch (_: Exception) {
      // If remote fails, get from local
      return local.calculateWorkedHoursPastEvents(orgId = orgId, start = start, end = end)
    }
  }

  override suspend fun calculateWorkedHoursFutureEvents(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    // Try getting from remote first
    try {
      val hours = remote.calculateWorkedHoursFutureEvents(orgId = orgId, start = start, end = end)

      // Sync local with all remote events
      synchronizeLocal(orgId = orgId, remoteEvents = remote.getAllEvents(orgId = orgId))

      return hours
    } catch (_: Exception) {
      // If remote fails, get from local
      return local.calculateWorkedHoursFutureEvents(orgId = orgId, start = start, end = end)
    }
  }

  override suspend fun calculateWorkedHours(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    // Try getting from remote first
    try {
      val hours = remote.calculateWorkedHours(orgId = orgId, start = start, end = end)

      // Sync local with all remote events
      synchronizeLocal(orgId = orgId, remoteEvents = remote.getAllEvents(orgId = orgId))

      return hours
    } catch (_: Exception) {
      // If remote fails, get from local
      return local.calculateWorkedHours(orgId = orgId, start = start, end = end)
    }
  }

  /**
   * Synchronizes local events with the provided remote events.
   *
   * For each event in the remote list:
   * - If it doesn't exist locally, it is inserted.
   * - If it exists locally, versions are compared:
   *     - If remote is newer, local is updated.
   *     - If local is newer, remote is updated.
   *     - If versions are equal, ensure cloud storage statuses are correct.
   *
   * After processing remote events, any local events not present in remote are pushed to remote.
   *
   * @param orgId The organization ID.
   * @param remoteEvents The list of events retrieved from remote storage.
   */
  private suspend fun synchronizeLocal(orgId: String, remoteEvents: List<Event>) {
    // Get all local events
    val localEvents = local.getAllEvents(orgId = orgId)

    // Create maps for remote and local events for easy lookup
    val remoteMap = remoteEvents.associateBy { it.id }
    val localMap = localEvents.associateBy { it.id }

    // Sets of IDs for easy existence checks
    val remoteIds = remoteMap.keys

    // Assume both local and remote storage
    val cloudStorageStatusBoth = setOf(CloudStorageStatus.FIRESTORE, CloudStorageStatus.LOCAL)

    // First look at events present in remote
    for ((id, remoteEvent) in remoteMap) {

      // Get corresponding local event if exists
      val localEvent = localMap[id]

      // If not in local, insert it
      if (localEvent == null) {
        val updated = remoteEvent.copy(cloudStorageStatuses = cloudStorageStatusBoth)
        local.insertEvent(orgId = orgId, item = updated)
        continue
      }

      // If in both, compare versions
      if (remoteEvent.version > localEvent.version) {
        // If remote is newer -> update event in local
        val updated = remoteEvent.copy(cloudStorageStatuses = cloudStorageStatusBoth)
        local.updateEvent(orgId = orgId, itemId = id, item = updated)
      }

      // If in both, and local is newer
      else if (remoteEvent.version < localEvent.version) {
        // Put local event to remote
        val updated = localEvent.copy(cloudStorageStatuses = cloudStorageStatusBoth)
        try {
          remote.updateEvent(orgId = orgId, item = updated, itemId = id)
        } catch (_: Exception) {
          // If remote update fails, revert local event to LOCAL only
          val fallback = updated.copy(cloudStorageStatuses = setOf(CloudStorageStatus.LOCAL))
          local.updateEvent(orgId = orgId, itemId = id, item = fallback)
        }
      }
      // If versions are equal
      else {
        // Ensure cloud storage statuses are correct (both FIRESTORE and LOCAL)
        val updated = remoteEvent.copy(cloudStorageStatuses = cloudStorageStatusBoth)
        local.updateEvent(orgId = orgId, itemId = id, item = updated)
      }
    }

    // Look at events present in local
    for ((id, localEvent) in localMap) {
      // If not in remote
      if (!remoteIds.contains(id)) {

        // Put local event to remote (with both FIRESTORE and LOCAL as status)
        try {
          val updated = localEvent.copy(cloudStorageStatuses = cloudStorageStatusBoth)
          remote.insertEvent(orgId = orgId, item = updated)
          // Local event updated to reflect remote storage
          local.updateEvent(orgId = orgId, itemId = id, item = updated)
        } catch (_: Exception) {
          // If remote insert fails, revert local event to LOCAL only
          val fallback = localEvent.copy(cloudStorageStatuses = setOf(CloudStorageStatus.LOCAL))
          local.updateEvent(orgId = orgId, itemId = id, item = fallback)
        }
      }
    }
  }
}
