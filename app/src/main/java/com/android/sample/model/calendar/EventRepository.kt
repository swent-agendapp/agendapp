package com.android.sample.model.calendar

import com.android.sample.model.constants.FirestoreConstants.MAP_COLLECTION_PATH
import java.time.Instant

interface EventRepository {


  fun getNewUid(): String

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
   * @throws IllegalArgumentException if the itemId does not exist.
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
   * Retrieves all event items that overlap the given date range [startDate, endDate] (inclusive).
   *
   * An event is considered overlapping the range if both of the following are true:
   * - the event starts on or before `endDate` (event.startDate <= endDate), and
   * - the event ends on or after `startDate` (event.endDate >= startDate).
   *
   * @param startDate The start (inclusive) of the range to query.
   * @param endDate The end (inclusive) of the range to query. Must be >= startDate.
   * @return A list of event items overlapping the specified date range.
   * @throws IllegalArgumentException if `startDate` is after `endDate`.
   * @PerformanceNote: Implementations may use a linear scan and filter in memory. For large
   *   datasets, consider indexing or delegating the overlap check to the backing store when
   *   supported.
   */
  suspend fun getEventsBetweenDates(startDate: Instant, endDate: Instant): List<Event>
}
