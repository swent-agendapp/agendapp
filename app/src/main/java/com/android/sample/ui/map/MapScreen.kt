package com.android.sample.ui.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.ktx.MapsExperimentalFeature
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull


object MapScreenTestTags {
  const val GOOGLE_MAP_SCREEN = "mapScreen"
}

/** Displays the Map in a screen composable. */
@OptIn(MapsExperimentalFeature::class)
@Composable
fun MapScreen(
    mapViewModel: MapViewModel = viewModel()
) {
    val uiState by mapViewModel.state.collectAsState()
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(Unit) {
        snapshotFlow { uiState.currentLocation }
            .distinctUntilChanged()
            .filterNotNull()
            .collect { target ->
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(target, 17f))
            }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) mapViewModel.fetchUserLocation()
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    GoogleMap(
        modifier =
            Modifier.fillMaxSize().testTag(MapScreenTestTags.GOOGLE_MAP_SCREEN),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = uiState.hasPermission,
            mapType = MapType.NORMAL
        )
    ) {}


}
