package com.android.sample.model.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Repository to monitor network connectivity status. */
class NetworkStatusRepository(private val context: Context) {

  // Connectivity Manager to monitor network status
  private val connectivityManager =
      context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  // Boolean Flow indicating whether the device is connected to the internet or not
  val isConnected: Flow<Boolean> = callbackFlow {
    val callback =
        object : ConnectivityManager.NetworkCallback() {

          override fun onAvailable(network: Network) {
            trySend(element = true)
          }

          override fun onLost(network: Network) {
            trySend(element = hasInternet())
          }
        }

    // Register network callback to monitor internet connectivity
    val request =
        NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()

    connectivityManager.registerNetworkCallback(request, callback)

    // Initial status
    trySend(hasInternet())

    // Clean up when the flow is closed
    awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
  }

  /* Check if there is an active internet connection */
  private fun hasInternet(): Boolean {
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }
}
