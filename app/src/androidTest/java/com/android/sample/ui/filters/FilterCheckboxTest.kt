package com.android.sample.ui.filters

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.ui.calendar.filters.components.FilterCheckbox
import com.android.sample.ui.calendar.filters.components.FilterCheckboxTestTags
import org.junit.Rule
import org.junit.Test

// Assisted by AI
/** Test class for FilterCheckbox composable. */
class FilterCheckboxTest {

  @get:Rule val compose = createComposeRule()

  /** Test to verify that the FilterCheckbox toggles selection correctly. */
  @Test
  fun checkbox_togglesSelectionCorrectly() {
    val key = "Course"
    val parentTag = FilterCheckboxTestTags.PREFIX + key
    val boxTag = parentTag + "_Box"

    var isChecked by mutableStateOf(false)

    compose.setContent {
      FilterCheckbox(
          label = "Course", isChecked = isChecked, onCheckedChange = { isChecked = it }, key = key)
    }

    // Off
    compose.onNodeWithTag(boxTag).assertIsOff()

    // click → On
    compose.onNodeWithTag(boxTag).performClick()
    compose.waitForIdle()
    assert(isChecked)
    compose.onNodeWithTag(boxTag).assertIsOn()

    // click again → Off
    compose.onNodeWithTag(boxTag).performClick()
    compose.waitForIdle()
    assert(!isChecked)
    compose.onNodeWithTag(boxTag).assertIsOff()
  }
}
