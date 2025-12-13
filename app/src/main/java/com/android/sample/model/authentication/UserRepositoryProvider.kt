package com.android.sample.model.authentication

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object UserRepositoryProvider {
  private val _repository: UserRepository by lazy {
    // Change this to switch between different implementations
    UsersRepositoryFirebase(Firebase.firestore)
  }

  var repository: UserRepository = _repository
}
