package com.android.sample.ui.category

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.R
import com.android.sample.model.category.EventCategory
import com.android.sample.ui.category.components.CategoryEditBottomSheet
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Assisted by AI

/**
 * UI tests for [com.android.sample.ui.category.components.CategoryEditBottomSheet].
 *
 * NOTE (NOW vs LATER):
 * - NOW: we pass [initialLabel] and [initialColor] directly to the composable.
 * - LATER: when the bottom sheet is driven by an EditCategoryViewModel, the setup in this file
 *   should be adapted to initialize a fake ViewModel in @Before and drive the sheet through its
 *   uiState instead of passing these parameters.
 */
@RunWith(AndroidJUnit4::class)
class CategoryEditBottomSheetTest {

  @get:Rule val composeTestRule = createComposeRule()

  private var defaultColor: Color = EventCategory.defaultCategory().color
  private lateinit var nameLabelString: String

  @Before
  fun setUp() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    nameLabelString = context.getString(R.string.edit_category_name_label)

    // LATER: when using a ViewModel, create and configure a fake EditCategoryViewModel here.
    // For example:
    // fakeViewModel = FakeEditCategoryViewModel(initialUiState = ...)
    // and drive the sheet with fakeViewModel.uiState in setContent().
  }

  @OptIn(ExperimentalMaterial3Api::class)
  private fun setContent(
      showBottomSheet: Boolean,
      initialLabel: String?,
      initialColor: Color? = null,
  ) {
    composeTestRule.setContent {
      val sheetState =
          rememberModalBottomSheetState(
              skipPartiallyExpanded = true,
          )

      // LATER: when using a ViewModel, remove initialLabel / initialColor parameters
      // and instead pass the ViewModel instance down (or read its uiState here).
      CategoryEditBottomSheet(
          showBottomSheet = showBottomSheet,
          sheetState = sheetState,
          initialLabel = initialLabel,
          initialColor = initialColor,
          onDismiss = {},
          onSave = { _, _ -> },
      )
    }
  }

  @Test
  fun bottomSheet_showsAllElements_whenInputsAreValid() {
    val label = "My category"

    setContent(showBottomSheet = true, initialLabel = label, initialColor = defaultColor)

    // Bottom sheet root
    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET)
        .assertExists()
        .assertIsDisplayed()

    // Label text field, pre-filled with initial label
    composeTestRule.onNodeWithText(label).assertExists().assertIsDisplayed()

    // Save button is visible and enabled when label is non-blank
    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET_SAVE_BUTTON)
        .assertExists()
        .assertIsDisplayed()
        .assertIsEnabled()

    // NOTE: we do not assert the ColorSelector directly here because it does not receive a
    // dedicated testTag from this composable.
    // LATER: if we expose a tag or drive the color from ViewModel, we can assert its state too.
  }

  @Test
  fun bottomSheet_notShown_whenShowBottomSheetIsFalse() {
    setContent(showBottomSheet = false, initialLabel = "My category", initialColor = defaultColor)

    // Root container of the bottom sheet must not exist
    composeTestRule.onNodeWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET).assertDoesNotExist()
  }

  @Test
  fun initialNullLabel_showsPlaceholder() {
    setContent(showBottomSheet = true, initialLabel = null, initialColor = defaultColor)

    // The "Category name" text (edit_category_name_label) is displayed as label/placeholder
    composeTestRule.onNodeWithText(nameLabelString).assertIsDisplayed()
  }

  @Test
  fun saveButton_togglesEnabled_whenUserTypesAndClearsText_initialLabelNull() {
    setContent(showBottomSheet = true, initialLabel = null, initialColor = defaultColor)

    val textField =
        composeTestRule.onNodeWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET_LABEL_TEXT_FIELD)
    val saveButton =
        composeTestRule.onNodeWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET_SAVE_BUTTON)

    // Initially: label is "", so save button must be disabled
    saveButton.assertIsNotEnabled()

    // Type a non-blank character -> button becomes enabled
    textField.performTextInput("A")
    saveButton.assertIsEnabled()

    // Clear text -> button becomes disabled again
    textField.performTextClearance()
    saveButton.assertIsNotEnabled()

    // LATER: with ViewModel, instead of checking only button state, we might also assert that
    // the ViewModel receives the updated label (through its events).
  }

  @Test
  fun saveButton_togglesEnabled_whenUserTypesAndClearsText_initialLabelSpaces() {
    // Initial label is only spaces -> trim() is empty -> disabled
    setContent(showBottomSheet = true, initialLabel = "   ", initialColor = defaultColor)

    val textField =
        composeTestRule.onNodeWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET_LABEL_TEXT_FIELD)
    val saveButton =
        composeTestRule.onNodeWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET_SAVE_BUTTON)

    // Initially: label is "   " so label.trim() is empty -> disabled
    saveButton.assertIsNotEnabled()

    // Type a non-blank character -> button becomes enabled
    textField.performTextInput("X")
    saveButton.assertIsEnabled()

    // Clear text -> button becomes disabled again
    textField.performTextClearance()
    saveButton.assertIsNotEnabled()

    // LATER: this test can also assert the ViewModel's uiState when the text value changes.
  }
}
