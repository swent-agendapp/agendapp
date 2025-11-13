package com.android.sample.model.replacement

/**
 * Provides a single instance of the replacement repository in the app. `repository` is mutable for
 * testing purposes.
 */
object ReplacementRepositoryProvider {

  private val _repository: ReplacementRepository by lazy {
    // Change this to switch between different implementations
    ReplacementRepositoryLocal()
    // ReplacementRepositoryFirebase()
  }

  var repository: ReplacementRepository = _repository
}
