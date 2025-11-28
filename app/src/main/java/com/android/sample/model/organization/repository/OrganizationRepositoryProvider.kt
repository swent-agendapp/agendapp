package com.android.sample.model.organization.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object OrganizationRepositoryProvider {
  private val _repository: OrganizationRepository by lazy {
    // Change this to switch between different implementations
    // OrganizationRepositoryLocal()
    OrganizationRepositoryFirebase(Firebase.firestore)
  }

  var repository: OrganizationRepository = _repository
}
