package com.android.sample.ui.map

import android.Manifest
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.map.MapScreenTestTags.CREATE_AREA_BUTTON
import com.android.sample.ui.map.MapScreenTestTags.CREATE_AREA_FLOATING_BUTTON
import com.android.sample.ui.map.MapScreenTestTags.DELETE_MARKER_BUTTON
import com.android.sample.ui.map.MapScreenTestTags.DOWN_SHEET
import com.android.sample.ui.theme.Blue
import com.android.sample.ui.theme.Blue70
import com.android.sample.ui.theme.DefaultZoom
import com.android.sample.ui.theme.Red80
import com.android.sample.ui.theme.Salmon
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.SpacingSmall
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
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
  const val DELETE_MARKER_BUTTON = "delete_marker_button"
  const val CREATE_AREA_FLOATING_BUTTON = "delete_marker_button"
}

enum class BottomSheetState {
  HIDE,
  ADD_AREA,
  DELETE_MARKER,
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
  val sheetState = rememberModalBottomSheetState()
  var showBottomSheet by remember { mutableStateOf(BottomSheetState.HIDE) }
  var deleteMarkerId by remember { mutableStateOf("") }
  val tooltipState = rememberTooltipState(isPersistent = true)
  val scope = rememberCoroutineScope()
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
            },
            actions = {
              TooltipBox(
                  positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(8.dp),
                  tooltip = {
                    RichTooltip(
                        caretSize = TooltipDefaults.caretSize,
                    ) {
                      Column(modifier = Modifier.testTag(MapScreenTestTags.TOOLTIP_TEXT)) {
                        Text(
                            stringResource(R.string.tooltip_area_marker_title),
                            fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.tooltip_area_marker_text))
                        Text(
                            stringResource(R.string.tooltip_area_multiple_marker_title),
                            fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.tooltip_area_multiple_marker_text))
                        Text(
                            stringResource(R.string.tooltip_area_delete_marker_title),
                            fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.tooltip_area_delete_marker_text))
                        Text(
                            stringResource(R.string.tooltip_area_create_title),
                            fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.tooltip_area_create_text))
                      }
                    }
                  },
                  state = tooltipState) {
                    IconButton(
                        modifier = Modifier.testTag(MapScreenTestTags.TOOLTIP_BUTTON),
                        onClick = { scope.launch { tooltipState.show() } }) {
                          Icon(imageVector = Icons.Filled.Info, contentDescription = "Info")
                        }
                  }
            })
      },
      floatingActionButton = {
        ExtendedFloatingActionButton(
          modifier = Modifier.testTag(CREATE_AREA_FLOATING_BUTTON),
            text = { Text(stringResource(R.string.create_area_button)) },
            icon = {
              Icon(
                  Icons.Default.Add,
                  contentDescription = stringResource(R.string.create_area_button))
            },
            onClick = { showBottomSheet = BottomSheetState.ADD_AREA },
            containerColor = Salmon)
      },
      floatingActionButtonPosition = FabPosition.Start,
      content = { padding ->
        Column(modifier = Modifier.fillMaxSize()) {
          Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(padding)) {
            GoogleMap(
                modifier = Modifier.matchParentSize().testTag(MapScreenTestTags.GOOGLE_MAP_SCREEN),
                cameraPositionState = cameraPositionState,
                onMapLongClick = { pos -> mapViewModel.addNewMarker(pos) },
                properties =
                    MapProperties(
                        isMyLocationEnabled = uiState.hasPermission, mapType = MapType.NORMAL)) {
                  uiState.listNewMarker.forEach { marker ->
                    Marker(
                        state =
                            MarkerState(
                                position =
                                    LatLng(marker.location.latitude, marker.location.longitude)),
                        title = marker.label,
                        draggable = false,
                        onClick = { _ ->
                          showBottomSheet = BottomSheetState.DELETE_MARKER
                          deleteMarkerId = marker.id
                          true
                        },
                        tag = marker.id)
                  }
                  uiState.listArea.forEach { area ->
                    Polygon(
                        points =
                            area.markers.map { marker ->
                              LatLng(marker.location.latitude, marker.location.longitude)
                            },
                        strokeColor = Blue,
                        fillColor = Blue70,
                        onClick = {},
                        tag = area.id)
                  }
                }
          }
          if (showBottomSheet != BottomSheetState.HIDE) {
            ModalBottomSheet(
                modifier = Modifier.testTag(DOWN_SHEET),
                onDismissRequest = { showBottomSheet = BottomSheetState.HIDE },
                sheetState = sheetState) {
                  when (showBottomSheet) {
                    BottomSheetState.ADD_AREA ->
                        Column(Modifier.fillMaxWidth().padding(SpacingLarge)) {
                          Text(stringResource(R.string.down_sheet_title))
                          Spacer(Modifier.height(SpacingSmall))
                          OutlinedTextField(
                              value = uiState.nextAreaName,
                              onValueChange = { mapViewModel.setNewAreaName(it) },
                              label = { Text(stringResource(R.string.down_sheet_text_field)) },
                              singleLine = true,
                              modifier = Modifier.fillMaxWidth())
                          Spacer(Modifier.height(SpacingSmall))
                          Button(
                              onClick = {
                                mapViewModel.createNewArea()
                                showBottomSheet = BottomSheetState.HIDE
                              },
                              modifier = Modifier.fillMaxWidth().testTag(CREATE_AREA_BUTTON),
                              colors =
                                  ButtonColors(
                                      Salmon, Color.Black, Color.DarkGray, Color.LightGray)) {
                                Text(stringResource(R.string.down_sheet_button_create))
                              }
                        }
                    BottomSheetState.DELETE_MARKER ->
                        Column(Modifier.fillMaxWidth().padding(SpacingLarge)) {
                          Button(
                              onClick = {
                                mapViewModel.deleteMarker(deleteMarkerId)
                                showBottomSheet = BottomSheetState.HIDE
                              },
                              modifier = Modifier.fillMaxWidth().testTag(DELETE_MARKER_BUTTON),
                              colors =
                                  ButtonColors(
                                      Red80, Color.Black, Color.DarkGray, Color.LightGray)) {
                                Text(stringResource(R.string.down_sheet_button_delete))
                              }
                        }
                    else -> {}
                  }
                }
          }
        }
      })
}
