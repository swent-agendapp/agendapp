package com.android.sample.ui.calendar

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryProvider
import com.android.sample.model.map.LocationRepository
import com.android.sample.model.map.LocationRepositoryAndroid
import com.android.sample.model.map.MapRepository
import com.android.sample.model.map.MapRepositoryProvider
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import com.android.sample.ui.organization.SelectedOrganizationViewModel
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class LocationStatus {
  NO_PERMISSION, // Grey - user hasn't accepted location
  OUTSIDE_AREA, // Red - user is not inside any area
  INSIDE_AREA // Green - user is inside an area
}

/**
 * Represents the UI state for the Calendar screen.
 *
 * @property events A list of Event objects to be displayed in the Calendar screen.
 * @property errorMsg An error message to be shown when fetching events fails. Null if no error is
 *   present.
 * @property isLoading Indicates if the events are currently being loaded.
 * @property locationStatus The current location status of the user relative to defined areas.
 */
data class CalendarUIState(
    val events: List<Event> = emptyList(),
    val errorMsg: String? = null,
    val isLoading: Boolean = false,
    val locationStatus: LocationStatus = LocationStatus.NO_PERMISSION
)

/**
 * ViewModel for the Calendar screen.
 *
 * Responsible for managing the UI state, by fetching and providing Event items via the
 * [EventRepository].
 *
 * @property app Application context needed for location services.
 * @property eventRepository The repository used to fetch and manage Event items.
 * @property locationRepository The repository used to check user location.
 * @property mapRepository The repository used to fetch areas.
 * @property selectedOrganizationViewModel ViewModel that provides the currently selected
 *   organization.
 */
class CalendarViewModel(
    app: Application,
    // used to get Events
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    private val locationRepository: LocationRepository = LocationRepositoryAndroid(app),
    private val mapRepository: MapRepository = MapRepositoryProvider.repository,
    selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel
) : ViewModel() {
  private val _uiState = MutableStateFlow(CalendarUIState())
  // Publicly exposed immutable UI state
  val uiState: StateFlow<CalendarUIState> = _uiState.asStateFlow()

  private val selectedOrganizationId: StateFlow<String?> =
      selectedOrganizationViewModel.selectedOrganizationId

  init {
    checkUserLocationStatus()
  }

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
   * Checks if the user's current location is inside any of the defined areas and updates the
   * location status.
   *
   * Fetches all areas from the map repository and uses the location repository to check if the user
   * is inside any of them. Updates the locationStatus property in the UI state based on:
   * - NO_PERMISSION if location permission is not granted
   * - INSIDE_AREA if user is inside at least one area
   * - OUTSIDE_AREA if user has permission but is not inside any area
   */
  fun checkUserLocationStatus() {
    viewModelScope.launch {
      val orgId = selectedOrganizationId.value
      if (orgId == null)
          throw IllegalArgumentException("You must join an Organisation for this feature")
      try {
        val areas = mapRepository.getAllAreas(orgId = orgId)
        val isInside = locationRepository.isUserLocationInAreas(areas)
        _uiState.value =
            _uiState.value.copy(
                locationStatus =
                    if (isInside) LocationStatus.INSIDE_AREA else LocationStatus.OUTSIDE_AREA)
      } catch (e: SecurityException) {
        // No permission granted
        _uiState.value = _uiState.value.copy(locationStatus = LocationStatus.NO_PERMISSION)
      } catch (e: Exception) {
        // Other errors - keep current status or set to NO_PERMISSION
        _uiState.value = _uiState.value.copy(locationStatus = LocationStatus.NO_PERMISSION)
      }
    }
  }

  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer {
        val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
        CalendarViewModel(app = app)
      }
    }
  }
}
