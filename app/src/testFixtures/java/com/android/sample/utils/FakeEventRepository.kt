package com.android.sample.utils

import com.android.sample.data.global.repositories.BaseEventRepository
import com.android.sample.model.calendar.Event
import java.time.Instant
import java.util.UUID

// Enum to identify repository methods for failure simulation
enum class RepoMethod {
  GET_NEW_UID,
  GET_ALL_EVENTS,
  INSERT_EVENT,
  UPDATE_EVENT,
  DELETE_EVENT,
  GET_EVENT_BY_ID,
  GET_EVENTS_BETWEEN_DATES,
  ENSURE_ORG_EXISTS
}

/** simple FakeEventRepository only for test */
class FakeEventRepository : BaseEventRepository() {
  private val events = mutableListOf<Event>()
  var deletedIds = mutableListOf<String>()

  // Map to simulate failures on any function by name
  // For example, to simulate failure on deleteEvent, add "deleteEvent" to this set
  var failMethods: MutableSet<RepoMethod> = mutableSetOf()

  // Helper to simulate failure on a specific method
  private fun failIfNeeded(method: RepoMethod) {
    if (method in failMethods) throw RuntimeException("${method.name} failed")
  }

  fun add(event: Event) {
    events.add(event)
  }

  override fun getNewUid(): String {
    failIfNeeded(method = RepoMethod.GET_NEW_UID)
    return UUID.randomUUID().toString()
  }

  override suspend fun getAllEvents(orgId: String): List<Event> {
    failIfNeeded(method = RepoMethod.GET_ALL_EVENTS)
    return events.toList()
  }

  override suspend fun insertEvent(orgId: String, item: Event) {
    failIfNeeded(method = RepoMethod.INSERT_EVENT)
    events.add(item)
  }

  override suspend fun updateEvent(orgId: String, itemId: String, item: Event) {
    failIfNeeded(method = RepoMethod.UPDATE_EVENT)
    val idx = events.indexOfFirst { it.id == itemId }
    if (idx != -1) events[idx] = item
  }

  override suspend fun deleteEvent(orgId: String, itemId: String) {
    failIfNeeded(method = RepoMethod.DELETE_EVENT)
    deletedIds.add(itemId)
    events.removeIf { it.id == itemId }
  }

  override suspend fun getEventById(orgId: String, itemId: String): Event? {
    failIfNeeded(method = RepoMethod.GET_EVENT_BY_ID)
    return events.find { it.id == itemId }
  }

  override suspend fun getEventsBetweenDates(
      orgId: String,
      startDate: Instant,
      endDate: Instant
  ): List<Event> {
    failIfNeeded(method = RepoMethod.GET_EVENTS_BETWEEN_DATES)
    return events.filter { it.startDate >= startDate && it.endDate <= endDate }
  }

  override suspend fun ensureOrganizationExists(orgId: String) {
    failIfNeeded(RepoMethod.ENSURE_ORG_EXISTS)
    // Do nothing, as this is a fake repository
  }
}
