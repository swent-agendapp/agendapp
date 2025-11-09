package com.android.sample.model.authentication

import com.github.se.bootcamp.model.authentication.AuthRepository
import com.github.se.bootcamp.model.authentication.AuthRepositoryFirebase

object AuthRepositoryProvider {
  private val _repository: AuthRepository by lazy {
    // Change this to switch between different implementations
    AuthRepositoryFirebase()
  }
  var repository: AuthRepository = _repository
}
