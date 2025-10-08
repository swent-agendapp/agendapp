package com.android.sample.model.calendar

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object EventRepositoryProvider {
  private val _repository: EventRepository by lazy {
    // Change this to switch between different implementations
    EventRepositoryLocal()
    // EventRepositoryFirestore()
  }

  var repository: EventRepository = _repository
}
