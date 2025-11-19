package com.android.sample.model.versioning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.AuthRepositoryProvider
import com.android.sample.model.organization.Organization
import com.android.sample.model.organization.OrganizationRepository
import com.github.se.bootcamp.model.authentication.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Represents the synchronization state exposed to the UI (or callers). */
data class VersionSyncState(val isSyncing: Boolean = false, val lastError: String? = null)

/**
 * ViewModel responsible for synchronizing versioned entities between local and remote sources.
 *
 * Currently it synchronizes [Organization] data but can be extended to other entities that expose a
 * `version` field.
 */
class VersionSyncViewModel(
    private val localOrganizationRepository: OrganizationRepository,
    private val remoteOrganizationRepository: OrganizationRepository,
    private val authRepository: AuthRepository = AuthRepositoryProvider.repository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

  private val _state = MutableStateFlow(VersionSyncState())
  val state: StateFlow<VersionSyncState> = _state.asStateFlow()

  /** Pulls remote data and updates local repositories when newer versions exist. */
  fun pull() {
    viewModelScope.launch(dispatcher) { runSync { pullOrganizations() } }
  }

  /** Pushes local changes to the remote repository, updating the version before sending. */
  fun push(organization: Organization) {
    viewModelScope.launch(dispatcher) { runSync { pushOrganization(organization) } }
  }

  private suspend fun pullOrganizations() {
    val user =
        authRepository.getCurrentUser()
            ?: throw IllegalStateException("No authenticated user available for sync")

    val remoteOrganizations = remoteOrganizationRepository.getAllOrganizations(user)
    val localOrganizations =
        localOrganizationRepository.getAllOrganizations(user).associateBy { it.id }

    remoteOrganizations.forEach { remoteOrganization ->
      val localOrganization = localOrganizations[remoteOrganization.id]
      when {
        localOrganization == null -> localOrganizationRepository.insertOrganization(remoteOrganization, user)
        remoteOrganization.version > localOrganization.version ->
            localOrganizationRepository.updateOrganization(
                remoteOrganization.id, remoteOrganization, user)
      }
    }
  }

  private suspend fun pushOrganization(organization: Organization) {
    val user =
        authRepository.getCurrentUser()
            ?: throw IllegalStateException("No authenticated user available for sync")

    val updatedOrganization = organization.withUpdatedVersion()
    val local = localOrganizationRepository.getOrganizationById(updatedOrganization.id, user)
    if (local == null) {
      localOrganizationRepository.insertOrganization(updatedOrganization, user)
    } else {
      localOrganizationRepository.updateOrganization(updatedOrganization.id, updatedOrganization, user)
    }

    val remote = remoteOrganizationRepository.getOrganizationById(updatedOrganization.id, user)
    if (remote == null) {
      remoteOrganizationRepository.insertOrganization(updatedOrganization, user)
    } else {
      remoteOrganizationRepository.updateOrganization(updatedOrganization.id, updatedOrganization, user)
    }
  }

  private suspend fun runSync(block: suspend () -> Unit) {
    _state.value = VersionSyncState(isSyncing = true, lastError = null)
    try {
      block()
      _state.value = VersionSyncState(isSyncing = false, lastError = null)
    } catch (e: Exception) {
      _state.value =
          VersionSyncState(
              isSyncing = false, lastError = e.localizedMessage ?: "Synchronization error")
    }
  }
}
