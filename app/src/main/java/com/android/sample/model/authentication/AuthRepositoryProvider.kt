package com.android.sample.model.authentication

object AuthRepositoryProvider {
  private val _repository: AuthRepository by lazy {
    // Change this to switch between different implementations
    AuthRepositoryFirebase()
  }
  var repository: AuthRepository = _repository
}
