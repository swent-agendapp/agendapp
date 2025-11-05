package com.android.sample.ui.calendar.editEvent

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant

// This ViewModel is currently a placeholder generated with AI assistance.
// The implementation has not yet been started, but this structure ensures that
// the corresponding View components compile and run without errors until the
// actual ViewModel logic is developed.

class EditEventViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(EditEventUiState())
  val uiState: StateFlow<EditEventUiState> = _uiState

  fun addParticipant(name: String) {
    // Temporary placeholder to avoid unused parameter warning, will be implemented later
    println("Adding participant: $name")
  }

  fun removeParticipant(name: String) {
    // Temporary placeholder to avoid unused parameter warning, will be implemented later
    println("Removing participant: $name")
  }
}

data class EditEventUiState(
    val title: String = "",
    val description: String = "",
    val participants: List<String> = emptyList(),
    val startInstant: Instant? = null,
    val endInstant: Instant? = null,
    // Additional fields can be added as needed
)
