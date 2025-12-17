package com.android.sample.model.network

import android.content.Context

/** Provides a single instance of the NetworkStatusRepository in the app. */
object NetworkStatusRepositoryProvider {
  private lateinit var _repository: NetworkStatusRepository
  val repository: NetworkStatusRepository
    get() = _repository

  fun init(context: Context) {
    _repository = NetworkStatusRepository(AndroidConnectivityChecker(context))
  }
}
