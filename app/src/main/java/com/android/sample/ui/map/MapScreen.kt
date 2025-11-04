package com.android.sample.ui.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.theme.DefaultZoom
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
  const val MAP_TITLE = "map_title"
  const val MAP_GO_BACK_BUTTON = "map_go_back_button"
}

/** Displays the Map in a screen composable. */
@OptIn(MapsExperimentalFeature::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    mapViewModel: MapViewModel = viewModel(),
    onGoBack: () -> Unit = {},
) {
  val uiState by mapViewModel.state.collectAsState()
  val cameraPositionState = rememberCameraPositionState()

  LaunchedEffect(Unit) {
    snapshotFlow { uiState.currentLocation }
        .distinctUntilChanged()
        .filterNotNull()
        .collect { target ->
          cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(target, DefaultZoom))
        }
  }

  val locationPermissionLauncher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) mapViewModel.fetchUserLocation()
      }

  LaunchedEffect(Unit) {
    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
  }

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(stringResource(R.string.delimit_organization_title), Modifier.testTag(MapScreenTestTags.MAP_TITLE))
            },
            navigationIcon = {
              IconButton(
                  onClick = { onGoBack() },
                  Modifier.testTag(MapScreenTestTags.MAP_GO_BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            },
        )
      },
      content = { padding ->
        GoogleMap(
            modifier =
                Modifier.fillMaxSize()
                    .testTag(MapScreenTestTags.GOOGLE_MAP_SCREEN)
                    .padding(padding),
            cameraPositionState = cameraPositionState,
            properties =
                MapProperties(
                    isMyLocationEnabled = uiState.hasPermission, mapType = MapType.NORMAL)) {}
      })
}
