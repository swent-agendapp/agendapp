package com.android.sample.ui.calendar.editEvent

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryProvider
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.utils.EventColor
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Assisted by AI
data class EditCalendarEventUIState(
    val eventId: String = "",
    val title: String = "",
    val description: String = "",
    val startInstant: Instant = Instant.now(),
    val endInstant: Instant = Instant.now().plus(Duration.ofHours(1)),
    val recurrenceMode: RecurrenceStatus = RecurrenceStatus.OneTime,
    val participants: Set<String> = emptySet(),
    val notifications: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val step: EditEventStep = EditEventStep.MAIN
)

/**
 * Enum representing the steps in the Edit Event flow.
 * - MAIN: The main event editing screen.
 * - ATTENDEES: The participants editing screen.
 */
enum class EditEventStep {
  MAIN,
  ATTENDEES
}

/**
 * ViewModel that manages the UI state for editing an existing calendar event.
 *
 * Responsibilities:
 * - Loading an existing event by ID.
 * - Managing edits to event fields (title, description, dates, recurrence, participants).
 * - Validating input fields.
 * - Saving changes back to the repository.
 * - Navigating between edit steps.
 */
class EditEventViewModel(
    private val repository: EventRepository = EventRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(EditCalendarEventUIState())
  val uiState: StateFlow<EditCalendarEventUIState> = _uiState.asStateFlow()

  fun loadEvent(eventId: String) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
      try {
        val event = repository.getEventById(eventId)
        if (event != null) {
          _uiState.value =
              _uiState.value.copy(
                  eventId = event.id,
                  title = event.title,
                  description = event.description,
                  startInstant = event.startDate,
                  endInstant = event.endDate,
                  recurrenceMode = event.recurrenceStatus,
                  participants = event.participants,
                  isLoading = false)
        } else {
          _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Event not found.")
        }
      } catch (e: Exception) {
        Log.e("EditEventViewModel", "Error loading event: ${e.message}")
        _uiState.value =
            _uiState.value.copy(
                isLoading = false, errorMessage = "Failed to load event: ${e.message}")
      }
    }
  }

  fun saveEditEventChanges() {
    viewModelScope.launch {
      val state = _uiState.value
      try {
        val updated =
            Event(
                id = state.eventId,
                title = state.title,
                description = state.description,
                startDate = state.startInstant,
                endDate = state.endInstant,
                cloudStorageStatuses = emptySet(),
                locallyStoredBy = emptyList(),
                personalNotes = null,
                participants = state.participants,
                version = System.currentTimeMillis(),
                recurrenceStatus = state.recurrenceMode,
                hasBeenDeleted = false,
                color = EventColor.Blue)
        repository.updateEvent(updated.id, updated)
      } catch (e: Exception) {
        Log.e("EditEventViewModel", "Error saving event changes: ${e.message}")
      }
    }
  }

  // --- field updates ---
  fun setTitle(value: String) {
    _uiState.value = _uiState.value.copy(title = value)
  }

  fun setDescription(value: String) {
    _uiState.value = _uiState.value.copy(description = value)
  }

  fun setStartInstant(instant: Instant) {
    _uiState.value = _uiState.value.copy(startInstant = instant)
  }

  fun setEndInstant(instant: Instant) {
    _uiState.value = _uiState.value.copy(endInstant = instant)
  }

  fun setRecurrenceMode(mode: RecurrenceStatus) {
    _uiState.value = _uiState.value.copy(recurrenceMode = mode)
  }

  fun addParticipant(name: String) {
    _uiState.value = _uiState.value.copy(participants = _uiState.value.participants + name)
  }

  fun removeParticipant(name: String) {
    _uiState.value = _uiState.value.copy(participants = _uiState.value.participants - name)
  }

  // --- validation ---
  private fun titleIsBlank() = _uiState.value.title.isBlank()

  private fun descriptionIsBlank() = _uiState.value.description.isBlank()

  private fun startAfterEnd() = _uiState.value.startInstant.isAfter(_uiState.value.endInstant)

  fun allFieldsValid() = !(titleIsBlank() || descriptionIsBlank() || startAfterEnd())

  // --- step navigation: ONLY via uiState.step ---
  fun goToAttendeesStep() {
    _uiState.value = _uiState.value.copy(step = EditEventStep.ATTENDEES)
  }

  fun goBackToMainStep() {
    _uiState.value = _uiState.value.copy(step = EditEventStep.MAIN)
  }

  fun resetUiState() {
    _uiState.value = _uiState.value.copy(step = EditEventStep.MAIN)
  }

  fun setEditStep(step: EditEventStep) {
    _uiState.value = _uiState.value.copy(step = step)
  }
}
