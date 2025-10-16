package com.android.sample.ui.calendar

// import com.android.sample.ui.calendar.utils.DateTimeUtils TODO uncomment when DateTimeUtils is
// created
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryProvider
import com.android.sample.model.calendar.RecurrenceStatus
import com.android.sample.model.calendar.createEvent
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddCalendarEventUIState(
    val title: String = "",
    val description: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val startHour: Int = LocalTime.now().hour,
    val startMinute: Int = LocalTime.now().minute,
    val endHour: Int = LocalTime.now().hour + 1,
    val endMinute: Int = LocalTime.now().minute,
    val recurrenceEndTime: Instant = Instant.now(),
    val recurrenceMode: RecurrenceStatus = RecurrenceStatus.OneTime,
    val participants: Set<String> = emptySet(),
)

class AddEventViewModel(
    private val repository: EventRepository = EventRepositoryProvider.repository
) : ViewModel() {
  private val _uiState = MutableStateFlow(AddCalendarEventUIState())
  val uiState: StateFlow<AddCalendarEventUIState> = _uiState.asStateFlow()

  fun addEvent() {
    val currentState = _uiState.value
    val newEvent =
        createEvent(
            title = currentState.title,
            description = currentState.description,
            startDate =
                LocalDateTime.of(
                        _uiState.value.startDate,
                        LocalTime.of(_uiState.value.startHour, _uiState.value.startMinute))
                    .atZone(ZoneId.systemDefault())
                    .toInstant(),
            endDate =
                LocalDateTime.of(
                        _uiState.value.startDate,
                        LocalTime.of(_uiState.value.endHour, _uiState.value.endMinute))
                    .atZone(ZoneId.systemDefault())
                    .toInstant(),
            cloudStorageStatuses = emptySet(), // hardcoded for now
            personalNotes = "", // hardcoded for now
            participants = currentState.participants)
    addEventToRepository(newEvent)
  }

  fun addEventToRepository(event: Event) {
    viewModelScope.launch {
      try {
        repository.insertEvent(event)
      } catch (e: Exception) {
        Log.e("AddEventViewModel", "Error adding event: ${e.message}")
      }
    }
  }

  fun titleIsBlank() = _uiState.value.title.isBlank()

  fun descriptionIsBlank() = _uiState.value.description.isBlank()

  // TODO uncomment when DateTimeUtils is created
  //  fun startTimeIsAfterEndTime() =
  //      DateTimeUtils.localDateTimeToInstant(
  //              _uiState.value.startDate,
  //              LocalTime.of(_uiState.value.startHour, _uiState.value.startMinute))
  //          .isAfter(
  //              DateTimeUtils.localDateTimeToInstant(
  //                  _uiState.value.startDate,
  //                  LocalTime.of(_uiState.value.endHour, _uiState.value.endMinute)))

  fun allFieldsValid() =
      !(titleIsBlank() ||
          descriptionIsBlank()) // || startTimeIsAfterEndTime()) TODO uncomment when DateTimeUtils
  // is created

  fun setRecurrenceMode(mode: RecurrenceStatus) {
    _uiState.value = _uiState.value.copy(recurrenceMode = mode)
  }

  fun setTitle(title: String) {
    _uiState.value = _uiState.value.copy(title = title)
  }

  fun setDescription(description: String) {
    _uiState.value = _uiState.value.copy(description = description)
  }

  fun setDate(date: LocalDate) {
    _uiState.value = _uiState.value.copy(startDate = date)
  }

  fun setStartHour(hour: Int) {
    _uiState.value = _uiState.value.copy(startHour = hour)
  }

  fun setStartMinute(minute: Int) {
    _uiState.value = _uiState.value.copy(startMinute = minute)
  }

  fun setEndHour(hour: Int) {
    _uiState.value = _uiState.value.copy(endHour = hour)
  }

  fun setEndMinute(minute: Int) {
    _uiState.value = _uiState.value.copy(endMinute = minute)
  }

  fun setRecurrenceEndTime(recurrenceEndTime: Instant) {
    _uiState.value = _uiState.value.copy(recurrenceEndTime = recurrenceEndTime)
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
}
