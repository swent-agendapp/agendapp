package com.android.sample.data.global.providers

import com.android.sample.data.global.repositories.EventRepository
import com.android.sample.data.hybrid.repositories.EventRepositoryHybrid

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object EventRepositoryProvider {
  private val _repository: EventRepository by lazy { EventRepositoryHybrid() }

  var repository: EventRepository = _repository
}
