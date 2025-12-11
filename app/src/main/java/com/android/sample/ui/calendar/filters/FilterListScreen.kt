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
import com.android.sample.ui.calendar.filters.components.FilterCheckbox
import com.android.sample.ui.common.BottomNavigationButtons
import com.android.sample.ui.theme.*

// Assisted by AI
/**
 * A generic reusable screen used by all filter screens (Participants, Locations, Event Types).
 *
 * Structure:
 * - Header (back button + title)
 * - Scrollable list with checkboxes
 * - Bottom navigation buttons (Clear / Apply)
 *
 * @param title The title displayed in the header.
 * @param items The list of selectable items.
 * @param selected Initially selected items.
 * @param onBack Called when user presses back.
 * @param onApply Called when the user confirms their selection.
 * @param testTagPrefix A prefix used to generate all test tags.
 */
@Composable
fun FilterListScreen(
    title: String,
    items: List<String>,
    selected: List<String>,
    onBack: () -> Unit,
    onApply: (List<String>) -> Unit,
    testTagPrefix: String
) {
  var selections by remember { mutableStateOf(selected.toList()) }

  // ----------- Auto-generated TestTags -----------
  val screenTag = "${testTagPrefix}_Screen"
  val headerTag = "${testTagPrefix}_Header"
  val backTag = "${testTagPrefix}_BackButton"
  val titleTag = "${testTagPrefix}_Title"
  val listTag = "${testTagPrefix}_List"
  val itemPrefix = "${testTagPrefix}_Item_"
  val clearTag = "${testTagPrefix}_Clear"
  val applyTag = "${testTagPrefix}_Apply"

  Column(modifier = Modifier.padding(PaddingLarge).testTag(screenTag)) {

    // ----- Header -----
    Row(
        modifier = Modifier.fillMaxWidth().testTag(headerTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onBack, modifier = Modifier.testTag(backTag)) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(com.android.sample.R.string.goBack))
          }

          Text(
              text = title,
              style = MaterialTheme.typography.titleLarge,
              modifier = Modifier.testTag(titleTag))

          Spacer(modifier = Modifier.width(widthSmall))
        }

    Spacer(Modifier.height(SpacingMedium))

    // ----- Scrollable list -----
    LazyColumn(modifier = Modifier.weight(Weight).testTag(listTag)) {
      items(items) { item ->
        Column(modifier = Modifier.testTag(itemPrefix + item)) {
          FilterCheckbox(
              label = item,
              key = item,
              isChecked = item in selections,
              onCheckedChange = { checked ->
                selections = if (checked) selections + item else selections - item
              })
        }

        Spacer(Modifier.height(SpacingSmall))
      }
    }

    Spacer(Modifier.height(SpacingExtraLarge))

    // ----- Bottom Buttons -----
    BottomNavigationButtons(
        onBack = { selections = emptyList() },
        onNext = { onApply(selections) },
        canGoBack = true,
        canGoNext = true,
        backButtonText = stringResource(com.android.sample.R.string.clear_all),
        nextButtonText = stringResource(com.android.sample.R.string.apply),
        backButtonTestTag = clearTag,
        nextButtonTestTag = applyTag)
  }
}

@Preview(showBackground = true)
@Composable
fun FilterListScreenPreview() {
  val sampleItems = listOf("Option A", "Option B", "Option C")

  MaterialTheme {
    FilterListScreen(
        title = "Sample Filter",
        items = sampleItems,
        selected = listOf("Option B"), // Pre-selected for visual clarity
        onBack = {},
        onApply = {},
        testTagPrefix = "SampleFilter")
  }
}
