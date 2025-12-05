package com.android.sample.model.replacement

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Provides a single instance of the replacement repository in the app. `repository` is mutable for
 * testing purposes.
 */
object ReplacementRepositoryProvider {

  private val _repository: ReplacementRepository by lazy {
    // Change this to switch between different implementations
    // ReplacementRepositoryLocal()
    ReplacementRepositoryFirebase(Firebase.firestore)
  }

  var repository: ReplacementRepository = _repository
}
