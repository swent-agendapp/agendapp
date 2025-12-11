package com.android.sample.model.metadata

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Provides a single global instance of EventMetadataRepository. Allows swapping implementations
 * during tests.
 */
object EventMetadataRepositoryProvider {

  private val defaultRepository: EventMetadataRepository by lazy {
    EventMetadataRepositoryFirebase(Firebase.firestore)
  }

  /** The repository instance used by the app. Tests may override this with a fake repository. */
  var repository: EventMetadataRepository = defaultRepository
}
