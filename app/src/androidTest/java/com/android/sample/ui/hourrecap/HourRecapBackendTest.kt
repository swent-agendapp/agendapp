package com.android.sample.ui.hourrecap

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import com.android.sample.ui.calendar.CalendarViewModel
import org.junit.Rule
import org.junit.Test

/**
 * Test class for the HourRecapScreen composable using real CalendarViewModel, but injecting test
 * data into its UI state.
 */
class HourRecapBackendTest {

  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun recapItems_displayWorkedHoursCorrectly() {
    val calendarViewModel = CalendarViewModel()

    calendarViewModel.setTestWorkedHours(
        listOf(
            "Alice" to 12.5,
            "Bob" to 8.0,
        ))

    compose.setContent { HourRecapScreen(calendarViewModel = calendarViewModel) }
    compose.onNodeWithText("Alice").assertExists()
    compose.onNodeWithText("12.5h").assertExists()

    compose.onNodeWithText("Bob").assertExists()
    compose.onNodeWithText("8.0h").assertExists()

    // Only 2 recap item
    compose.onAllNodesWithTag(HourRecapTestTags.RECAP_ITEM).assertCountEquals(2)
  }
}
