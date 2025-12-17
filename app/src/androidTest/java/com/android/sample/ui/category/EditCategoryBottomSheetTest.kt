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
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.R
import com.android.sample.model.category.EventCategory
import com.android.sample.ui.category.components.CategoryEditBottomSheet
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Assisted by AI

@RunWith(AndroidJUnit4::class)
class CategoryEditBottomSheetTest {

  @get:Rule val composeTestRule = createComposeRule()

  private var defaultColor: Color = EventCategory.defaultCategory().color
  private lateinit var nameLabelString: String
  private var dismissCalled = false

  @Before
  fun setUp() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    nameLabelString = context.getString(R.string.edit_category_name_label)
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

      CategoryEditBottomSheet(
          showBottomSheet = showBottomSheet,
          sheetState = sheetState,
          initialLabel = initialLabel,
          initialColor = initialColor,
          onDismiss = { dismissCalled = true },
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

    composeTestRule.onNodeWithText(label).assertExists().assertIsDisplayed()

    // Save button is visible and enabled when label is non-blank
    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET_SAVE_BUTTON)
        .assertExists()
        .assertIsDisplayed()
        .assertIsEnabled()
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
  }

  @Test
  fun saveButton_callsOnDismiss() {
    val label = "My category"
    setContent(showBottomSheet = true, initialLabel = label, initialColor = defaultColor)

    // Click Save
    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET_SAVE_BUTTON)
        .assertIsEnabled()
        .performClick()

    // onDismiss must have been called
    composeTestRule.runOnIdle { assertTrue(dismissCalled) }
  }
}
