package com.android.sample.utils

import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import java.time.Instant

/** simple FakeEventRepository only for test */
class FakeEventRepository(private val event: Event) : EventRepository {

  var deletedIds = mutableListOf<String>()
  var shouldFailDelete = false

  override suspend fun getAllEvents(orgId: String): List<Event> = listOf(event)

  override suspend fun insertEvent(orgId: String, item: Event) {}

  override suspend fun updateEvent(orgId: String, itemId: String, item: Event) {}

  override suspend fun deleteEvent(orgId: String, itemId: String) {
    if (shouldFailDelete) throw RuntimeException("delete failed")
    deletedIds.add(itemId)
  }

  override suspend fun getEventById(orgId: String, itemId: String): Event? =
      if (itemId == event.id) event else null

  override suspend fun getEventsBetweenDates(
      orgId: String,
      startDate: Instant,
      endDate: Instant
  ): List<Event> = listOf(event)
}
