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

object ParticipantFilterTestTags {
  const val SCREEN = "ParticipantFilter_Screen"
  const val HEADER = "ParticipantFilter_Header"
  const val TITLE = "ParticipantFilter_Title"
  const val BACK_BUTTON = "ParticipantFilter_BackButton"
  const val LIST = "ParticipantFilter_List"
  const val ITEM_PREFIX = "ParticipantFilter_Item_"
  const val APPLY = "ParticipantFilter_Apply"
  const val CLEAR = "ParticipantFilter_Clear"
}

/**
 * A screen for selecting participants to filter calendar events.
 *
 * Shows a header with a back button, a scrollable list of participant names with checkboxes, and
 * Clear/Apply buttons at the bottom.
 *
 * @param selected The initially selected participants.
 * @param onBack Called when the user returns without applying changes.
 * @param onApply Called with the updated list of selected participants.
 */
@Composable
fun ParticipantFilterScreen(
    selected: List<String>,
    onBack: () -> Unit,
    onApply: (List<String>) -> Unit
) {
  var selections by remember { mutableStateOf(selected.toList()) }

  val participants =
      listOf(
          "Alice", "Bob", "Charlie", "David", "Emma", "Lucas", "Sophie", "Martin", "Olivia", "Noah")

  Column(modifier = Modifier.padding(PaddingLarge).testTag(ParticipantFilterTestTags.SCREEN)) {

    // ----- Header -----
    Row(
        modifier = Modifier.fillMaxWidth().testTag(ParticipantFilterTestTags.HEADER),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          IconButton(
              onClick = onBack,
              modifier = Modifier.testTag(ParticipantFilterTestTags.BACK_BUTTON)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back))
              }

          Text(
              stringResource(R.string.filter_participants),
              style = MaterialTheme.typography.titleLarge,
              modifier = Modifier.testTag(ParticipantFilterTestTags.TITLE))

          Spacer(modifier = Modifier.width(widthSmall))
        }

    Spacer(Modifier.height(SpacingMedium))

    // ----- Scrollable list -----
    LazyColumn(modifier = Modifier.weight(Weight).testTag(ParticipantFilterTestTags.LIST)) {
      items(participants) { person ->
        Column(modifier = Modifier.testTag(ParticipantFilterTestTags.ITEM_PREFIX + person)) {
          FilterCheckbox(
              label = person,
              key = person,
              isChecked = person in selections,
              onCheckedChange = { checked ->
                selections = if (checked) selections + person else selections - person
              })
        }

        Spacer(Modifier.height(SpacingSmall))
      }
    }

    Spacer(Modifier.height(SpacingExtraLarge))

    // ----- Bottom fixed action buttons -----
    BottomNavigationButtons(
        onBack = { selections = emptyList() },
        onNext = { onApply(selections) },
        canGoBack = true,
        canGoNext = true,
        backButtonText = stringResource(R.string.clear_all),
        nextButtonText = stringResource(R.string.apply),
        backButtonTestTag = ParticipantFilterTestTags.CLEAR,
        nextButtonTestTag = ParticipantFilterTestTags.APPLY)
  }
}

@Preview(showBackground = true)
@Composable
fun ParticipantFilterScreenPreview() {
  MaterialTheme {
    ParticipantFilterScreen(selected = listOf("Alice", "Charlie"), onBack = {}, onApply = {})
  }
}
