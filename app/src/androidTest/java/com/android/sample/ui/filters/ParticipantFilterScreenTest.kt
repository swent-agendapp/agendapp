package com.android.sample.ui.filters

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.ui.calendar.filters.ParticipantFilterScreen
import com.android.sample.ui.calendar.filters.ParticipantFilterTestTags
import com.android.sample.ui.calendar.filters.components.FilterCheckboxTestTags
import org.junit.Rule
import org.junit.Test

/** Test suite for ParticipantFilterScreen. */
class ParticipantFilterScreenTest {

  @get:Rule val compose = createComposeRule()

  private val participants = listOf("Alice", "Bob", "Charlie")

  // Reuse only first three for simple testing

  /** Test that the screen loads and essential UI elements exist. */
  @Test
  fun participantFilterScreen_displaysUIElements() {
    compose.setContent {
      ParticipantFilterScreen(selected = emptyList(), onBack = {}, onApply = {})
    }

    compose.onNodeWithTag(ParticipantFilterTestTags.SCREEN).assertExists()
    compose.onNodeWithTag(ParticipantFilterTestTags.HEADER).assertExists()
    compose.onNodeWithTag(ParticipantFilterTestTags.LIST).assertExists()

    // First item exists
    compose
        .onNodeWithTag(ParticipantFilterTestTags.ITEM_PREFIX + participants.first())
        .assertExists()
  }

  /** Test checkbox toggling state correctly. */
  @Test
  fun participantFilterScreen_checkbox_togglesCorrectly() {
    compose.setContent {
      ParticipantFilterScreen(selected = emptyList(), onBack = {}, onApply = {})
    }

    val person = participants.first()
    val boxTag = FilterCheckboxTestTags.PREFIX + person + "_Box"

    // Checkbox initially off
    compose.onNodeWithTag(boxTag).assertIsOff()

    // Click → ON
    compose.onNodeWithTag(boxTag).performClick()
    compose.onNodeWithTag(boxTag).assertIsOn()

    // Click → OFF
    compose.onNodeWithTag(boxTag).performClick()
    compose.onNodeWithTag(boxTag).assertIsOff()
  }

  /** Test that "Clear" removes all selections. */
  @Test
  fun participantFilterScreen_clear_clearsSelections() {
    compose.setContent {
      ParticipantFilterScreen(
          selected = participants, // all preselected
          onBack = {},
          onApply = {})
    }

    // All should be ON initially
    participants.forEach { p ->
      val boxTag = FilterCheckboxTestTags.PREFIX + p + "_Box"
      compose.onNodeWithTag(boxTag).assertIsOn()
    }

    // Click Clear
    compose.onNodeWithTag(ParticipantFilterTestTags.CLEAR).performClick()

    // All should be OFF now
    participants.forEach { p ->
      val boxTag = FilterCheckboxTestTags.PREFIX + p + "_Box"
      compose.onNodeWithTag(boxTag).assertIsOff()
    }
  }

  /** Test that Apply returns the correct selected participants. */
  @Test
  fun participantFilterScreen_apply_returnsCorrectSelections() {
    var appliedList: List<String>? = null

    compose.setContent {
      ParticipantFilterScreen(selected = emptyList(), onBack = {}, onApply = { appliedList = it })
    }

    // Select Alice + Bob
    listOf("Alice", "Bob").forEach { p ->
      val boxTag = FilterCheckboxTestTags.PREFIX + p + "_Box"
      compose.onNodeWithTag(boxTag).performClick()
    }

    // Click Apply
    compose.onNodeWithTag(ParticipantFilterTestTags.APPLY).performClick()

    assert(appliedList == listOf("Alice", "Bob"))
  }
}
