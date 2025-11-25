package com.android.sample.model.calendar

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.android.sample.R
import com.android.sample.ui.calendar.utils.DateTimeUtils
import com.android.sample.ui.theme.EventPalette
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

/**
 * Data class representing a calendar event.
 *
 * @property id Unique identifier for the event.
 * @property organizationId Identifier for the organization the event belongs to.
 * @property title Title of the event.
 * @property description Description of the event.
 * @property startDate Start date and time of the event.
 * @property endDate End date and time of the event.
 * @property cloudStorageStatuses Set of storage locations where the event is saved (e.g., local,
 *   Firestore).
 * @property personalNotes Optional personal notes for the event.
 * @property participants Set of user IDs participating in the event.
 * @property version timestamp of last modification, used for conflict resolution.
 * @property hasBeenDeleted Flag indicating if the event has been deleted.
 * @property recurrenceStatus Recurrence pattern of the event (e.g., one-time, weekly).
 * @property color Color used to display the event in the UI.
 * @property locallyStoredBy List of user IDs who have the event stored locally.
 */
data class Event(
    val id: String,
    val organizationId: String,
    val title: String,
    val description: String,
    val startDate: Instant,
    val endDate: Instant,
    val cloudStorageStatuses: Set<CloudStorageStatus>,
    val locallyStoredBy: List<String> = emptyList(),
    val personalNotes: String?,
    val participants: Set<String>,
    val version: Long,
    val recurrenceStatus: RecurrenceStatus,
    val hasBeenDeleted: Boolean = false,
    val color: Color
) {
  // Returns the start date as a LocalDate in the system's default time zone
  val startLocalDate: LocalDate
    get() = startDate.atZone(ZoneId.systemDefault()).toLocalDate()

  // Returns the end date as a LocalDate in the system's default time zone
  val endLocalDate: LocalDate
    get() = endDate.atZone(ZoneId.systemDefault()).toLocalDate()

  // Returns the start time as a LocalTime in the system's default time zone
  val startLocalTime: LocalTime
    get() = startDate.atZone(ZoneId.systemDefault()).toLocalTime()

  // Returns the end time as a LocalTime in the system's default time zone
  val endLocalTime: LocalTime
    get() = endDate.atZone(ZoneId.systemDefault()).toLocalTime()
}

/** Enum representing the recurrence pattern of an event. */
enum class RecurrenceStatus {
  OneTime,
  Daily,
  Weekly,
  Monthly,
  Yearly
}

/** Enum representing the cloud storage location of an event. */
enum class CloudStorageStatus {
  FIRESTORE,
}

/**
 * Factory function to create a new Event instance with a generated unique ID and default values.
 *
 * @param organizationId Identifier for the organization the event belongs to.
 * @param title Title of the event.
 * @param description Description of the event.
 * @param startDate Start date and time of the event.
 * @param endDate End date and time of the event.
 * @param cloudStorageStatuses Set of storage locations for the event.
 * @param personalNotes Optional personal notes.
 * @param participants Set of user IDs participating in the event.
 * @param color Color used to display the event in the UI.
 * @return A new Event instance.
 */
fun createEvent(
    organizationId: String,
    repository: EventRepository? = null,
    title: String = "Untitled",
    description: String = "",
    startDate: Instant = Instant.now(),
    endDate: Instant = Instant.now(),
    cloudStorageStatuses: Set<CloudStorageStatus> = emptySet(),
    personalNotes: String? = null,
    participants: Set<String> = emptySet(),
    color: Color = EventPalette.Blue,
    recurrence: RecurrenceStatus = RecurrenceStatus.OneTime,
    endRecurrence: Instant = Instant.now(),
): List<Event> {
  require(!endDate.isBefore(startDate)) { "End date cannot be before start date" }
  val zone = ZoneId.systemDefault()

  return when (recurrence) {
    RecurrenceStatus.OneTime ->
        listOf(
            Event(
                id = repository?.getNewUid() ?: UUID.randomUUID().toString(),
                organizationId = organizationId,
                title = title,
                description = description,
                startDate = startDate,
                endDate = endDate,
                cloudStorageStatuses = cloudStorageStatuses,
                personalNotes = personalNotes,
                participants = participants,
                version = System.currentTimeMillis(),
                recurrenceStatus = recurrence,
                color = color))
    RecurrenceStatus.Daily -> {
      val days =
          1 +
              ChronoUnit.DAYS.between(
                  startDate.atZone(ZoneOffset.UTC), endRecurrence.atZone(ZoneOffset.UTC))
      List(days.toInt()) { i ->
        Event(
            id = repository?.getNewUid() ?: UUID.randomUUID().toString(),
            title = title,
            description = description,
            startDate = startDate.plus(i * 1L, ChronoUnit.DAYS),
            endDate = endDate.plus(i * 1L, ChronoUnit.DAYS),
            cloudStorageStatuses = cloudStorageStatuses,
            personalNotes = personalNotes,
            participants = participants,
            version = System.currentTimeMillis(),
            recurrenceStatus = recurrence,
            color = color)
      }
    }
    RecurrenceStatus.Weekly -> {
      val weeks =
          1 +
              ChronoUnit.WEEKS.between(
                  startDate.atZone(ZoneOffset.UTC), endRecurrence.atZone(ZoneOffset.UTC))
      List(weeks.toInt()) { i ->
        Event(
            id = repository?.getNewUid() ?: UUID.randomUUID().toString(),
            organizationId = organizationId,
            title = title,
            description = description,
            startDate = startDate.plus(i * 7L, ChronoUnit.DAYS),
            endDate = endDate.plus(i * 7L, ChronoUnit.DAYS),
            cloudStorageStatuses = cloudStorageStatuses,
            personalNotes = personalNotes,
            participants = participants,
            version = System.currentTimeMillis(),
            recurrenceStatus = recurrence,
            color = color)
      }
    }
    RecurrenceStatus.Monthly -> {
      val months =
          1 +
              ChronoUnit.MONTHS.between(
                  startDate.atZone(ZoneOffset.UTC), endRecurrence.atZone(ZoneOffset.UTC))
      List(months.toInt()) { i ->
        Event(
            id = repository?.getNewUid() ?: UUID.randomUUID().toString(),
            organizationId = organizationId,
            title = title,
            description = description,
            startDate = startDate.atZone(zone).plusMonths(i * 1L).toInstant(),
            endDate = endDate.atZone(zone).plusMonths(i * 1L).toInstant(),
            cloudStorageStatuses = cloudStorageStatuses,
            personalNotes = personalNotes,
            participants = participants,
            version = System.currentTimeMillis(),
            recurrenceStatus = recurrence,
            color = color)
      }
    }
    RecurrenceStatus.Yearly -> {
      val years =
          1 +
              ChronoUnit.YEARS.between(
                  startDate.atZone(ZoneOffset.UTC), endRecurrence.atZone(ZoneOffset.UTC))
      List(years.toInt()) { i ->
        Event(
            id = repository?.getNewUid() ?: UUID.randomUUID().toString(),
            organizationId = organizationId,
            title = title,
            description = description,
            startDate = startDate.atZone(zone).plusYears(i * 1L).toInstant(),
            endDate = endDate.atZone(zone).plusYears(i * 1L).toInstant(),
            cloudStorageStatuses = cloudStorageStatuses,
            personalNotes = personalNotes,
            participants = participants,
            version = System.currentTimeMillis(),
            recurrenceStatus = recurrence,
            color = color)
      }
    }
  }
}

/**
 * Helper to create an [Event] the given times using [DateTimeUtils] and the real [createEvent]
 * factory.
 *
 * This ensures we rely on the same time conversion utilities as the production code.
 *
 * @param organizationId Identifier for the organization the event belongs to.
 * @param title Title of the event.
 * @param startHour Start hour of the event.
 * @param startMinute Start minute of the event.
 * @param endHour End hour of the event.
 * @param endMinute End minute of the event.
 * @return A new Event instance.
 */
fun createEventForTimes(
    organizationId: String,
    title: String = "Untitled",
    startHour: Int = 8,
    startMinute: Int = 0,
    endHour: Int = 12,
    endMinute: Int = 0,
): List<Event> {
  val baseDate = LocalDate.of(2025, 1, 1)

  val startTime = LocalTime.of(startHour, startMinute)
  val endTime = LocalTime.of(endHour, endMinute)
  val startInstant = DateTimeUtils.localDateTimeToInstant(baseDate, startTime)
  val endInstant = DateTimeUtils.localDateTimeToInstant(baseDate, endTime)

  return createEvent(
      organizationId = organizationId,
      title = title,
      startDate = startInstant,
      endDate = endInstant,
  )
}

@StringRes
fun RecurrenceStatus.labelRes(): Int =
    when (this) {
      RecurrenceStatus.OneTime -> R.string.recurrence_one_time
      RecurrenceStatus.Daily -> R.string.recurrence_daily
      RecurrenceStatus.Weekly -> R.string.recurrence_weekly
      RecurrenceStatus.Monthly -> R.string.recurrence_monthly
      RecurrenceStatus.Yearly -> R.string.recurrence_yearly
    }
