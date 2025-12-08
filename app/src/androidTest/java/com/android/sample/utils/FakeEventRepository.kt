package com.android.sample.utils

import com.android.sample.data.global.repositories.EventRepository
import com.android.sample.model.calendar.Event
import java.time.Instant
import java.util.UUID

/** simple FakeEventRepository only for test */
class FakeEventRepository : EventRepository {

  private val events = mutableListOf<Event>()
  var deletedIds = mutableListOf<String>()
  var shouldFailDelete = false

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
  ): List<Event> = events.filter { it.startDate >= startDate && it.endDate <= endDate }

  override suspend fun calculateWorkedHoursPastEvents(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    TODO("Not yet implemented")
  }

  override suspend fun calculateWorkedHoursFutureEvents(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    TODO("Not yet implemented")
  }

  override suspend fun calculateWorkedHours(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    TODO("Not yet implemented")
  }
}
