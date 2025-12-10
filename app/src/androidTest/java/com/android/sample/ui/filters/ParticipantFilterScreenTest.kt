package com.android.sample.ui.filters

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.ui.calendar.filters.FilterListScreen
import com.android.sample.ui.calendar.filters.components.FilterCheckboxTestTags
import org.junit.Rule
import org.junit.Test

// Assisted by AI
/** Test suite for ParticipantFilterScreen. */
class ParticipantFilterScreenTest {

  @get:Rule val compose = createComposeRule()

  private val participants = listOf("Alice", "Bob", "Charlie")

  // -------------------------------
  // 1. Screen loads and essential UI appears
  // -------------------------------
  @Test
  fun participantFilterScreen_displaysUIElements() {
    compose.setContent {
      FilterListScreen(
          title = "Participants",
          items = participants,
          selected = emptyList(),
          onBack = {},
          onApply = {},
          testTagPrefix = "ParticipantFilter")
    }

    compose.onNodeWithTag("ParticipantFilter_Screen").assertExists()
    compose.onNodeWithTag("ParticipantFilter_Header").assertExists()
    compose.onNodeWithTag("ParticipantFilter_List").assertExists()

    // Check first item exists
    compose.onNodeWithTag("ParticipantFilter_Item_${participants.first()}").assertExists()
  }

  // -------------------------------
  // 2. Checkbox toggles correctly
  // -------------------------------
  @Test
  fun participantFilterScreen_checkbox_togglesCorrectly() {
    compose.setContent {
      FilterListScreen(
          title = "Participants",
          items = participants,
          selected = emptyList(),
          onBack = {},
          onApply = {},
          testTagPrefix = "ParticipantFilter")
    }

    val person = participants.first()
    val checkboxTag = FilterCheckboxTestTags.PREFIX + person + "_Box"

    compose.onNodeWithTag(checkboxTag).assertIsOff()

    // On
    compose.onNodeWithTag(checkboxTag).performClick()
    compose.onNodeWithTag(checkboxTag).assertIsOn()

    // Off
    compose.onNodeWithTag(checkboxTag).performClick()
    compose.onNodeWithTag(checkboxTag).assertIsOff()
  }

  // -------------------------------
  // 3. Clear removes all selections
  // -------------------------------
  @Test
  fun participantFilterScreen_clear_clearsSelections() {
    compose.setContent {
      FilterListScreen(
          title = "Participants",
          items = participants,
          selected = participants, // all ON initially
          onBack = {},
          onApply = {},
          testTagPrefix = "ParticipantFilter")
    }

    // All ON initially
    participants.forEach { p ->
      val tag = FilterCheckboxTestTags.PREFIX + p + "_Box"
      compose.onNodeWithTag(tag).assertIsOn()
    }

    // Clear
    compose.onNodeWithTag("ParticipantFilter_Clear").performClick()

    // All OFF
    participants.forEach { p ->
      val tag = FilterCheckboxTestTags.PREFIX + p + "_Box"
      compose.onNodeWithTag(tag).assertIsOff()
    }
  }

  // -------------------------------
  // 4. Apply returns correct selected list
  // -------------------------------
  @Test
  fun participantFilterScreen_apply_returnsCorrectSelections() {
    var appliedList: List<String>? = null

    compose.setContent {
      FilterListScreen(
          title = "Participants",
          items = participants,
          selected = emptyList(),
          onBack = {},
          onApply = { appliedList = it },
          testTagPrefix = "ParticipantFilter")
    }

    // Select Alice and Bob
    listOf("Alice", "Bob").forEach { p ->
      val checkboxTag = FilterCheckboxTestTags.PREFIX + p + "_Box"
      compose.onNodeWithTag(checkboxTag).performClick()
    }

    // Apply
    compose.onNodeWithTag("ParticipantFilter_Apply").performClick()

    assert(appliedList == listOf("Alice", "Bob"))
  }
}
