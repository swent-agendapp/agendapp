package com.android.sample.model.authentication

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object UserRepositoryProvider {
  private val firestore: FirebaseFirestore by lazy {
    Firebase.firestore.apply { useEmulator("10.0.2.2", 8080) }
  }

  private val _repository: UserRepository by lazy {
    UsersRepositoryFirebase(firestore, AuthRepositoryProvider.repository)
  }

  var repository: UserRepository = _repository
}
