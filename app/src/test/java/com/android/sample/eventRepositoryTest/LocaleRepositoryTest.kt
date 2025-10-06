package com.android.sample.model.calandar

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant

// In-memory implementation of EventRepository
class InMemoryEventRepository : EventRepository {

    private val events = mutableMapOf<String, Event>()

    override suspend fun getAllEvents(): List<Event> = events.values.toList()

    override suspend fun insertEvent(item: Event) {
        events[item.id] = item
    }

    override suspend fun updateEvent(itemId: String, item: Event) {
        if (events.containsKey(itemId)) {
            events[itemId] = item.copy(id = itemId)
        }
    }

    override suspend fun deleteEvent(itemId: String) {
        events.remove(itemId)
    }

    override suspend fun getEventById(itemId: String): Event? = events[itemId]

    override suspend fun getEventsBetweenDates(startDate: Instant, endDate: Instant): List<Event> =
        events.values.filter { it.startDate >= startDate && it.endDate <= endDate }

    override suspend fun getAllUnsyncedEvents(db: EventStatus): List<Event> =
        events.values.filter { !it.status.contains(db) }
}

class EventRepositoryIntegrationTest {

    private lateinit var repository: EventRepository

    private val sampleEvent1 = createEvent(
        title = "Event 1",
        description = "Desc 1",
        startDate = Instant.parse("2025-10-07T00:00:00Z"),
        endDate = Instant.parse("2025-10-07T01:00:00Z"),
        status = EventStatus.LOCAL
    )

    private val sampleEvent2 = createEvent(
        title = "Event 2",
        description = "Desc 2",
        startDate = Instant.parse("2025-10-08T00:00:00Z"),
        endDate = Instant.parse("2025-10-08T01:00:00Z"),
        status = EventStatus.FIRESTORE
    )

    @Before
    fun setup() {
        repository = InMemoryEventRepository()
    }



    @Test
    fun updateEventWorks() {
        runBlockingTest { repository.insertEvent(sampleEvent1) }

        val updatedEvent = sampleEvent1.copy(title = "Updated Title")
        runBlockingTest { repository.updateEvent(sampleEvent1.id, updatedEvent) }

        val fetched = runBlockingTest { repository.getEventById(sampleEvent1.id) }
        assertEquals("Updated Title", fetched?.title)
    }

    @Test
    fun deleteEventWorks() {
        runBlockingTest { repository.insertEvent(sampleEvent1) }
        runBlockingTest { repository.deleteEvent(sampleEvent1.id) }

        val fetched = runBlockingTest { repository.getEventById(sampleEvent1.id) }
        assertNull(fetched)
    }

    @Test
    fun getEventsBetweenDatesWorks() {
        runBlockingTest {
            repository.insertEvent(sampleEvent1)
            repository.insertEvent(sampleEvent2)
        }

        val start = Instant.parse("2025-10-07T00:00:00Z")
        val end = Instant.parse("2025-10-07T23:59:59Z")

        val eventsInRange = runBlockingTest { repository.getEventsBetweenDates(start, end) }

        assertEquals(1, eventsInRange.size)
        assertEquals(sampleEvent1, eventsInRange[0])
    }

    @Test
    fun getAllUnsyncedEventsWorks() {
        runBlockingTest {
            repository.insertEvent(sampleEvent1)
            repository.insertEvent(sampleEvent2)
        }

        val unsyncedLocal = runBlockingTest { repository.getAllUnsyncedEvents(EventStatus.LOCAL) }
        val unsyncedFirestore = runBlockingTest { repository.getAllUnsyncedEvents(EventStatus.FIRESTORE) }

        assertEquals(1, unsyncedFirestore.size)
        assertEquals(sampleEvent1, unsyncedFirestore[0])

        assertEquals(1, unsyncedLocal.size)
        assertEquals(sampleEvent2, unsyncedLocal[0])
    }

    // Helper for running suspend functions in JUnit4 tests
    private fun <T> runBlockingTest(block: suspend () -> T): T =
        kotlinx.coroutines.runBlocking { block() }
}