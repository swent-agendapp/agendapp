package com.android.sample.ui.calendar.addEvent

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authorization.AuthorizationService
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryProvider
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.calendar.createEvent
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Assisted by AI

/**
 * UI state representing the current draft of an event being created.
 *
 * This data class is used by [AddEventViewModel] and observed by the composables to render the Add
 * Event multi-step flow. All fields have sensible defaults so a fresh state represents a new event
 * draft.
 *
 * @property title User-entered event title.
 * @property description User-entered event description.
 * @property startInstant The start instant (date + time) of the event. Defaults to now.
 * @property endInstant The end instant (date + time) of the event. Defaults to one hour after now.
 * @property recurrenceEndInstant The end instant for a recurrence rule (if any).
 * @property recurrenceMode The recurrence mode for the event (OneTime, Weekly, ...).
 * @property participants The set of selected participant identifiers (currently `String` names).
 * @property errorMsg Optional error message surfaced to the UI (e.g., permission errors).
 */
data class AddCalendarEventUIState(
    val title: String = "",
    val description: String = "",
    val startInstant: Instant = Instant.now(),
    val endInstant: Instant = Instant.now().plus(Duration.ofHours(1)),
    val recurrenceEndInstant: Instant = Instant.now(),
    val recurrenceMode: RecurrenceStatus = RecurrenceStatus.OneTime,
    val participants: Set<String> = emptySet(),
    val errorMsg: String? = null,
)

/**
 * ViewModel that manages the UI state for the Add Event flow.
 *
 * Responsibilities:
 * - Holds all event draft data (`AddCalendarEventUIState`)
 * - Exposes validation helpers (e.g., `titleIsBlank()`, `allFieldsValid()`)
 * - Builds the domain `Event` object when `addEvent()` is called
 * - Persists the event through `EventRepository`
 *
 * UI Components only interact with the ViewModel through its exposed methods:
 * - setTitle(), setDescription(), setStartInstant(), setEndInstant(), ...
 * - addParticipant() / removeParticipant()
 *
 * State updates are exposed as StateFlow and automatically recomposed in UI.
 */
class AddEventViewModel(
    private val repository: EventRepository = EventRepositoryProvider.repository,
    private val authz: AuthorizationService = AuthorizationService()
) : ViewModel() {
  private val _uiState = MutableStateFlow(AddCalendarEventUIState())

  /** Public immutable state that the UI observes. */
  val uiState: StateFlow<AddCalendarEventUIState> = _uiState.asStateFlow()

  /**
   * Builds a new event from the current UI state and delegates storage. Calls
   * `addEventToRepository()` to persist the event.
   */
  fun addEvent() {
    val currentState = _uiState.value
    val newEvent =
        createEvent(
            title = currentState.title,
            description = currentState.description,
            startDate = currentState.startInstant,
            endDate = currentState.endInstant,
            cloudStorageStatuses = emptySet(), // hardcoded for now
            personalNotes = "", // hardcoded for now
            participants = currentState.participants)
    addEventToRepository(newEvent)
  }

  /**
   * Inserts the event into the repository after checking authorization.
   *
   * If the user is not authorized, the state is updated with an error message instead of throwing
   * an exception.
   *
   * Any unexpected error is caught and surfaced to the UI through `errorMsg`.
   *
   * @param event The event to persist.
   */
  fun addEventToRepository(event: Event) {
    viewModelScope.launch {
      try {
        val allowed = runCatching { authz.requireAdmin() }.isSuccess
        if (!allowed) {
          _uiState.value = _uiState.value.copy(errorMsg = "You are not allowed to create events")
          return@launch
        }

        repository.insertEvent(event)
      } catch (e: Exception) {
        Log.e("AddEventViewModel", "Error adding event: ${e.message}")
        _uiState.value = _uiState.value.copy(errorMsg = "Unexpected error while creating the event")
      }
    }
  }
  /** @return `true` if the title field is empty or blank. */
  fun titleIsBlank() = _uiState.value.title.isBlank()
  /** @return `true` if the description field is empty or blank. */
  fun descriptionIsBlank() = _uiState.value.description.isBlank()
  /** @return `true` if start time is strictly after end time (invalid state). */
  fun startTimeIsAfterEndTime() = _uiState.value.startInstant.isAfter(_uiState.value.endInstant)
  /**
   * @return `true` only when all event fields are valid.
   *
   * Validation rules:
   * - Title must not be blank
   * - Description must not be blank
   * - Start time must be before or equal to end time
   */
  fun allFieldsValid() = !(titleIsBlank() || descriptionIsBlank() || startTimeIsAfterEndTime())
  /** Updates the recurrence mode for the event. */
  fun setRecurrenceMode(mode: RecurrenceStatus) {
    _uiState.value = _uiState.value.copy(recurrenceMode = mode)
  }
  /** Sets or updates the event title. */
  fun setTitle(title: String) {
    _uiState.value = _uiState.value.copy(title = title)
  }
  /** Sets or updates the event description. */
  fun setDescription(description: String) {
    _uiState.value = _uiState.value.copy(description = description)
  }
  /** Updates the start date/time of the event. */
  fun setStartInstant(instant: Instant) {
    _uiState.value = _uiState.value.copy(startInstant = instant)
  }
  /** Updates the end date/time of the event. */
  fun setEndInstant(instant: Instant) {
    _uiState.value = _uiState.value.copy(endInstant = instant)
  }
  /** Sets the end date for the recurrence rule. Ignored when recurrence mode is OneTime. */
  fun setRecurrenceEndTime(recurrenceEndTime: Instant) {
    _uiState.value = _uiState.value.copy(recurrenceEndInstant = recurrenceEndTime)
  }
  /**
   * Adds a participant to the event.
   *
   * If the participant already exists in the set, it is ignored.
   *
   * @param participant Unique participant identifier (currently String username).
   */
  fun addParticipant(participant: String) {
    val updatedParticipants = _uiState.value.participants.toMutableSet().apply { add(participant) }
    _uiState.value = _uiState.value.copy(participants = updatedParticipants)
  }
  /**
   * Removes a participant from the event.
   *
   * @param participant The participant to remove.
   */
  fun removeParticipant(participant: String) {
    val updatedParticipants =
        _uiState.value.participants.toMutableSet().apply { remove(participant) }
    _uiState.value = _uiState.value.copy(participants = updatedParticipants)
  }
  /**
   * Resets all fields to the default event draft.
   *
   * Useful when:
   * - A new event creation starts
   * - User cancels the flow
   */
  fun resetUiState() {
    _uiState.value = AddCalendarEventUIState()
  }
}
