package com.android.sample.model.organization.invitation

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Provides a single instance of [InvitationRepository] in the app. `repository` is mutable for
 * testing purposes.
 */
object InvitationRepositoryProvider {
  private val _repository: InvitationRepository by lazy {
    // Change this to switch between different implementations
    InvitationRepositoryFirebase(Firebase.firestore)
  }
  var repository: InvitationRepository = _repository
}
