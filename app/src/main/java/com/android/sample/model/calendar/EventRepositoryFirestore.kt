package com.android.sample.model.calendar

import java.time.Instant

class EventRepositoryFirestore : EventRepository {

    override suspend fun getAllEvents(): List<Event> {
        TODO("Not yet implemented")
    }

    override suspend fun insertEvent(item: Event) {
        TODO("Not yet implemented")
    }

    override suspend fun updateEvent(
        itemId: String,
        item: Event
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteEvent(itemId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getEventById(itemId: String): Event? {
        TODO("Not yet implemented")
    }

    override suspend fun getEventsBetweenDates(
        startDate: Instant,
        endDate: Instant
    ): List<Event> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllUnsyncedEvents(db: StorageStatus): List<Event> {
        TODO("Not yet implemented")
    }
}