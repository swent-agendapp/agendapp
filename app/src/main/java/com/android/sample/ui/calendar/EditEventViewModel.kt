package com.android.sample.ui.calendar

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

/** UI state representing the editable fields for an existing calendar event. */
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
)

/** ViewModel responsible for managing the Edit Event screen logic. */
class EditEventViewModel(
    private val repository: EventRepository = EventRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(EditCalendarEventUIState())
  val uiState: StateFlow<EditCalendarEventUIState> = _uiState.asStateFlow()

  /** Loads an existing event by ID from the repository. */
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
                  // notifications = event.notifications,
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

  /** Saves changes made to the event (updates the existing record). */
  fun saveChanges() {
    viewModelScope.launch {
      val state = _uiState.value
      try {
        val updatedEvent =
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
                color = EventColor.Blue
                // notifications = state.notifications
                )
        repository.updateEvent(updatedEvent.id, updatedEvent)
      } catch (e: Exception) {
        Log.e("EditEventViewModel", "Error saving event changes: ${e.message}")
      }
    }
  }

  /** Updates title field */
  fun setTitle(title: String) {
    _uiState.value = _uiState.value.copy(title = title)
  }

  /** Updates description field */
  fun setDescription(description: String) {
    _uiState.value = _uiState.value.copy(description = description)
  }

  /** Updates start time */
  fun setStartInstant(instant: Instant) {
    _uiState.value = _uiState.value.copy(startInstant = instant)
  }

  /** Updates end time */
  fun setEndInstant(instant: Instant) {
    _uiState.value = _uiState.value.copy(endInstant = instant)
  }

  /** Updates recurrence mode */
  fun setRecurrenceMode(mode: RecurrenceStatus) {
    _uiState.value = _uiState.value.copy(recurrenceMode = mode)
  }

  /** Adds a notification */
  fun addNotification(notification: String) {
    val updated = _uiState.value.notifications + notification
    _uiState.value = _uiState.value.copy(notifications = updated)
  }

  /** Removes a notification */
  fun removeNotification(notification: String) {
    val updated = _uiState.value.notifications - notification
    _uiState.value = _uiState.value.copy(notifications = updated)
  }

  /** Adds a participant */
  fun addParticipant(name: String) {
    val updated = _uiState.value.participants + name
    _uiState.value = _uiState.value.copy(participants = updated)
  }

  /** Removes a participant */
  fun removeParticipant(name: String) {
    val updated = _uiState.value.participants - name
    _uiState.value = _uiState.value.copy(participants = updated)
  }

  /** Simple field validation */
  private fun titleIsBlank() = _uiState.value.title.isBlank()

  private fun descriptionIsBlank() = _uiState.value.description.isBlank()

  private fun startTimeIsAfterEndTime() =
      _uiState.value.startInstant.isAfter(_uiState.value.endInstant)

  fun allFieldsValid() = !(titleIsBlank() || descriptionIsBlank() || startTimeIsAfterEndTime())
}
