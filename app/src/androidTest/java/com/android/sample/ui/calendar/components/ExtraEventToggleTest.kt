package com.android.sample.ui.calendar.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.R
import org.junit.Rule
import org.junit.Test

class ExtraEventToggleTest {

  @get:Rule val composeRule = createComposeRule()

  @Test
  fun toggleUpdatesDescription() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val offText = context.getString(R.string.extra_event_description_off)
    val onText = context.getString(R.string.extra_event_description_on)

    composeRule.setContent {
      val (isExtra, setIsExtra) = remember { mutableStateOf(false) }
      ExtraEventToggle(
          isExtra = isExtra,
          onToggle = setIsExtra,
          toggleTestTag = "toggle",
          descriptionTestTag = "description",
      )
    }

    composeRule.onNodeWithTag("description").assertTextEquals(offText)
    composeRule.onNodeWithTag("toggle").performClick()
    composeRule.waitForIdle()
    composeRule.onNodeWithTag("description").assertTextEquals(onText)
  }
}
