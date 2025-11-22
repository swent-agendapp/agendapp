package com.android.sample.model.calendar

import java.time.Instant

interface EventRepository {

  fun getNewUid(): String

  /**
   * Retrieves all event items from the repository.
   *
   * @param orgId The organization ID to which the events belong.
   * @return A list of all event items.
   */
  suspend fun getAllEvents(orgId: String): List<Event>

  /**
   * Inserts a new event item into the repository.
   *
   * Note: Implementations have to call super.insertEvent to perform the organizationId check and
   * the deletion status check.
   *
   * @param orgId The organization ID to which the event belongs.
   * @param item The event item to be inserted.
   */
  suspend fun insertEvent(orgId: String, item: Event) {
    require(item.organizationId == orgId) {
      "Event's organizationId ${item.organizationId} does not match the provided orgId $orgId."
    }
    require(!item.hasBeenDeleted) { "Cannot insert an event that is marked as deleted." }
  }

  /**
   * Updates an existing event item in the repository.
   *
   * Note: Implementations have to call super.updateEvent to perform the organizationId check and
   * the deletion status check.
   *
   * @param orgId The organization ID to which the event belongs.
   * @param itemId The unique identifier of the event item to be updated.
   * @param item The event item to be updated.
   */
  suspend fun updateEvent(orgId: String, itemId: String, item: Event) {
    require(item.organizationId == orgId) {
      "Event's organizationId ${item.organizationId} does not match the provided orgId $orgId."
    }
    require(!item.hasBeenDeleted) { "Cannot insert an event that is marked as deleted." }
  }
  /**
   * Deletes an event item from the repository.
   *
   * @param orgId The organization ID to which the event belongs.
   * @param itemId The event item to be deleted.
   * @throws IllegalArgumentException if the itemId does not exist.
   */
  suspend fun deleteEvent(orgId: String, itemId: String)
  /**
   * Retrieves an event item by its unique identifier.
   *
   * @param orgId The organization ID to which the event belongs.
   * @param itemId The unique identifier of the event item.
   * @return The event item if found, or null if not found.
   */
  suspend fun getEventById(orgId: String, itemId: String): Event?

  /**
   * Retrieves all event items that overlap the given date range [startDate, endDate] (inclusive).
   *
   * An event is considered overlapping the range if both of the following are true:
   * - the event starts on or before `endDate` (event.startDate <= endDate), and
   * - the event ends on or after `startDate` (event.endDate >= startDate).
   *
   * @param orgId The organization ID to which the events belong.
   * @param startDate The start (inclusive) of the range to query.
   * @param endDate The end (inclusive) of the range to query. Must be >= startDate.
   * @return A list of event items overlapping the specified date range.
   * @throws IllegalArgumentException if `startDate` is after `endDate`.
   * @PerformanceNote: Implementations may use a linear scan and filter in memory. For large
   *   datasets, consider indexing or delegating the overlap check to the backing store when
   *   supported.
   */
  suspend fun getEventsBetweenDates(
      orgId: String,
      startDate: Instant,
      endDate: Instant
  ): List<Event>
}
