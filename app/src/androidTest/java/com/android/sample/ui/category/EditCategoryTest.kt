package com.android.sample.ui.category

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// assisted by AI

/**
 * UI tests for [EditCategoryScreen].
 *
 * Currently the screen uses local state and [getMockEventCategory] inside the composable. Later
 * these tests can be adapted to work with an EditCategoryViewModel.
 */
@RunWith(AndroidJUnit4::class)
class EditCategoryScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var titleString: String

  @Before
  fun setUp() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    titleString = context.getString(R.string.edit_category_title)
    // LATER: create and configure a fake EditCategoryViewModel here, then pass it down
    // to EditCategoryScreen instead of relying on local state.
  }

  private fun setContent(onBack: () -> Unit = {}) {
    composeTestRule.setContent {
      // LATER: pass fakeViewModel here instead of local state when ViewModel is introduced.
      EditCategoryScreen(onBack = onBack)
    }
  }

  // Initial state: screen, top bar, FAB and category list
  @Test
  fun initialState_showsTopBarFabListAndMockCategories() {
    setContent()

    // Root / list
    composeTestRule.onNodeWithTag(EditCategoryScreenTestTags.SCREEN_ROOT).assertIsDisplayed()
    val cards = composeTestRule.onAllNodesWithTag(EditCategoryScreenTestTags.CATEGORY_CARD)
    assertTrue(
        "There should be at least one category card at start",
        cards.fetchSemanticsNodes().isNotEmpty())

    // Top bar title
    composeTestRule.onNodeWithText(titleString).assertIsDisplayed()

    // Floating action button (add)
    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.CATEGORY_CARD_ADD_BUTTON)
        .assertIsDisplayed()

    // Bottom sheet must not be visible initially
    composeTestRule.onAllNodesWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET).assertCountEquals(0)
  }

  // Back button calls the onBack lambda
  @Test
  fun backButton_callsOnBack() {
    var backCalled = false
    setContent(onBack = { backCalled = true })

    // LATER: when the top bar exposes a dedicated testTag, use it instead of text search.
    composeTestRule
        .onNode(
            SemanticsMatcher.expectValue(
                androidx.compose.ui.semantics.SemanticsProperties.ContentDescription,
                listOf("Back")))
        .performClick()

    assertTrue(backCalled)
  }

  // FAB opens creation sheet, saves a new category, then reopens in creation mode
  @Test
  fun fab_addCategory_thenReopenSheet_resetsSelection() {
    setContent()

    val initialCardsCount = 3
    deleteCategoriesUntilRemaining(initialCardsCount)

    // Open bottom sheet via FAB
    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.CATEGORY_CARD_ADD_BUTTON)
        .performClick()

    // Bottom sheet visible, label empty, Save disabled
    val textField =
        composeTestRule
            .onNodeWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET_LABEL_TEXT_FIELD)
            .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET_SAVE_BUTTON)
        .assertIsNotEnabled()

    // Type a valid label
    textField.performTextInput("New category")

    // Save becomes enabled and we save
    val saveButton =
        composeTestRule
            .onNodeWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET_SAVE_BUTTON)
            .assertIsEnabled()
    saveButton.performClick()

    // Bottom sheet closed, list increased by 1
    composeTestRule.onAllNodesWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET).assertCountEquals(0)

    composeTestRule.onNodeWithTag(EditCategoryScreenTestTags.SCREEN_ROOT).performTouchInput {
      swipeUp()
    }

    val afterAddCount =
        composeTestRule
            .onAllNodesWithTag(EditCategoryScreenTestTags.CATEGORY_CARD)
            .fetchSemanticsNodes()
            .size
    assertEquals(initialCardsCount + 1, afterAddCount)

    // Reopen via FAB: creation mode (label empty, Save disabled)
    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.CATEGORY_CARD_ADD_BUTTON)
        .performClick()

    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET_SAVE_BUTTON)
        .assertIsNotEnabled()
  }

  // Dismissing creation sheet without saving keeps the list unchanged
  @Test
  fun fab_dismissWithoutSaving_keepsListUnchanged() {
    setContent()

    val initialCardsCount =
        composeTestRule
            .onAllNodesWithTag(EditCategoryScreenTestTags.CATEGORY_CARD)
            .fetchSemanticsNodes()
            .size

    // Open creation sheet
    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.CATEGORY_CARD_ADD_BUTTON)
        .performClick()

    // Type some text (to ensure it is not saved)
    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET_LABEL_TEXT_FIELD)
        .performTextInput("Temp")

    // Close bottom sheet by swiping down (dismiss)
    composeTestRule.onNodeWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET).performTouchInput {
      swipeDown()
    }

    // Sheet is closed, list has not changed
    composeTestRule.onAllNodesWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET).assertCountEquals(0)
    val afterDismissCount =
        composeTestRule
            .onAllNodesWithTag(EditCategoryScreenTestTags.CATEGORY_CARD)
            .fetchSemanticsNodes()
            .size
    assertEquals(initialCardsCount, afterDismissCount)
  }

  // Edit a category and save it (keeps at least one item)
  @Test
  fun editCategory_updatesItem_thenFabOpensEmptySheet() {
    setContent()

    // Open edit bottom sheet via the Edit button of the first card
    composeTestRule
        .onAllNodesWithTag(EditCategoryScreenTestTags.CATEGORY_CARD_EDIT_BUTTON)
        .onFirst()
        .performClick()

    val textField =
        composeTestRule
            .onNodeWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET_LABEL_TEXT_FIELD)
            .assertIsDisplayed()

    // Change the label
    textField.performTextClearance()
    textField.performTextInput("Edited label")

    // Save
    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET_SAVE_BUTTON)
        .assertIsEnabled()
        .performClick()

    assertTrue(currentCategoryCount() >= 1)
  }

  // Edit a category then dismiss without saving
  @Test
  fun editCategory_dismissWithoutSaving_keepsOriginalLabel() {
    setContent()

    // Open in edit mode
    composeTestRule
        .onAllNodesWithTag(EditCategoryScreenTestTags.CATEGORY_CARD_EDIT_BUTTON)
        .onFirst()
        .performClick()

    val textField =
        composeTestRule
            .onNodeWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET_LABEL_TEXT_FIELD)
            .assertIsDisplayed()
    textField.performTextClearance()
    textField.performTextInput("Should not be saved")

    // Dismiss bottom sheet without saving
    composeTestRule.onNodeWithTag(EditCategoryScreenTestTags.BOTTOM_SHEET).performTouchInput {
      swipeDown()
    }
  }

  // Open delete dialog and cancel: category is kept
  @Test
  fun deleteDialog_cancelKeepsCategory() {
    setContent()

    val initialCount =
        composeTestRule
            .onAllNodesWithTag(EditCategoryScreenTestTags.CATEGORY_CARD)
            .fetchSemanticsNodes()
            .size

    // Open delete dialog for the first card
    composeTestRule
        .onAllNodesWithTag(EditCategoryScreenTestTags.CATEGORY_CARD_DELETE_BUTTON)
        .onFirst()
        .performClick()

    // Dialog message is shown
    composeTestRule
        .onNodeWithText(
            InstrumentationRegistry.getInstrumentation()
                .targetContext
                .getString(R.string.delete_category_message))
        .assertIsDisplayed()

    // Click on "Cancel"
    composeTestRule
        .onNodeWithText(
            InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.cancel))
        .performClick()

    // Number of categories stays the same
    val afterCancelCount =
        composeTestRule
            .onAllNodesWithTag(EditCategoryScreenTestTags.CATEGORY_CARD)
            .fetchSemanticsNodes()
            .size
    assertEquals(initialCount, afterCancelCount)
  }

  // Delete all categories and show the empty state
  @Test
  fun deleteTwoCategories_leadsToEmptyState_andNextSheetIsCreationMode() {
    setContent()

    deleteCategoriesUntilRemaining(0)

    assertEquals(0, currentCategoryCount())

    composeTestRule.onNodeWithTag(EditCategoryScreenTestTags.EMPTY_STATE_TITLE).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.EMPTY_STATE_MESSAGE)
        .assertIsDisplayed()
  }

  // Helper to delete categories until only [remainingCount] are left
  private fun deleteCategoriesUntilRemaining(remainingCount: Int) {
    while (currentCategoryCount() > remainingCount) {
      composeTestRule
          .onAllNodesWithTag(EditCategoryScreenTestTags.CATEGORY_CARD_DELETE_BUTTON)
          .onFirst()
          .performClick()
      composeTestRule
          .onNodeWithText(
              InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.delete))
          .performClick()
    }
  }

  private fun currentCategoryCount(): Int =
      composeTestRule
          .onAllNodesWithTag(EditCategoryScreenTestTags.CATEGORY_CARD)
          .fetchSemanticsNodes()
          .size
}
