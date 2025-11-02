package com.android.sample.ui.map

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.android.sample.BuildConfig
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * EPFL Location for default value
 */
object DefaultLocation {
  const val LATITUDE = 46.5191
  const val LONGITUDE = 6.5668
}

/**
 * Represents the Stat of the screen UI .
 *
 * @property currentLocation current Location of the phone. has default value centered in EPFL
 * @property errorLocation in case where we cannot fetch the location, we show the error message.
 * @property hasPermission if the user has grant permission of location to our app.
 */
data class MapUiState(
    val currentLocation: LatLng = LatLng(DefaultLocation.LATITUDE, DefaultLocation.LONGITUDE),
    val errorLocation: String? = null,
    val hasPermission: Boolean = false
)

class MapViewModel(app: Application) : AndroidViewModel(app) {
    /**
     * Provider for android GPS
     */
  private val fusedClient = LocationServices.getFusedLocationProviderClient(app)

  private val _state = MutableStateFlow(MapUiState())
  val state: StateFlow<MapUiState> = _state

  init {
    val apiKey = BuildConfig.MAPS_API_KEY
    Places.initializeWithNewPlacesApiEnabled(app, apiKey)
  }

    /**
     * Verify that the user has given the right to get his location
     *
     * then fetch the User Location, if another app has already fetch it, it get this cached value
     * with lastLocation
     *
     * if there is no cached location, it ask the GPS to compute a new one with getCurrentLocation
     *
     * if the provider make an error, it update the State to an error State
     */
  fun fetchUserLocation() {
    val app = getApplication<Application>()
    val fine = Manifest.permission.ACCESS_FINE_LOCATION
    val coarse = Manifest.permission.ACCESS_COARSE_LOCATION
    val cancellationTokenSource = CancellationTokenSource()

    val hasLocation =
        ContextCompat.checkSelfPermission(app, fine) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(app, coarse) == PackageManager.PERMISSION_GRANTED

    if (!hasLocation) {
      _state.value =
          MapUiState(
              errorLocation =
                  "Location permission required\nTo show your position on the map, we need access to your location. Please enable it in your device settings.")
      return
    }

    fusedClient.lastLocation
        .addOnSuccessListener { lastLocation ->
          if (lastLocation == null) {
            fusedClient
                .getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationTokenSource.token)
                .addOnSuccessListener { location ->
                  if (location == null) {
                    _state.value =
                        MapUiState(
                            errorLocation = "Error: Cannot fetch you location",
                            hasPermission = true)
                  } else {
                    _state.value =
                        MapUiState(
                            currentLocation = LatLng(location.latitude, location.longitude),
                            hasPermission = true)
                  }
                }
                .addOnFailureListener { e ->
                  _state.value = MapUiState(errorLocation = e.message, hasPermission = true)
                }
          } else {
            _state.value =
                MapUiState(
                    currentLocation = LatLng(lastLocation.latitude, lastLocation.longitude),
                    hasPermission = true)
          }
        }
        .addOnFailureListener { e ->
          _state.value = MapUiState(errorLocation = e.message, hasPermission = true)
        }
  }
}
