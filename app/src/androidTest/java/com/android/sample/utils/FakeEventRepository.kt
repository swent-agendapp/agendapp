package com.android.sample.utils

import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import java.time.Instant
import java.util.UUID

/** simple FakeEventRepository only for test */
class FakeEventRepository : EventRepository {

  private val events = mutableListOf<Event>()
  var deletedIds = mutableListOf<String>()
  var shouldFailDelete = false
  var workedHoursResult: List<Pair<String, Double>> = emptyList()
  var shouldThrowOnCalculateWorkedHours = false

  fun add(event: Event) {
    events.add(event)
  }

  override fun getNewUid(): String = UUID.randomUUID().toString()

  override suspend fun getAllEvents(orgId: String): List<Event> = events.toList()

  override suspend fun insertEvent(orgId: String, item: Event) {
    events.add(item)
  }

  override suspend fun updateEvent(orgId: String, itemId: String, item: Event) {
    val idx = events.indexOfFirst { it.id == itemId }
    if (idx != -1) events[idx] = item
  }

  override suspend fun deleteEvent(orgId: String, itemId: String) {
    if (shouldFailDelete) throw RuntimeException("delete failed")
    deletedIds.add(itemId)
    events.removeIf { it.id == itemId }
  }

  override suspend fun getEventById(orgId: String, itemId: String): Event? =
      events.find { it.id == itemId }

  override suspend fun getEventsBetweenDates(
      orgId: String,
      startDate: Instant,
      endDate: Instant
  ): List<Event> =
      events.filter { it.endDate >= startDate && it.startDate <= endDate && !it.hasBeenDeleted }

  override suspend fun calculateWorkedHoursPastEvents(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    if (shouldThrowOnCalculateWorkedHours) throw IllegalArgumentException("Failed")
    return workedHoursResult
  }

  override suspend fun calculateWorkedHoursFutureEvents(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    return emptyList() // Default to no future hours in tests
  }

  override suspend fun calculateWorkedHours(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    if (shouldThrowOnCalculateWorkedHours) throw RuntimeException("error!")
    return workedHoursResult
  }
}
