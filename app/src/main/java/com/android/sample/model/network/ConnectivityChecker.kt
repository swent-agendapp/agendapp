package com.android.sample.model.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

/**
 * Interface to check network connectivity status.
 *
 * @function hasInternet Checks if there is an active internet connection.
 * @function registerCallback Registers a callback to listen for connectivity changes. ((Boolean) ->
 *   Unit parameter indicates whether there is internet connectivity)
 */
interface ConnectivityChecker {
  fun hasInternet(): Boolean

  fun registerCallback(callback: (Boolean) -> Unit)
}

/** Real implementation of ConnectivityChecker using Android's ConnectivityManager. */
class AndroidConnectivityChecker(context: Context) : ConnectivityChecker {

  // Get the ConnectivityManager from the system services
  private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  override fun hasInternet(): Boolean {
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }

  override fun registerCallback(callback: (Boolean) -> Unit) {
    val request =
        NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
    cm.registerNetworkCallback(
        request,
        object : ConnectivityManager.NetworkCallback() {
          override fun onAvailable(network: Network) = callback(true)

          override fun onLost(network: Network) = callback(hasInternet())
        })
  }
}
