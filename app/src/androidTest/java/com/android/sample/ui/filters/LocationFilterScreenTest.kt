package com.android.sample.ui.filters

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.android.sample.ui.calendar.filters.FilterListScreen
import com.android.sample.ui.calendar.filters.components.FilterCheckboxTestTags
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// Assisted by AI
/** Test suite for LocationFilterScreen. */
class LocationFilterScreenTest {

  @get:Rule val compose = createComposeRule()

  private val locations = listOf("Salle 1", "Salle 2", "Unknown")

  // -------------------------------
  // 1. Screen loads correctly
  // -------------------------------
  @Test
  fun locationFilterScreen_rendersCorrectly() {
    compose.setContent {
      FilterListScreen(
          title = "Location",
          items = locations,
          selected = emptyList(),
          onBack = {},
          onApply = {},
          testTagPrefix = "LocationFilter")
    }

    compose.onNodeWithTag("LocationFilter_Screen").assertExists()
    compose.onNodeWithTag("LocationFilter_Header").assertExists()
    compose.onNodeWithTag("LocationFilter_List").assertExists()

    locations.forEach { loc -> compose.onNodeWithTag("LocationFilter_Item_$loc").assertExists() }
  }

  // -------------------------------
  // 2. Clicking checkbox toggles selection
  // -------------------------------
  @Test
  fun locationFilterScreen_checkbox_togglesSelection() {
    var appliedList: List<String> = emptyList()

    compose.setContent {
      FilterListScreen(
          title = "Location",
          items = locations,
          selected = emptyList(),
          onBack = {},
          onApply = { appliedList = it },
          testTagPrefix = "LocationFilter")
    }

    val key = "Salle 1"
    val itemTag = "LocationFilter_Item_$key"
    val checkboxTag = FilterCheckboxTestTags.PREFIX + key + "_Box"

    compose.onNodeWithTag(itemTag).assertExists()

    // Initially unchecked
    compose.onNodeWithTag(checkboxTag).assertIsOff()

    // Toggle ON
    compose.onNodeWithTag(checkboxTag).performClick()
    compose.onNodeWithTag(checkboxTag).assertIsOn()

    // Apply -> should contain key
    compose.onNodeWithTag("LocationFilter_Apply").performClick()
    assertTrue(appliedList.contains(key))

    // Toggle OFF
    compose.onNodeWithTag(checkboxTag).performClick()
    compose.onNodeWithTag(checkboxTag).assertIsOff()

    // Apply -> should NOT contain key
    compose.onNodeWithTag("LocationFilter_Apply").performClick()
    assertFalse(appliedList.contains(key))
  }

  // -------------------------------
  // 3. Clear button removes all selections
  // -------------------------------
  @Test
  fun locationFilterScreen_clearButton_clearsSelections() {
    compose.setContent {
      FilterListScreen(
          title = "Location",
          items = locations,
          selected = listOf("Salle 1", "Salle 2"),
          onBack = {},
          onApply = {},
          testTagPrefix = "LocationFilter")
    }

    // Both selected initially
    listOf("Salle 1", "Salle 2").forEach { loc ->
      val tag = FilterCheckboxTestTags.PREFIX + loc + "_Box"
      compose.onNodeWithTag(tag).assertIsOn()
    }

    // Click CLEAR
    compose.onNodeWithTag("LocationFilter_Clear").performClick()

    // All unchecked
    locations.forEach { loc ->
      val tag = FilterCheckboxTestTags.PREFIX + loc + "_Box"
      compose.onNodeWithTag(tag).assertIsOff()
    }
  }

  // -------------------------------
  // 4. Back button triggers callback
  // -------------------------------
  @Test
  fun locationFilterScreen_backButton_callsOnBack() {
    var backCalled = false

    compose.setContent {
      FilterListScreen(
          title = "Location",
          items = locations,
          selected = emptyList(),
          onBack = { backCalled = true },
          onApply = {},
          testTagPrefix = "LocationFilter")
    }

    compose.onNodeWithTag("LocationFilter_BackButton").performClick()
    assertTrue(backCalled)
  }
}
