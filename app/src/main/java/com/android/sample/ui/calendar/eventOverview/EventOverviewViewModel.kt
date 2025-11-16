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
    // used to get Events
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

  /**
   * Loads the event corresponding to the given [eventId] from the [EventRepository].
   *
   * Updates the UI state with the retrieved event while preserving previously loaded participant
   * names. If the event cannot be found or fetched, an error message is set and a
   * [NoSuchElementException] is thrown.
   *
   * @param eventId The unique identifier of the event to load.
   */
  fun loadEvent(eventId: String) {
    setLoading(true)
    viewModelScope.launch {
      try {
        val event =
            eventRepository.getEventById(eventId)
                ?: throw NoSuchElementException("Event with id=$eventId not found.")
        _uiState.value =
            OverviewUIState(event = event, participantsNames = _uiState.value.participantsNames)
      } catch (e: Exception) {
        setErrorMsg("Failed to fetch event $eventId: ${e.message}")
        throw NoSuchElementException("Event with id=$eventId not found.")
      } finally {
        setLoading(false)
      }
    }
  }

  /**
   * Loads the participant display names for the event currently stored in the UI state and updates
   * the UI accordingly.
   *
   * For the current implementation, participant “names” are taken directly from the event’s
   * participant ids, so that the overview screen can display something meaningful even though the
   * authentication repository does not yet contain corresponding users.
   *
   * If no event is present in the UI state, this function returns without making any changes. In a
   * future iteration, this method is expected to resolve human-readable names via the
   * [AuthRepository].
   */
  fun loadParticipantNames() {
    if (_uiState.value.event == null) return
    viewModelScope.launch {
      // Later (when the Add flow will propose a list of User that are in the Auth repository
      // instead of a fake name's list) :

      //          val participantsNames = _uiState.value.event!!.participants.mapNotNull { userId ->
      //              // For each ID, resolve a readable display name (nulls are filtered out by
      // mapNotNull)
      //              try {
      //                  val user: User? = authRepository.getUserById(userId)
      //                  // Prefer the displayName, and if it is null or blank we return null to
      // allow filtering
      //                  user?.displayName?.takeIf { it.isNotBlank() }
      //              } catch (e: Exception) {
      //                  setErrorMsg("Failed to fetch user $userId: ${e.message}")
      //                  null
      //              }
      //          }

      // Note : we can't use it now because the AddViewModel add user ID like "Alice", "Bob" but no
      // User with these ids exist in the Auth repo
      // => the “authRepository.getUserById(userId)“ doesn't find any user with an "Alice" id, and
      // return an empty
      // list

      // To still see something coherent with what we "add", we update it like so :
      val participantsNames = _uiState.value.event!!.participants.toList()

      // update the uiState with the new participants list
      _uiState.value =
          OverviewUIState(event = _uiState.value.event, participantsNames = participantsNames)
    }
  }
}
