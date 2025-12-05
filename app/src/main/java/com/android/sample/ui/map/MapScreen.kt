package com.android.sample.ui.map

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.model.map.Marker
import com.android.sample.ui.common.PrimaryButton
import com.android.sample.ui.map.MapScreenTestTags.CREATE_AREA_BUTTON
import com.android.sample.ui.map.MapScreenTestTags.DOWN_SHEET
import com.android.sample.ui.map.MapScreenTestTags.DOWN_SHEET_FORM
import com.android.sample.ui.profile.ProfileScreenTestTags
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

// Assisted by AI

/**
 * Calculates dynamic slider range based on map zoom level.
 * Higher zoom = smaller, more precise ranges.
 */
private fun getSliderRangeForZoom(zoom: Float): ClosedFloatingPointRange<Float> {
  return when {
    zoom >= 17f -> 10f..200f
    zoom >= 15f -> 50f..500f
    zoom >= 13f -> 100f..1000f
    zoom >= 11f -> 200f..2000f
    else -> 500f..5000f
  }
}

/** Renders markers and circular areas on the map. */
@Composable
private fun MapContent(uiState: MapUiState, onAreaClick: (String) -> Unit) {
  // Current selection marker and circle
  uiState.selectedMarker?.let { marker ->
    Marker(
        state = MarkerState(position = LatLng(marker.location.latitude, marker.location.longitude)))
    Circle(
        center = LatLng(marker.location.latitude, marker.location.longitude),
        strokeColor = MapPalette.Stroke,
        fillColor = MapPalette.Fill,
        radius = uiState.selectedRadius)
  }

  // Existing areas
  uiState.listArea.forEach { area ->
    Circle(
        clickable = true,
        center = LatLng(area.marker.location.latitude, area.marker.location.longitude),
        radius = area.radius,
        strokeColor = MapPalette.Stroke,
        fillColor = MapPalette.Fill,
        onClick = { _ -> onAreaClick(area.id) })
  }
}

/** Bottom sheet for creating/editing areas. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AreaBottomSheet(
    uiState: MapUiState,
    sliderRange: ClosedFloatingPointRange<Float>,
    onDismiss: () -> Unit,
    onNameChange: (String) -> Unit,
    onRadiusChange: (Double) -> Unit,
    onDelete: (String) -> Unit,
    onCreate: () -> Unit,
) {
  ModalBottomSheet(
      modifier = Modifier.testTag(DOWN_SHEET),
      onDismissRequest = onDismiss,
      sheetState = rememberModalBottomSheetState()) {
        Column(Modifier.fillMaxWidth().padding(SpacingLarge)) {
          // Title row with optional delete button
          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.down_sheet_title))
            uiState.selectedId?.let { id ->
              IconButton(
                  onClick = { onDelete(id) },
                  modifier = Modifier.testTag(MapScreenTestTags.DELETE_MARKER_BUTTON)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription =
                            stringResource(R.string.profile_save_content_description))
                  }
            }
          }

          Spacer(Modifier.height(SpacingSmall))

          // Name input
          OutlinedTextField(
              value = uiState.selectedAreaName,
              onValueChange = onNameChange,
              label = { Text(stringResource(R.string.down_sheet_text_field)) },
              singleLine = true,
              modifier =
                  Modifier.fillMaxWidth().testTag(DOWN_SHEET_FORM).padding(horizontal = PaddingMedium))

          Spacer(Modifier.height(SpacingSmall))

          // Radius slider
          Column {
            Text(
                text =
                    stringResource(R.string.down_sheet_area_size) +
                        "${uiState.selectedRadius.toInt()} m")
            Slider(
                modifier = Modifier.padding(horizontal = PaddingMedium),
                value = uiState.selectedRadius.toFloat().coerceIn(sliderRange),
                onValueChange = { onRadiusChange(it.toDouble()) },
                colors =
                    SliderDefaults.colors(
                        thumbColor = Palette.PastelOrange,
                        activeTrackColor = Palette.Orchid,
                        inactiveTrackColor = Palette.LightGray,
                    ),
                steps = 10,
                valueRange = sliderRange)
          }

          // Create button
          PrimaryButton(
              modifier = Modifier.testTag(CREATE_AREA_BUTTON),
              text = stringResource(R.string.down_sheet_button_create),
              onClick = onCreate,
          )
        }
      }
}

object MapScreenTestTags {
  const val GOOGLE_MAP_SCREEN = "mapScreen"
  const val MAP_TITLE = "map_title"
  const val MAP_GO_BACK_BUTTON = "map_go_back_button"
  const val CREATE_AREA_BUTTON = "create_area_button"
  const val DOWN_SHEET = "down_sheet"
  const val DOWN_SHEET_FORM = "down_sheet_form"
  const val DELETE_MARKER_BUTTON = "delete_marker_button"
}

/** Displays the Map in a screen composable. */
@OptIn(MapsExperimentalFeature::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    mapViewModel: MapViewModel = viewModel(factory = MapViewModel.Factory),
    onGoBack: () -> Unit = {},
) {
  val uiState by mapViewModel.state.collectAsState()
  val cameraPositionState = rememberCameraPositionState()
  var showBottomSheet by remember { mutableStateOf(false) }
  val sliderRange = getSliderRangeForZoom(cameraPositionState.position.zoom)

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

  LaunchedEffect(Unit) { mapViewModel.fetchUserLocation() }

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
                  MapContent(
                      uiState = uiState,
                      onAreaClick = { areaId ->
                        mapViewModel.selectArea(uiState.listArea.first { it.id == areaId })
                        showBottomSheet = true
                      })
                }
          }
          if (showBottomSheet) {
            AreaBottomSheet(
                uiState = uiState,
                sliderRange = sliderRange,
                onDismiss = {
                  showBottomSheet = false
                  mapViewModel.unselectArea()
                },
                onNameChange = { mapViewModel.setNewAreaName(it) },
                onRadiusChange = { mapViewModel.setNewAreaRadius(it) },
                onDelete = { id ->
                  mapViewModel.deleteArea()
                  showBottomSheet = false
                },
                onCreate = {
                  if(uiState.selectedId == null) {
                    mapViewModel.createNewArea()
                  } else {
                    mapViewModel.updateArea()
                  }
                  showBottomSheet = false
                })
          }
        }
      })
}
