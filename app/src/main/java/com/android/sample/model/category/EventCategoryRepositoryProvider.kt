package com.android.sample.model.category

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object EventCategoryRepositoryProvider {
  private val _repository: EventCategoryRepository by lazy {
    // Change this to switch between different implementations
    // EventCategoryRepositoryLocal()
    EventCategoryRepositoryFirebase(Firebase.firestore)
  }

  var repository: EventCategoryRepository = _repository
}
