package com.android.sample.ui.calendar.eventOverview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.calendar.Event
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryProvider
import com.github.se.bootcamp.model.authentication.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OverviewUIState(
    val event: Event? = null,
    val participantsNames: List<String> = emptyList(),
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
class EventOverviewViewModel(
    // used to get Event
    private val eventRepository: EventRepository = EventRepositoryProvider.repository,
    // used to get the name of the participants (the event only contains user id, not name)
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel() {
  private val _uiState = MutableStateFlow(OverviewUIState())
  // Publicly exposed immutable UI state
  val uiState: StateFlow<OverviewUIState> = _uiState.asStateFlow()

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

  fun loadEvent(eventId: String) {
    viewModelScope.launch {
      try {
        val event =
            eventRepository.getEventById(eventId)
                ?: throw NoSuchElementException("Event with id=$eventId not found.")
        _uiState.value = OverviewUIState(event = event)
      } catch (e: Exception) {
        setErrorMsg("Failed to fetch event $eventId: ${e.message}")
        throw NoSuchElementException("Event with id=$eventId not found.")
      }
    }
  }

  fun loadParticipantNames(eventId: String) {
    viewModelScope.launch {
      loadEvent(eventId)

      // for now :
      val participantsNames = _uiState.value.event!!.participants.toList()

      // later (same comment as in EventOverviewScreen) :
      //            val participantsNames = _uiState.value.event!!.participants.mapNotNull { userId
      // ->
      //                // For each ID, resolve a readable display name (nulls are filtered out by
      // mapNotNull)
      //                try {
      //                    val user: User? = authRepository.getUserById(userId)
      //                    // Prefer the displayName, and if it is null or blank we return null to
      // allow filtering
      //                    user?.displayName?.takeIf { it.isNotBlank() }
      //                } catch (e: Exception) {
      //                    setErrorMsg("Failed to fetch user $userId: ${e.message}")
      //                    null
      //                }
      //            }
      _uiState.value =
          OverviewUIState(event = _uiState.value.event, participantsNames = participantsNames)
    }
  }
}
