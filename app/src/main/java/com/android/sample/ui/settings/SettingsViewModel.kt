package com.android.sample.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.network.NetworkStatusRepository
import com.android.sample.model.network.NetworkStatusRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** UI State for the Settings screen. */
data class SettingsUiState(val networkAvailable: Boolean = true)

/** ViewModel for managing settings data */
open class SettingsViewModel(
    private val networkStatusRepository: NetworkStatusRepository =
        NetworkStatusRepositoryProvider.repository
) : ViewModel() {
  val _uiState = MutableStateFlow(SettingsUiState())
  val uiState: StateFlow<SettingsUiState> = _uiState

  init {
    observeNetworkStatus()
  }

  // Observe network status changes
  fun observeNetworkStatus() {
    viewModelScope.launch {
      networkStatusRepository.isConnected.collect { connected ->
        _uiState.update { it.copy(networkAvailable = connected) }
      }
    }
  }
}
