package com.android.sample.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.StreetViewPanoramaOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.ktx.MapsExperimentalFeature

object MapScreenTestTags {
  const val GOOGLE_MAP_SCREEN = "mapScreen"
}

/** Displays the Map in a screen composable. */
@OptIn(MapsExperimentalFeature::class)
@Composable
fun MapScreen() {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(46.5191, 6.5668), 10f)
    }
    val options = GoogleMapOptions()
    options.mapType(MAP_TYPE_HYBRID)
        .compassEnabled(true)
        .rotateGesturesEnabled(true)
        .tiltGesturesEnabled(true)
    GoogleMap(
        modifier =
            Modifier.fillMaxSize().testTag(MapScreenTestTags.GOOGLE_MAP_SCREEN),
        cameraPositionState = cameraPositionState,
        googleMapOptionsFactory = {options}
    ) {}


}
