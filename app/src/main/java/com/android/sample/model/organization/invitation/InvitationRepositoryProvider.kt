package com.android.sample.model.organization.invitation

/**
 * Provides a single instance of [InvitationRepository] in the app. `repository` is mutable for
 * testing purposes.
 */
object InvitationRepositoryProvider {
  private val _repository: InvitationRepository by lazy {
    // Change this to switch between different implementations
    InvitationRepositoryLocal()
  }

  var repository: InvitationRepository = _repository
}
