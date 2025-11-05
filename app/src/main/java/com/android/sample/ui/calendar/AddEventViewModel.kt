package com.android.sample.ui.calendar

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

class AddEventViewModel(
    private val repository: EventRepository = EventRepositoryProvider.repository,
    private val authz: AuthorizationService = AuthorizationService()
) : ViewModel() {
  private val _uiState = MutableStateFlow(AddCalendarEventUIState())
  val uiState: StateFlow<AddCalendarEventUIState> = _uiState.asStateFlow()

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

  fun titleIsBlank() = _uiState.value.title.isBlank()

  fun descriptionIsBlank() = _uiState.value.description.isBlank()

  fun startTimeIsAfterEndTime() = _uiState.value.startInstant.isAfter(_uiState.value.endInstant)

  fun allFieldsValid() = !(titleIsBlank() || descriptionIsBlank() || startTimeIsAfterEndTime())

  fun setRecurrenceMode(mode: RecurrenceStatus) {
    _uiState.value = _uiState.value.copy(recurrenceMode = mode)
  }

  fun setTitle(title: String) {
    _uiState.value = _uiState.value.copy(title = title)
  }

  fun setDescription(description: String) {
    _uiState.value = _uiState.value.copy(description = description)
  }

  fun setStartInstant(instant: Instant) {
    _uiState.value = _uiState.value.copy(startInstant = instant)
  }

  fun setEndInstant(instant: Instant) {
    _uiState.value = _uiState.value.copy(endInstant = instant)
  }

  fun setRecurrenceEndTime(recurrenceEndTime: Instant) {
    _uiState.value = _uiState.value.copy(recurrenceEndInstant = recurrenceEndTime)
  }

  fun addParticipant(participant: String) {
    val updatedParticipants = _uiState.value.participants.toMutableSet().apply { add(participant) }
    _uiState.value = _uiState.value.copy(participants = updatedParticipants)
  }

  fun removeParticipant(participant: String) {
    val updatedParticipants =
        _uiState.value.participants.toMutableSet().apply { remove(participant) }
    _uiState.value = _uiState.value.copy(participants = updatedParticipants)
  }

  fun resetUiState() {
    _uiState.value = AddCalendarEventUIState()
  }
}
