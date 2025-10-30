package com.android.sample.ui.map

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.android.sample.BuildConfig
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object DefaultLocation {
    const val LATITUDE = 46.5191
    const val LONGITUDE = 6.5668
}

data class MapUiState(
    val currentLocation: LatLng = LatLng(DefaultLocation.LATITUDE, DefaultLocation.LONGITUDE),
    val errorLocation: String? = null,
    val hasPermission: Boolean = false
)

class MapViewModel(
    app: Application
) : AndroidViewModel(app) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(app)

    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state

    init {
        val apiKey = BuildConfig.MAPS_API_KEY
        Places.initializeWithNewPlacesApiEnabled(app, apiKey)
    }

    fun fetchUserLocation() {
        val app = getApplication<Application>()
        val fine = Manifest.permission.ACCESS_FINE_LOCATION
        val coarse = Manifest.permission.ACCESS_COARSE_LOCATION

        val hasLocation =
            ContextCompat.checkSelfPermission(app, fine) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(app, coarse) == PackageManager.PERMISSION_GRANTED

        if (!hasLocation) {
            _state.value = MapUiState(
                errorLocation = "Location permission required\nTo show your position on the map, we need access to your location. Please enable it in your device settings."
            )
            return
        }

        fusedClient.lastLocation
            .addOnSuccessListener { location ->
                _state.value = MapUiState(
                    currentLocation = LatLng(location.latitude, location.longitude),
                    hasPermission = true
                )
            }
            .addOnFailureListener { e ->
                _state.value = MapUiState(
                    errorLocation = e.message,
                    hasPermission = true
                )
            }
    }
}

