package com.android.sample.ui.filters

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.sample.ui.calendar.filters.LocationFilterScreen
import com.android.sample.ui.calendar.filters.LocationFilterTestTags
import com.android.sample.ui.calendar.filters.components.FilterCheckboxTestTags
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Test suite for LocationFilterScreen. */
class LocationFilterScreenTest {

  @get:Rule val compose = createComposeRule()

  private val locations = listOf("Salle 1", "Salle 2", "Unknown")

  // -------------------------------
  // 1. Screen loads correctly
  // -------------------------------
  @Test
  fun locationFilterScreen_rendersCorrectly() {
    compose.setContent { LocationFilterScreen(selected = emptyList(), onBack = {}, onApply = {}) }

    compose.onNodeWithTag(LocationFilterTestTags.SCREEN).assertExists()
    compose.onNodeWithTag(LocationFilterTestTags.HEADER).assertExists()
    compose.onNodeWithTag(LocationFilterTestTags.LIST).assertExists()

    // Each item exists
    locations.forEach { loc ->
      compose.onNodeWithTag(LocationFilterTestTags.ITEM_PREFIX + loc).assertExists()
    }
  }

  // -------------------------------
  // 2. Clicking checkbox toggles selection
  // -------------------------------
  @Test
  fun locationFilterScreen_checkbox_togglesSelection() {
    var appliedList: List<String> = emptyList()

    compose.setContent {
      LocationFilterScreen(selected = emptyList(), onBack = {}, onApply = { appliedList = it })
    }

    val key = "Salle 1"
    val itemTag = FilterCheckboxTestTags.PREFIX + key
    val boxTag = itemTag + "_Box"

    // Initial Off
    compose.onNodeWithTag(boxTag).assertIsOff()

    // Toggle ON
    compose.onNodeWithTag(boxTag).performClick()
    compose.onNodeWithTag(boxTag).assertIsOn()

    // Apply → should contain Salle 1
    compose.onNodeWithTag(LocationFilterTestTags.APPLY_BUTTON).performClick()
    assertTrue(appliedList.contains(key))

    // Toggle OFF
    compose.onNodeWithTag(boxTag).performClick()
    compose.onNodeWithTag(boxTag).assertIsOff()

    // Apply → should NOT contain Salle 1
    compose.onNodeWithTag(LocationFilterTestTags.APPLY_BUTTON).performClick()
    assertFalse(appliedList.contains(key))
  }

  // -------------------------------
  // 3. Clear button removes all selections
  // -------------------------------
  @Test
  fun locationFilterScreen_clearButton_clearsSelections() {
    compose.setContent {
      LocationFilterScreen(selected = listOf("Salle 1", "Salle 2"), onBack = {}, onApply = {})
    }

    // Both should start checked
    locations.take(2).forEach { loc ->
      val boxTag = FilterCheckboxTestTags.PREFIX + loc + "_Box"
      compose.onNodeWithTag(boxTag).assertIsOn()
    }

    // Click clear
    compose.onNodeWithTag(LocationFilterTestTags.CLEAR_BUTTON).performClick()

    // All should now be unchecked
    locations.forEach { loc ->
      val boxTag = FilterCheckboxTestTags.PREFIX + loc + "_Box"
      compose.onNodeWithTag(boxTag).assertIsOff()
    }
  }

  // -------------------------------
  // 4. Back button triggers callback
  // -------------------------------
  @Test
  fun locationFilterScreen_backButton_callsOnBack() {
    var backCalled = false

    compose.setContent {
      LocationFilterScreen(selected = emptyList(), onBack = { backCalled = true }, onApply = {})
    }

    compose.onNodeWithTag(LocationFilterTestTags.BACK_BUTTON).performClick()

    assertTrue(backCalled)
  }
}
