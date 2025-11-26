package com.android.sample.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryProvider
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import com.android.sample.ui.organization.SelectedOrganizationViewModel
import java.time.Duration
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
    val isLoading: Boolean = false,
    val workedHours: List<Pair<String, Double>> = emptyList()
)

/**
 * ViewModel for the Calendar screen.
 *
 * Responsible for managing the UI state, by fetching and providing Event items via the
 * [EventRepository].
 *
 * @property eventRepository The repository used to fetch and manage Event items.
 * @property selectedOrganizationViewModel ViewModel that provides the currently selected
 *   organization.
 */
class CalendarViewModel(
    // used to get Events
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel
) : ViewModel() {
  private val _uiState = MutableStateFlow(CalendarUIState())
  // Publicly exposed immutable UI state
  val uiState: StateFlow<CalendarUIState> = _uiState.asStateFlow()

  private val selectedOrganizationId: StateFlow<String?> =
      selectedOrganizationViewModel.selectedOrganizationId

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
    val orgId = selectedOrganizationId.value ?: return

    loadEventsHelper(
        loadEventsBlock = { eventRepository.getAllEvents(orgId = orgId) },
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
    val orgId = selectedOrganizationId.value
    if (orgId == null) return

    loadEventsHelper(
        loadEventsBlock = {
          eventRepository.getEventsBetweenDates(orgId = orgId, startDate = start, endDate = end)
        },
        errorMessage = "Failed to load events between $start and $end")
  }

  /**
   * Calculates the total worked hours for each employee within a given time range.
   *
   * @param start The start of the time range.
   * @param end The end of the time range.
   */
  fun calculateWorkedHours(start: Instant, end: Instant) {
    val orgId = selectedOrganizationId.value ?: return
    viewModelScope.launch {
      try {
        val events =
            eventRepository.getEventsBetweenDates(orgId = orgId, startDate = start, endDate = end)
        val workedHoursMap = mutableMapOf<String, Double>()
        val now = Instant.now()

        events.forEach { event ->
          val durationHours = Duration.between(event.startDate, event.endDate).toMinutes() / 60.0

          if (event.endDate.isBefore(now)) {
            // Past event: check presence
            event.presence.forEach { (userId, isPresent) ->
              if (isPresent) {
                workedHoursMap[userId] = workedHoursMap.getOrDefault(userId, 0.0) + durationHours
              }
            }
          } else {
            // Future event: assume participation
            event.participants.forEach { userId ->
              workedHoursMap[userId] = workedHoursMap.getOrDefault(userId, 0.0) + durationHours
            }
          }
        }
        _uiState.value = _uiState.value.copy(workedHours = workedHoursMap.toList())
      } catch (e: Exception) {
        setErrorMsg("Failed to calculate worked hours: ${e.message}")
      }
    }
  }
}
