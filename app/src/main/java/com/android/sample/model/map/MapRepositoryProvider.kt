package com.android.sample.model.map

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object MapRepositoryProvider {
  private val _repository: MapRepository by lazy {
    // Change this to switch between different implementations
    MapRepositoryFirebase(Firebase.firestore)
    // EventRepositoryFirestore()
  }

  var repository: MapRepository = _repository
}
