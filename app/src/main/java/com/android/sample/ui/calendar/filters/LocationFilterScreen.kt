package com.android.sample.ui.calendar.filters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.ui.calendar.filters.components.FilterCheckbox
import com.android.sample.ui.components.BottomNavigationButtons
import com.android.sample.ui.theme.*

/** Local test tags for LocationFilterScreen. */
object LocationFilterTestTags {
  const val SCREEN = "LocationFilter_Screen"
  const val HEADER = "LocationFilter_Header"
  const val BACK_BUTTON = "LocationFilter_BackButton"
  const val TITLE = "LocationFilter_Title"
  const val LIST = "LocationFilter_List"
  const val ITEM_PREFIX = "LocationFilter_Item_"
  const val CLEAR_BUTTON = "LocationFilter_Clear"
  const val APPLY_BUTTON = "LocationFilter_Apply"
}

/**
 * A screen for selecting one or more locations to filter calendar events.
 *
 * Displays a header with a back button, a scrollable list of locations with checkboxes, and
 * Clear/Apply action buttons at the bottom.
 *
 * @param selected The list of locations initially selected.
 * @param onBack Called when the user navigates back without applying changes.
 * @param onApply Called with the updated list when the user confirms their selection.
 */
@Composable
fun LocationFilterScreen(
    selected: List<String>,
    onBack: () -> Unit,
    onApply: (List<String>) -> Unit
) {
  // State for current selections
  var selections by remember { mutableStateOf(selected.toList()) }

  // Placeholder locations (replace with backend data later)
  val locations = listOf("Salle 1", "Salle 2", "Unknown")

  Column(modifier = Modifier.padding(PaddingLarge).testTag(LocationFilterTestTags.SCREEN)) {

    // ---------------- HEADER ----------------
    Row(
        modifier = Modifier.fillMaxWidth().testTag(LocationFilterTestTags.HEADER),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          IconButton(
              onClick = onBack, modifier = Modifier.testTag(LocationFilterTestTags.BACK_BUTTON)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.goBack))
              }

          Text(
              text = stringResource(R.string.location),
              style = MaterialTheme.typography.titleLarge,
              modifier = Modifier.testTag(LocationFilterTestTags.TITLE))

          Spacer(modifier = Modifier.width(widthSmall))
        }

    Spacer(Modifier.height(SpacingMedium))

    // ---------------- SCROLL LIST ----------------
    LazyColumn(modifier = Modifier.weight(Weight).testTag(LocationFilterTestTags.LIST)) {
      items(locations) { location ->
        Column(modifier = Modifier.testTag(LocationFilterTestTags.ITEM_PREFIX + location)) {
          FilterCheckbox(
              label = location,
              key = location,
              isChecked = location in selections,
              onCheckedChange = { checked ->
                selections = if (checked) selections + location else selections - location
              })
        }

        Spacer(Modifier.height(SpacingSmall))
      }
    }

    Spacer(Modifier.height(SpacingExtraLarge))

    // ---------------- BOTTOM BUTTONS ----------------
    BottomNavigationButtons(
        onBack = { selections = emptyList() },
        onNext = { onApply(selections) },
        canGoBack = true,
        canGoNext = true,
        backButtonText = stringResource(R.string.clear_all),
        nextButtonText = stringResource(R.string.apply),
        backButtonTestTag = LocationFilterTestTags.CLEAR_BUTTON,
        nextButtonTestTag = LocationFilterTestTags.APPLY_BUTTON)
  }
}

@Preview(showBackground = true)
@Composable
fun LocationFilterScreenPreview() {
  MaterialTheme { LocationFilterScreen(selected = listOf("Salle 1"), onBack = {}, onApply = {}) }
}
