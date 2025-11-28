package com.android.sample.model.calendar

import java.time.Instant

class EventRepositoryLocal() : BaseEventRepository() {

  // In-memory storage for events : map of organization ID to list of events
  private val eventsByOrganization: MutableMap<String, MutableList<Event>> = mutableMapOf()

  override suspend fun getAllEvents(orgId: String): List<Event> {
    return eventsByOrganization[orgId]?.filter { !it.hasBeenDeleted } ?: emptyList()
  }

  override fun getNewUid(): String {
    return java.util.UUID.randomUUID().toString()
  }

  override suspend fun insertEvent(orgId: String, item: Event) {
    // Calls the interface check to ensure the organizationId matches
    super.insertEvent(orgId, item)

    val list = eventsByOrganization.getOrPut(orgId) { mutableListOf() }
    require(list.indexOfFirst { it.id == item.id } == -1) {
      "Item with id ${item.id} already exists."
    }

    val currentUserId = "LOCAL_USER" // later get current user id from auth when implemented
    val updatedItem =
        item.copy(
            locallyStoredBy = item.locallyStoredBy + currentUserId,
            version = System.currentTimeMillis())
    list.add(updatedItem)
  }

  override suspend fun updateEvent(orgId: String, itemId: String, item: Event) {
    // Calls the interface check to ensure the organizationId matches
    super.updateEvent(orgId, itemId, item)

    val list =
        eventsByOrganization[orgId] ?: throw IllegalArgumentException("Organization not found")
    val index = list.indexOfFirst { it.id == itemId }
    require(index != -1) { "Item with id $itemId does not exist." }
    require(!list[index].hasBeenDeleted) { "Cannot update a deleted event." }

    val currentUserId = "LOCAL_USER" // later get current user id from auth when implemented
    val newEvent =
        item.copy(
            locallyStoredBy = listOf(currentUserId),
            version = System.currentTimeMillis(),
            cloudStorageStatuses = emptySet())
    list[index] = newEvent
  }

  override suspend fun deleteEvent(orgId: String, itemId: String) {
    val list =
        eventsByOrganization[orgId] ?: throw IllegalArgumentException("Organization not found")
    val index = list.indexOfFirst { it.id == itemId }
    require(index != -1) { "Item with id $itemId does not exist." }
    require(!list[index].hasBeenDeleted) { "Item with id $itemId has already been deleted." }

    val oldEvent = list[index]

    require(oldEvent.organizationId == orgId) {
      "Event's organizationId ${oldEvent.organizationId} does not match the provided orgId $orgId."
    }

    val newEvent =
        oldEvent.copy(
            version = System.currentTimeMillis(),
            hasBeenDeleted = true,
            cloudStorageStatuses = emptySet())
    list[index] = newEvent
  }

  override suspend fun getEventById(orgId: String, itemId: String): Event? {
    val retrievedEvent = eventsByOrganization[orgId]?.find { it.id == itemId }

    // Return null if the event does not exist or has been deleted
    if (retrievedEvent == null || retrievedEvent.hasBeenDeleted) {
      return null
    }

    require(retrievedEvent.organizationId == orgId) {
      "Event's organizationId ${retrievedEvent.organizationId} does not match the provided orgId $orgId."
    }

    return retrievedEvent
  }

  override suspend fun getEventsBetweenDates(
      orgId: String,
      startDate: Instant,
      endDate: Instant
  ): List<Event> {
    require(startDate <= endDate) { "start date must be before or equal to end date" }
    val retrievedEvents =
        eventsByOrganization[orgId]?.filter {
          it.startDate <= endDate && it.endDate >= startDate && !it.hasBeenDeleted
        } ?: emptyList()

    require(retrievedEvents.all { event -> event.organizationId == orgId }) {
      "One or more events' organizationId do not match the provided orgId $orgId."
    }

    return retrievedEvents
  }

  override suspend fun ensureOrganizationExists(orgId: String) {
    require(eventsByOrganization.containsKey(orgId)) { "Organization with id $orgId not found" }
  }
}
