package com.android.sample.model.authentication

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object EmployeeRepositoryProvider {
  private val _repository: EmployeeRepository by lazy {
    // Change this to switch between different implementations
    UsersRepositoryFirebase(Firebase.firestore, AuthRepositoryProvider.repository)
  }

  var repository: EmployeeRepository = _repository
}
