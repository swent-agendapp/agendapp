package com.android.sample.ui.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.authentication.User
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryProvider
import com.github.se.bootcamp.model.authentication.AuthRepository
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the UI state for the Calendar screen.
 *
 * @property events A list of Event objects to be displayed in the Calendar screen.
 * @property errorMsg An error message to be shown when fetching events fails. Null if no error is
 *   present.
 * @property isLoading Indicates if the events are currently being loaded.
 */
data class CalendarUIState(
    val events: List<Event> = emptyList(),
    val errorMsg: String? = null,
    val isLoading: Boolean = false
)

/**
 * ViewModel for the Calendar screen.
 *
 * Responsible for managing the UI state, by fetching and providing Event items via the
 * [EventRepository].
 *
 * @property eventRepository The repository used to fetch and manage Event items.
 * @property authRepository The repository used to fetch user data, here the participant names.
 */
class CalendarViewModel(
    // used to get Events
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    // used to get the name of the participants (the event only contains user id, not name)
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel() {
  private val _uiState = MutableStateFlow(CalendarUIState())
  // Publicly exposed immutable UI state
  val uiState: StateFlow<CalendarUIState> = _uiState.asStateFlow()

  /** Sets an error message in the UI state. */
  private fun setErrorMsg(errorMsg: String) {
    _uiState.value = _uiState.value.copy(errorMsg = errorMsg)
  }

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  /** Sets the loading state in the UI state. */
  private fun setLoading(isLoading: Boolean) {
    _uiState.value = _uiState.value.copy(isLoading = isLoading)
  }

  /** Updates the list of events in the UI state. */
  private fun setEvents(events: List<Event>) {
    _uiState.value = _uiState.value.copy(events = events)
  }

  /**
   * Helper function to load events using a provided suspend lambda.
   *
   * This function launches a coroutine in the ViewModel scope, sets the loading state to true,
   * executes the provided suspend block to fetch events, and updates the UI state accordingly. If
   * an exception occurs, it sets an error message in the UI state. The loading state is set back to
   * false when the operation completes, regardless of success or failure.
   *
   * @param loadEventsBlock Suspend lambda that fetches a list of events.
   * @param errorMessage Error message to display if the operation fails.
   */
  private fun loadEventsHelper(loadEventsBlock: suspend () -> List<Event>, errorMessage: String) {
    viewModelScope.launch {
      setLoading(true)
      try {
        val events = loadEventsBlock()
        setEvents(events)
      } catch (e: Exception) {
        setErrorMsg("$errorMessage: ${e.message}")
      } finally {
        setLoading(false)
      }
    }
  }

  /**
   * Loads all events from the repository and updates the UI state. Handles errors by updating the
   * errorMsg property.
   */
  fun loadAllEvents() {
    loadEventsHelper(
        loadEventsBlock = { eventRepository.getAllEvents() },
        errorMessage = "Failed to load all events")
  }

  /**
   * Loads events between the specified start and end dates from the repository and updates the UI
   * state. Handles errors by updating the errorMsg property.
   *
   * @param start The start date (inclusive).
   * @param end The end date (inclusive).
   */
  fun loadEventsBetween(start: Instant, end: Instant) {
    loadEventsHelper(
        loadEventsBlock = { eventRepository.getEventsBetweenDates(start, end) },
        errorMessage = "Failed to load events between $start and $end")
  }

  /**
   * Retrieves the [Event] corresponding to the given [eventId].
   *
   * This method performs a lookup in the [EventRepository] and returns the matching event.
   * If no event is found or if an error occurs during the fetch, an error message is
   * propagated to the UI layer and a [NoSuchElementException] is thrown.
   *
   * @throws NoSuchElementException if no event with the given [eventId] exists.
   */
  suspend fun getEventById(eventId: String): Event {
    return try {
      eventRepository.getEventById(eventId)
        ?: throw NoSuchElementException("Event with id=$eventId not found.")
    } catch (e: Exception) {
      setErrorMsg("Failed to fetch event $eventId: ${e.message}")
      throw NoSuchElementException("Event with id=$eventId not found.")
    }
  }

  /**
   * Returns the display names of all participants for the event identified by [eventId].
   *
   * The function resolves the event via [getEventById], then maps each participant ID to a display
   * name using [resolveDisplayNameForUser]. Unknown users or users without a display name are
   * skipped.
   */
  suspend fun getParticipantNames(eventId: String): List<String> {
    val event = getEventById(eventId)
    // If the list is empty, this will simply return an empty list
    return event.participants.mapNotNull { userId ->
      Log.d("", "userId = ${userId} --- user Name = ${resolveDisplayNameForUser(userId)}")
      // For each ID, resolve a readable display name (nulls are filtered out by mapNotNull)
      resolveDisplayNameForUser(userId)
    }
  }

  /**
   * Resolves a user's display name from their [userId] using [AuthRepository.getUserById].
   *
   * If the user cannot be found or has no display name, the function returns `null`.
   *
   * Errors are caught and surfaced to the UI via [setErrorMsg] and return `null`.
   */
  suspend fun resolveDisplayNameForUser(userId: String): String? {
    return try {
      val user: User? = authRepository.getUserById(userId)
      // Prefer the displayName, and if it is null or blank we return null to allow filtering
      user?.displayName?.takeIf { it.isNotBlank() }
    } catch (e: Exception) {
      setErrorMsg("Failed to fetch user $userId: ${e.message}")
      null
    }
  }
}
