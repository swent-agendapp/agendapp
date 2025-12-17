package com.android.sample.model.network

/** Fake implementation of ConnectivityChecker for testing purposes. */
class FakeConnectivityChecker(private var state: Boolean = true) : ConnectivityChecker {
  private var callback: ((Boolean) -> Unit)? = null

  override fun hasInternet(): Boolean = state

  override fun registerCallback(callback: (Boolean) -> Unit) {
    this.callback = callback
  }

  /** Set the internet connectivity state and notify the callback. */
  fun setInternet(connected: Boolean) {
    state = connected
    callback?.invoke(state)
  }
}
