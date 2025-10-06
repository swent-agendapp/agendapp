package com.android.sample.model.calandar

import java.time.Instant

interface EventRepository {
    /**
     * Retrieves all event items from the repository.
     *
     * @return A list of all event items.
     */
    suspend fun getAllEvents(): List<Event>

    /**
     * Inserts a new event item into the repository.
     *
     * @param item The event item to be inserted.
     */
    suspend fun insertEvent(item: Event)

    /**
     * Updates an existing event item in the repository.
     *
     * @param item The event item to be updated.
     */
    suspend fun updateEvent(itemId: String, item: Event)
    /**
     * Deletes an event item from the repository.
     *
     * @param itemId The event item to be deleted.
     */
    suspend fun deleteEvent(itemId: String)
    /**
     * Retrieves an event item by its unique identifier.
     *
     * @param itemId The unique identifier of the event item.
     * @return The event item if found, or null if not found.
     */
    suspend fun getEventById(itemId: String): Event?

    /**
     * Retrieves all event items occurring between two dates (inclusive).
     *
     * @param startDate The start date of the range.
     * @param endDate The end date of the range.
     * @return A list of event items occurring between the specified dates (inclusive).
     * @throws IllegalArgumentException if the date format is invalid or if startDate is after endDate.
     * @PerformanceNote: Uses linear scan. Could improve to TreeSet for O(log n) range queries if needed.
     *
     */
    suspend fun getEventsBetweenDates(startDate: Instant, endDate: Instant): List<Event>


    /**
     * Retrieves all event items that have **not yet been synchronized** with the specified database.
     *
     * @param db The target database or storage system to check synchronization against.
     * @return A list of event items pending synchronization for the given database.
     *         Returns an empty list if all events are already synced to that database.
     */
    suspend fun getAllUnsyncedEvents(db: EventStatus): List<Event>

}