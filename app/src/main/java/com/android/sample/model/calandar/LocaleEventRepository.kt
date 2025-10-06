package com.android.sample.model.calandar

import java.time.Instant

class LocaleEventRepository : EventRepository {
    private val events: MutableList<Event> = mutableListOf()


    override suspend fun getAllEvents(): List<Event> {
        return events
    }

    override suspend fun insertEvent(item: Event) {
        events.add(item)
    }

    override suspend fun updateEvent(itemId: String, item: Event) {
        val index = events.indexOfFirst { it.id == itemId }
        if (index != -1) {
            events[index] = item
        }
    }

    override suspend fun deleteEvent(itemId: String) {
        events.removeAll { it.id == itemId }
    }

    override suspend fun getEventById(itemId: String): Event? {
        return events.find { it.id == itemId }
    }

    override suspend fun getEventsBetweenDates(startDate: Instant, endDate: Instant): List<Event> {
        require(startDate <= endDate) { "start date must be before or equal to end date" }
        return events.filter { it.startDate >= startDate && it.endDate <= endDate }
    }

    override suspend fun getAllUnsyncedEvents(db: EventStatus): List<Event> {
        return events.filter { db !in it.status }
    }
}