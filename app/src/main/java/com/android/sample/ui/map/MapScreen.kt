package com.android.sample.ui.map

import android.Manifest
import android.app.Application
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.android.sample.R
import com.android.sample.model.map.Marker
import com.android.sample.ui.common.PrimaryButton
import com.android.sample.ui.map.MapScreenTestTags.CREATE_AREA_BUTTON
import com.android.sample.ui.map.MapScreenTestTags.DOWN_SHEET
import com.android.sample.ui.map.MapScreenTestTags.DOWN_SHEET_FORM
import com.android.sample.ui.theme.DefaultZoom
import com.android.sample.ui.theme.MapPalette
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.Palette
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.SpacingSmall
import com.android.sample.ui.theme.Weight
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.ktx.MapsExperimentalFeature
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

object MapScreenTestTags {
  const val GOOGLE_MAP_SCREEN = "mapScreen"
  const val MAP_TITLE = "map_title"
  const val MAP_GO_BACK_BUTTON = "map_go_back_button"
  const val TOOLTIP_BUTTON = "tooltip_button"
  const val TOOLTIP_TEXT = "tooltip_text"
  const val CREATE_AREA_BUTTON = "create_area_button"
  const val DOWN_SHEET = "down_sheet"
  const val DOWN_SHEET_FORM = "down_sheet_form"
  const val DELETE_MARKER_BUTTON = "delete_marker_button"
  const val CREATE_AREA_FLOATING_BUTTON = "create_area_floating_button"
}

/** Displays the Map in a screen composable. */
@OptIn(MapsExperimentalFeature::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    mapViewModel: MapViewModel =
        MapViewModel(app = LocalContext.current.applicationContext as Application),
    onGoBack: () -> Unit = {},
) {
  val uiState by mapViewModel.state.collectAsState()
  val cameraPositionState = rememberCameraPositionState()

  val sheetState = rememberModalBottomSheetState()
  var showBottomSheet by remember { mutableStateOf(false) }

  val context = LocalContext.current

  LaunchedEffect(uiState.errorMessage) {
    uiState.errorMessage?.let { message ->
      Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
      mapViewModel.cleanMessageError()
    }
  }

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
              Text(
                  stringResource(R.string.delimit_organization_title),
                  Modifier.testTag(MapScreenTestTags.MAP_TITLE))
            },
            navigationIcon = {
              IconButton(
                  onClick = { onGoBack() },
                  Modifier.testTag(MapScreenTestTags.MAP_GO_BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      },
      content = { padding ->
        Column(modifier = Modifier.fillMaxSize()) {
          Box(modifier = Modifier.weight(Weight).fillMaxWidth().padding(padding)) {
            GoogleMap(
                modifier = Modifier.matchParentSize().testTag(MapScreenTestTags.GOOGLE_MAP_SCREEN),
                cameraPositionState = cameraPositionState,
                onMapLongClick = { pos ->
                  mapViewModel.selectNewMarker(pos)
                  showBottomSheet = true
                },
                properties =
                    MapProperties(
                        isMyLocationEnabled = uiState.hasPermission, mapType = MapType.NORMAL)) {
                  if (uiState.selectedMarker != null)
                  {
                    Marker(
                      state =
                        MarkerState(position =
                          LatLng(uiState.selectedMarker!!.location.latitude, uiState.selectedMarker!!.location.longitude),
                        )
                    )
                    Circle(center =
                      LatLng(uiState.selectedMarker!!.location.latitude, uiState.selectedMarker!!.location.longitude),
                      radius = uiState.selectedRadius)
                  }
                  uiState.listArea.forEach { area ->
                    Circle(
                        center =
                            LatLng(area.marker.location.latitude, area.marker.location.longitude),
                        radius = area.radius,
                        strokeColor = MapPalette.Stroke,
                        fillColor = MapPalette.Fill,
                        onClick = { _ ->
                          mapViewModel.selectArea(area)
                          showBottomSheet = true
                        })
                  }
                }
          }
          if (showBottomSheet) {
            ModalBottomSheet(
                modifier = Modifier.testTag(DOWN_SHEET),
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState) {
                  Column(Modifier.fillMaxWidth().padding(SpacingLarge)) {
                    Text(stringResource(R.string.down_sheet_title))
                    Spacer(Modifier.height(SpacingSmall))
                    OutlinedTextField(
                        value = uiState.selectedAreaName,
                        onValueChange = { mapViewModel.setNewAreaName(it) },
                        label = { Text(stringResource(R.string.down_sheet_text_field)) },
                        singleLine = true,
                        modifier =
                            Modifier.fillMaxWidth()
                                .testTag(DOWN_SHEET_FORM)
                                .padding(horizontal = PaddingMedium))
                    Spacer(Modifier.height(SpacingSmall))
                    Column {
                      Slider(
                          value = uiState.selectedRadius.toFloat(),
                          onValueChange = { it -> mapViewModel.setNewAreaRadius(it.toDouble()) },
                          colors =
                              SliderDefaults.colors(
                                  thumbColor = Palette.PastelOrange,
                                  activeTrackColor = Palette.Orchid,
                                  inactiveTrackColor = Palette.LightGray,
                              ),
                          steps = 10,
                          valueRange = 0f..100f)
                      Text(text = uiState.selectedRadius.toString())
                    }
                    PrimaryButton(
                        modifier = Modifier.testTag(CREATE_AREA_BUTTON),
                        text = stringResource(R.string.down_sheet_button_create),
                        onClick = {
                          mapViewModel.createNewArea()
                          showBottomSheet = false
                        },
                    )
                  }
                }
          }
        }
      })
}
