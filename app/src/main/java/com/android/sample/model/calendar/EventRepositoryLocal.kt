package com.android.sample.model.calendar

import java.time.Instant

class EventRepositoryLocal : EventRepository {
  private val events: MutableList<Event> = mutableListOf()

  override suspend fun getAllEvents(): List<Event> {
    return events.filter { !it.hasBeenDeleted }
  }

  override suspend fun insertEvent(item: Event) {
    require(events.indexOfFirst { it.id == item.id } == -1) {
      "Item with id ${item.id} already exists."
    }
    val currentUserId = "" // todo get current user id from auth when implemented
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

    val currentUserId = "" // todo get current user id from auth when implemented
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
    require(!events[index].hasBeenDeleted)

    val currentUserId = "" // todo get current user id from auth when implemented
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
    return events.filter { it.startDate >= startDate && it.endDate <= endDate }
  }
}
