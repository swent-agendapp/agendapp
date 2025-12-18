package com.android.sample.ui.category

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.R
import com.android.sample.model.category.EventCategory
import com.android.sample.ui.category.components.CategoryCard
import com.android.sample.ui.category.components.CategoryListSection
import com.android.sample.ui.category.components.ColorCircleTestTags
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Assisted by AI

/**
 * Tests for CategoryCard (part of the Category list screen).
 *
 * Conventions:
 * - We use tags declared in [EditCategoryScreenTestTags] and [ColorCircleTestTags] to target key UI
 *   elements.
 */
@RunWith(AndroidJUnit4::class)
class CategoryCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  // ---------- State / setup ----------

  private lateinit var testCategory: EventCategory
  private var editClicked: Boolean = false
  private var deleteClicked: Boolean = false

  @Before
  fun setUp() {
    testCategory = category()
    editClicked = false
    deleteClicked = false
  }

  // ---------- Helpers ----------

  private fun category(
      label: String = "My category",
      isDefault: Boolean = false,
  ): EventCategory = EventCategory.defaultCategory().copy(label = label, isDefault = isDefault)

  private fun defaultLabelString(): String {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    return context.getString(R.string.default_category_label)
  }

  private fun setContent(
      category: EventCategory = testCategory,
      onEditClick: () -> Unit = {},
      onDeleteClick: () -> Unit = {},
      isDragging: Boolean = false,
  ) {
    composeTestRule.setContent {
      CategoryCard(
          isDragging = isDragging,
          category = category,
          onEditClick = onEditClick,
          onDeleteClick = onDeleteClick,
      )
    }
  }

  // ---------- Tests ----------

  @Test
  fun allComponents_areDisplayed() {
    val cat = category(label = "My category", isDefault = false)

    setContent(category = cat)

    // Card
    composeTestRule.onNodeWithTag(EditCategoryScreenTestTags.CATEGORY_CARD).assertIsDisplayed()

    // Color circle
    composeTestRule.onNodeWithTag(ColorCircleTestTags.COLOR_CIRCLE).assertIsDisplayed()

    // Label
    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.CATEGORY_CARD_LABEL)
        .assertIsDisplayed()
        .assert(hasText("My category"))

    // Edit button
    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.CATEGORY_CARD_EDIT_BUTTON)
        .assertIsDisplayed()

    // Delete button
    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.CATEGORY_CARD_DELETE_BUTTON)
        .assertIsDisplayed()
  }

  @Test
  fun defaultCategory_showsDefaultLabel() {
    val cat = category(label = "Custom label", isDefault = true)
    val expectedLabel = defaultLabelString()

    setContent(category = cat)

    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.CATEGORY_CARD_LABEL)
        .assertIsDisplayed()
        .assert(hasText(expectedLabel))
  }

  @Test
  fun nonDefaultCategory_showsOwnLabel() {
    val cat = category(label = "Custom category", isDefault = false)

    setContent(category = cat)

    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.CATEGORY_CARD_LABEL)
        .assertIsDisplayed()
        .assert(hasText("Custom category"))
  }

  @Test
  fun editButton_callsOnEditClick() {
    val cat = category()

    setContent(
        category = cat,
        onEditClick = { editClicked = true },
    )

    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.CATEGORY_CARD_EDIT_BUTTON)
        .performClick()

    assertTrue(editClicked)
  }

  @Test
  fun deleteButton_callsOnDeleteClick() {
    val cat = category()

    setContent(
        category = cat,
        onDeleteClick = { deleteClicked = true },
    )

    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.CATEGORY_CARD_DELETE_BUTTON)
        .performClick()

    assertTrue(deleteClicked)
  }
}

/** Tests for CategoryListSection (the LazyColumn containing the list of categories). */
@RunWith(AndroidJUnit4::class)
class CategoryListSectionTest {

  @get:Rule val composeTestRule = createComposeRule()

  // ---------- Helpers ----------

  private fun categories(count: Int = 3): List<EventCategory> =
      (1..count).map { index ->
        EventCategory.defaultCategory()
            .copy(id = "id-$index", label = "Category $index", isDefault = false)
      }

  private fun emptyTitleString(): String {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    return context.getString(R.string.edit_category_empty_state_title)
  }

  private fun emptyMessageString(): String {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    return context.getString(R.string.edit_category_empty_state_message)
  }

  private fun setContentWithCategories(
      categories: List<EventCategory>,
      isLoading: Boolean = false,
  ) {
    composeTestRule.setContent {
      val reorderState =
          rememberReorderableLazyListState(
              onMove = { _, _ -> },
          )

      CategoryListSection(
          modifier = Modifier,
          categories = categories,
          reorderState = reorderState,
          onEditCategory = {},
          onDeleteCategory = {},
          isLoading = isLoading,
      )
    }
  }

  // ---------- Tests ----------

  @Test
  fun listWithCategories_displaysListAndCategoryCards() {
    val categories = categories(count = 3)

    setContentWithCategories(categories)

    // The LazyColumn list itself
    composeTestRule.onNodeWithTag(EditCategoryScreenTestTags.CATEGORY_LIST).assertIsDisplayed()

    // One CategoryCard per category (using CATEGORY_CARD tag)
    composeTestRule
        .onAllNodesWithTag(EditCategoryScreenTestTags.CATEGORY_CARD)
        .assertCountEquals(categories.size)
  }

  @Test
  fun emptyCategories_showsEmptyStateMessages() {
    setContentWithCategories(emptyList())

    val expectedTitle = emptyTitleString()
    val expectedMessage = emptyMessageString()

    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.EMPTY_STATE_TITLE)
        .assertIsDisplayed()
        .assert(hasText(expectedTitle))

    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.EMPTY_STATE_MESSAGE)
        .assertIsDisplayed()
        .assert(hasText(expectedMessage))
  }

  @Test
  fun loading_isDisplayed_whenIsLoadingTrue() {
    // Categories can be anything; we just want to verify the loading indicator is shown.
    setContentWithCategories(categories(count = 2), isLoading = true)

    composeTestRule.onNodeWithTag(EditCategoryScreenTestTags.LOADING).assertIsDisplayed()
  }

  @Test
  fun loading_doesNotExist_whenIsLoadingFalse() {
    setContentWithCategories(categories(count = 2), isLoading = false)

    composeTestRule.onNodeWithTag(EditCategoryScreenTestTags.LOADING).assertDoesNotExist()
  }

  @Test
  fun loading_hidesEmptyState_whenCategoriesEmpty() {
    // When loading, the empty state must not be displayed.
    setContentWithCategories(emptyList(), isLoading = true)

    composeTestRule.onNodeWithTag(EditCategoryScreenTestTags.LOADING).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EditCategoryScreenTestTags.EMPTY_STATE_TITLE).assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(EditCategoryScreenTestTags.EMPTY_STATE_MESSAGE)
        .assertDoesNotExist()
  }
}
