package com.android.sample.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryProvider
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
 */
class CalendarViewModel(
    private val eventRepository: EventRepository = EventRepositoryProvider.repository
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
   * Loads all events from the repository and updates the UI state. Handles errors by updating the
   * errorMsg property.
   */
  fun loadAllEvents() {
    viewModelScope.launch {
      setLoading(true)
      try {
        val events = eventRepository.getAllEvents()
        setEvents(events = events)
        setLoading(false)
      } catch (e: Exception) {
        setErrorMsg("Failed to load all events: ${e.message}")
        setLoading(false)
      }
    }
  }

  /**
   * Loads events between the specified start and end dates from the repository and updates the UI
   * state. Handles errors by updating the errorMsg property.
   *
   * @param start The start date (inclusive).
   * @param end The end date (inclusive).
   */
  fun loadEventsBetween(start: Instant, end: Instant) {
    viewModelScope.launch {
      setLoading(true)
      try {
        val events = eventRepository.getEventsBetweenDates(startDate = start, endDate = end)
        setEvents(events = events)
        setLoading(false)
      } catch (e: Exception) {
        setErrorMsg("Failed to load events between $start and $end: ${e.message}")
        setLoading(false)
      }
    }
  }
}
