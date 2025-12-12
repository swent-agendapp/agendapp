package com.android.sample.ui.category.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.android.sample.R
import com.android.sample.model.category.EventCategory
import com.android.sample.ui.category.EditCategoryScreenTestTags
import com.android.sample.ui.common.ColorSelector
import com.android.sample.ui.common.PrimaryButton
import com.android.sample.ui.theme.EventPalette
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.SpacingMedium
import com.android.sample.ui.theme.SpacingSmall

/**
 * Bottom sheet used to create or edit a category.
 *
 * When [showBottomSheet] is true, a [ModalBottomSheet] is displayed with:
 * - a [ColorSelector] to choose the category color,
 * - an [OutlinedTextField] to edit the label,
 * - a save button.
 *
 * The sheet is pre-filled using [initialLabel] and [initialColor]. When the user presses save,
 * [onSave] is called with the trimmed label and selected color.
 *
 * Currently the state is managed locally by `EditCategoryScreen`. Later, this sheet will be fully
 * driven by an `EditCategoryViewModel`.
 *
 * @param showBottomSheet Whether the sheet is visible.
 * @param sheetState State of the modal bottom sheet.
 * @param initialLabel Initial text for the label field, or `null` when creating a new category.
 * @param initialColor Initial color, or a default category color if `null`.
 * @param onDismiss Called when the sheet is dismissed.
 * @param onSave Called when the user confirms, passing the new label and color.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditBottomSheet(
    showBottomSheet: Boolean = false,
    sheetState: SheetState,
    initialLabel: String? = EventCategory.defaultCategory().label,
    initialColor: Color? = EventCategory.defaultCategory().color,
    onDismiss: () -> Unit = {},
    onSave: (String, Color) -> Unit = { _, _ -> },
) {
  if (!showBottomSheet) return

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
    CategoryEditBottomSheetContent(
        initialLabel = initialLabel,
        initialColor = initialColor ?: EventCategory.defaultCategory().color,
        onSave = onSave,
    )
  }
}

@Composable
private fun CategoryEditBottomSheetContent(
    initialLabel: String? = EventCategory.defaultCategory().label,
    initialColor: Color = EventCategory.defaultCategory().color,
    onSave: (String, Color) -> Unit = { _, _ -> },
) {
  // NOW: local bottom-sheet state driven by selectedCategory
  // LATER: this entire content will be bound to ViewModel uiState and events
  var currentColor by remember(initialColor) { mutableStateOf(initialColor) }
  var label by remember(initialLabel) { mutableStateOf(initialLabel ?: "") }

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(PaddingMedium)
              .testTag(EditCategoryScreenTestTags.BOTTOM_SHEET)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Color selector.
          // NOW: default to EventCategory.defaultCategory() if no selection
          // LATER: use ViewModel's selectedCategory.color
          Column {
            // this Spacer is a trick visually to align the ColorSelector with the OutlinedTextField
            Spacer(modifier = Modifier.height(SpacingSmall))
            ColorSelector(
                selectedColor = currentColor,
                onColorSelected = { currentColor = it },
                colors = EventPalette.defaultColors)
          }

          Spacer(modifier = Modifier.width(SpacingMedium))

          // Text field for the label
          // NOW: local state derived from selectedCategory
          // LATER: call editCategoryViewModel.setLabel(categoryId, newLabel)
          OutlinedTextField(
              value = label,
              onValueChange = { newValue -> label = newValue },
              label = { Text(stringResource(R.string.edit_category_name_label)) },
              placeholder = { Text(text = stringResource(R.string.edit_category_name_label)) },
              modifier = Modifier.testTag(EditCategoryScreenTestTags.BOTTOM_SHEET_LABEL_TEXT_FIELD),
          )
        }

        Spacer(modifier = Modifier.height(SpacingLarge))

        PrimaryButton(
            modifier = Modifier.testTag(EditCategoryScreenTestTags.BOTTOM_SHEET_SAVE_BUTTON),
            text = stringResource(R.string.edit_category_save_button),
            enabled = label.trim().isNotEmpty(),
            onClick = { onSave(label, currentColor) })
      }
}
