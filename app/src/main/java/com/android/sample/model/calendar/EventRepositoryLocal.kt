package com.android.sample.model.calendar

import com.android.sample.ui.theme.EventPalette
import java.time.Duration
import java.time.Instant

class EventRepositoryLocal(preloadSampleData: Boolean = false) : EventRepository {
  private val events: MutableList<Event> = mutableListOf()

  init {
    if (preloadSampleData) {
      populateSampleEvents()
    }
  }

  override suspend fun getAllEvents(): List<Event> {
    return events.filter { !it.hasBeenDeleted }
  }

  override suspend fun insertEvent(item: Event) {
    require(events.indexOfFirst { it.id == item.id } == -1) {
      "Item with id ${item.id} already exists."
    }
    val currentUserId = "LOCAL_USER" // later get current user id from auth when implemented
    val updatedItem =
        item.copy(
            locallyStoredBy = item.locallyStoredBy + currentUserId,
            version = System.currentTimeMillis())
    events.add(updatedItem)
  }

  override suspend fun updateEvent(itemId: String, item: Event) {
    require(!item.hasBeenDeleted) { "Cannot update a deleted event." }
    val index = events.indexOfFirst { it.id == itemId }
    require(index != -1) { "Item with id $itemId does not exist." }
    require(!events[index].hasBeenDeleted) { "Cannot update a deleted event." }

    val currentUserId = "LOCAL_USER" // later get current user id from auth when implemented
    val newEvent =
        item.copy(
            locallyStoredBy = listOf(currentUserId),
            version = System.currentTimeMillis(),
            cloudStorageStatuses = emptySet())
    events[index] = newEvent
  }

  override suspend fun deleteEvent(itemId: String) {
    val index = events.indexOfFirst { it.id == itemId }
    require(index != -1) { "Item with id $itemId does not exist." }
    require(!events[index].hasBeenDeleted) { "Item with id $itemId has already been deleted." }

    val oldEvent = events[index]
    val newEvent =
        oldEvent.copy(
            version = System.currentTimeMillis(),
            hasBeenDeleted = true,
            cloudStorageStatuses = emptySet())
    events[index] = newEvent
  }

  override suspend fun getEventById(itemId: String): Event? {
    return events.find { it.id == itemId }
  }

  override suspend fun getEventsBetweenDates(startDate: Instant, endDate: Instant): List<Event> {
    require(startDate <= endDate) { "start date must be before or equal to end date" }
    return events.filter { it.startDate <= endDate && it.endDate >= startDate }
  }

  // ---------------------------------------------------------------------------------
  // New additions below to add sample data for Edit Event screen preview/testing
  // ---------------------------------------------------------------------------------

  /** Preload local repository with fake events for testing / Edit VM preview. */
  private fun populateSampleEvents() {
    val now = Instant.now()
    val event1 =
        Event(
            id = "E001",
            title = "Weekly Team Meeting",
            description = "Discuss ongoing project progress and next steps.",
            startDate = now.plus(Duration.ofHours(2)),
            endDate = now.plus(Duration.ofHours(3)),
            participants = setOf("Alice", "Bob", "Charlie"),
            recurrenceStatus = RecurrenceStatus.Weekly,
            hasBeenDeleted = false,
            color = EventPalette.Blue,
            // notifications = listOf("30 min before"),
            version = System.currentTimeMillis(),
            locallyStoredBy = listOf("LOCAL_USER"),
            cloudStorageStatuses = emptySet(),
            personalNotes = null)

    val event2 =
        Event(
            id = "E002",
            title = "Client Demo Preparation",
            description = "Prepare slides and setup for Friday client demo.",
            startDate = now.plus(Duration.ofDays(1)),
            endDate = now.plus(Duration.ofDays(1)).plus(Duration.ofHours(1)),
            participants = setOf("David", "Eve"),
            recurrenceStatus = RecurrenceStatus.OneTime,
            hasBeenDeleted = false,
            color = EventPalette.Green,
            // notifications = listOf("1 hour before"),
            version = System.currentTimeMillis(),
            locallyStoredBy = listOf("LOCAL_USER"),
            cloudStorageStatuses = emptySet(),
            personalNotes = null)

    if (events.isEmpty()) {
      events.addAll(listOf(event1, event2))
    }
  }
}
