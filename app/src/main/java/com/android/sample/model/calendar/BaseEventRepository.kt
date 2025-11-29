package com.android.sample.model.calendar

import java.time.Instant

/**
 * Provides shared implementations for calculating worked hours across different event repositories.
 * Implementations only need to supply an organization existence check and event retrieval.
 */
abstract class BaseEventRepository : EventRepository {

  /** Ensures the given organization exists in the backing store. */
  protected abstract suspend fun ensureOrganizationExists(orgId: String)

  override suspend fun calculateWorkedHoursPastEvents(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    ensureOrganizationExists(orgId)

    val events = getEventsBetweenDates(orgId, start, end)
    val now = Instant.now()

    val workedHoursMap = mutableMapOf<String, Double>()
    events.forEach { event ->
      if (event.startDate <= now) {
        val durationHours =
            java.time.Duration.between(event.startDate, event.endDate).toMinutes() / 60.0
        event.presence.forEach { (userId, isPresent) ->
          if (isPresent) {
            workedHoursMap[userId] = workedHoursMap.getOrDefault(userId, 0.0) + durationHours
          }
        }
      }
    }

    return workedHoursMap.toList()
  }

  override suspend fun calculateWorkedHoursFutureEvents(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    ensureOrganizationExists(orgId)

    val events = getEventsBetweenDates(orgId, start, end)
    val now = Instant.now()

    val workedHoursMap = mutableMapOf<String, Double>()
    events.forEach { event ->
      if (event.startDate > now) {
        val durationHours =
            java.time.Duration.between(event.startDate, event.endDate).toMinutes() / 60.0
        event.participants.forEach { userId ->
          workedHoursMap[userId] = workedHoursMap.getOrDefault(userId, 0.0) + durationHours
        }
      }
    }

    return workedHoursMap.toList()
  }

  override suspend fun calculateWorkedHours(
      orgId: String,
      start: Instant,
      end: Instant
  ): List<Pair<String, Double>> {
    ensureOrganizationExists(orgId)

    val pastHours = calculateWorkedHoursPastEvents(orgId, start, end).toMap()
    val futureHours = calculateWorkedHoursFutureEvents(orgId, start, end).toMap()

    val allEmployeeIds = (pastHours.keys + futureHours.keys).toSet()
    val combinedHours =
        allEmployeeIds.map { employeeId ->
          val total =
              pastHours.getOrDefault(employeeId, 0.0) + futureHours.getOrDefault(employeeId, 0.0)
          employeeId to total
        }

    return combinedHours
  }
}
