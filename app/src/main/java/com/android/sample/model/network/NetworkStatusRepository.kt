package com.android.sample.model.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Repository to monitor network connectivity status. */
class NetworkStatusRepository(checker: ConnectivityChecker) {

  // MutableStateFlow to hold the current connectivity status
  private val _isConnected = MutableStateFlow(checker.hasInternet())
  val isConnected: StateFlow<Boolean>
    get() = _isConnected

  init {
    //  Register callback to listen for connectivity changes
    checker.registerCallback { connected -> _isConnected.value = connected }
  }
}
