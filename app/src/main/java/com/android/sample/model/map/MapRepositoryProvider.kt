package com.android.sample.model.map

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object MapRepositoryProvider {
  private val _repository: MapRepository by lazy {
    // Change this to switch between different implementations
    MapRepositoryLocal()
  }

  var repository: MapRepository = _repository
}
