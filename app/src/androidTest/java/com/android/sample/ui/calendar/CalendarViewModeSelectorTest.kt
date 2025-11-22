package com.android.sample.ui.calendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.sample.ui.calendar.components.ViewMode
import com.android.sample.ui.calendar.components.ViewModeSelector
import org.junit.Rule
import org.junit.Test

// Assisted by AI

class CalendarViewModeSelectorTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun initialState_shouldShowFabWithCurrentMode_andHideMenu() {
    composeTestRule.setContent { ViewModeSelector(currentMode = ViewMode.SEVEN_DAYS) }

    // FAB visible for SEVEN_DAYS
    composeTestRule
        .onNodeWithTag(
            CalendarScreenTestTags.VIEW_MODE_SELECTOR_FAB_PREFIX + ViewMode.SEVEN_DAYS.name)
        .assertExists()
        .assertIsDisplayed()

    // Menu items should NOT exist yet
    composeTestRule
        .onNodeWithTag(
            CalendarScreenTestTags.VIEW_MODE_SELECTOR_ITEM_PREFIX + ViewMode.ONE_DAY.name)
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(
            CalendarScreenTestTags.VIEW_MODE_SELECTOR_ITEM_PREFIX + ViewMode.FIVE_DAYS.name)
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(
            CalendarScreenTestTags.VIEW_MODE_SELECTOR_ITEM_PREFIX + ViewMode.SEVEN_DAYS.name)
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(CalendarScreenTestTags.VIEW_MODE_SELECTOR_ITEM_PREFIX + ViewMode.MONTH.name)
        .assertDoesNotExist()
  }

  @Test
  fun clickingFab_shouldOpenMenu_andShowAllOptions() {
    composeTestRule.setContent { ViewModeSelector(currentMode = ViewMode.SEVEN_DAYS) }

    composeTestRule
        .onNodeWithTag(
            CalendarScreenTestTags.VIEW_MODE_SELECTOR_FAB_PREFIX + ViewMode.SEVEN_DAYS.name)
        .performClick()

    // All options visible
    composeTestRule
        .onNodeWithTag(
            CalendarScreenTestTags.VIEW_MODE_SELECTOR_ITEM_PREFIX + ViewMode.ONE_DAY.name)
        .assertExists()
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(
            CalendarScreenTestTags.VIEW_MODE_SELECTOR_ITEM_PREFIX + ViewMode.FIVE_DAYS.name)
        .assertExists()
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(
            CalendarScreenTestTags.VIEW_MODE_SELECTOR_ITEM_PREFIX + ViewMode.SEVEN_DAYS.name)
        .assertExists()
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CalendarScreenTestTags.VIEW_MODE_SELECTOR_ITEM_PREFIX + ViewMode.MONTH.name)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun selectingItem_shouldUpdateFabIcon_andCloseMenu() {
    composeTestRule.setContent {
      var selectedMode by remember { mutableStateOf(ViewMode.SEVEN_DAYS) }

      ViewModeSelector(
          currentMode = selectedMode,
          onModeSelected = { selectedMode = it },
      )
    }

    // Open menu
    composeTestRule
        .onNodeWithTag(
            CalendarScreenTestTags.VIEW_MODE_SELECTOR_FAB_PREFIX + ViewMode.SEVEN_DAYS.name)
        .performClick()

    // Select ONE_DAY
    composeTestRule
        .onNodeWithTag(
            CalendarScreenTestTags.VIEW_MODE_SELECTOR_ITEM_PREFIX + ViewMode.ONE_DAY.name)
        .performClick()

    // Menu should close
    composeTestRule
        .onNodeWithTag(
            CalendarScreenTestTags.VIEW_MODE_SELECTOR_ITEM_PREFIX + ViewMode.ONE_DAY.name)
        .assertDoesNotExist()

    // FAB should now show ONE_DAY
    composeTestRule
        .onNodeWithTag(CalendarScreenTestTags.VIEW_MODE_SELECTOR_FAB_PREFIX + ViewMode.ONE_DAY.name)
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun clickingFabTwice_shouldStayStable_andFabShouldRemainVisible() {
    composeTestRule.setContent { ViewModeSelector(currentMode = ViewMode.SEVEN_DAYS) }

    val fabTag = CalendarScreenTestTags.VIEW_MODE_SELECTOR_FAB_PREFIX + ViewMode.SEVEN_DAYS.name

    // Click 1 — open
    composeTestRule.onNodeWithTag(fabTag).performClick()

    // Click 2 — may stay open or update state depending on platform,
    // but at minimum it must not crash and the FAB must stay visible.
    composeTestRule.onNodeWithTag(fabTag).performClick()

    composeTestRule.onNodeWithTag(fabTag).assertExists().assertIsDisplayed()
  }
}
