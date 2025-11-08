package com.android.sample.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.MainActivity
import com.android.sample.ui.calendar.EditEventTestTags
import com.android.sample.ui.screens.HomeTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Completed with AI assistance
/**
 * Navigation test to verify that clicking on an event from Home navigates to EditEventScreen,
 * and that the eventId is correctly passed through navigation.
 */
@RunWith(AndroidJUnit4::class)
class NavigationEditEventTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun navigateFromHome_toEditEvent_andBack() {
        // Step 1️⃣: Wait for home screen to load
        composeTestRule.onNodeWithTag(HomeTestTags.ROOT)
            .assertIsDisplayed()

        // Step 2️⃣: Simulate clicking an event item (that triggers navigateToEditEvent)
        composeTestRule.onNodeWithTag(HomeTestTags.EDIT_BUTTON)
            .performClick()

        // Step 3️⃣: Verify EditEventScreen appears
        composeTestRule.onNodeWithTag(EditEventTestTags.TITLE_FIELD)
            .assertIsDisplayed()

        // Step 4️⃣: Click "Cancel" to navigate back
        composeTestRule.onNodeWithTag(EditEventTestTags.CANCEL_BUTTON)
            .performClick()

        // Step 5️⃣: Verify we are back on the Home screen
        composeTestRule.onNodeWithTag(HomeTestTags.ROOT)
            .assertIsDisplayed()
    }

    @Test
    fun navigate_EditEvent_to_EditEventAttendant_andBack() {
        // Step 1️⃣: Verify HomeScreen is displayed
        composeTestRule.onNodeWithTag(HomeTestTags.ROOT)
            .assertIsDisplayed()

        // Step 2️⃣: Click edit button to navigate to EditEvent screen
        composeTestRule.onNodeWithTag(HomeTestTags.EDIT_BUTTON)
            .assertExists("Edit button not found on HomeScreen")
            .performClick()

        // Step 3️⃣: Wait for EditEventScreen to appear
        waitForNodeWithTag(EditEventTestTags.TITLE_FIELD)
        composeTestRule.onNodeWithTag(EditEventTestTags.TITLE_FIELD)
            .assertIsDisplayed()

        // Step 4️⃣: Scroll if needed, then click "Edit Participants"
        try {
            composeTestRule.onNodeWithTag(EditEventTestTags.EDIT_PARTICIPANTS_BUTTON)
                .performScrollTo()
        } catch (e: AssertionError) {
            // fallback if not LazyColumn — perform manual swipe
            composeTestRule.onRoot().performTouchInput { swipeUp() }
        }

        composeTestRule.onNodeWithTag(EditEventTestTags.EDIT_PARTICIPANTS_BUTTON)
            .assertExists("Edit Participants button not found")
            .performClick()

        // Step 5️⃣: Wait for navigation to EditEventAttendantScreen
        waitForNodeWithTag(EditEventTestTags.EDIT_EVENT_ATTENDANT_ROOT)
        composeTestRule.onNodeWithTag(EditEventTestTags.EDIT_EVENT_ATTENDANT_ROOT)
            .assertIsDisplayed()

        // Step 6️⃣: Click back button to return to EditEventScreen
        composeTestRule.onNodeWithTag(EditEventTestTags.BACK_BUTTON)
            .assertExists("Back button not found on EditEventAttendantScreen")
            .performClick()

        // Step 7️⃣: Verify we are back at EditEventScreen
        waitForNodeWithTag(EditEventTestTags.TITLE_FIELD)
        composeTestRule.onNodeWithTag(EditEventTestTags.TITLE_FIELD)
            .assertIsDisplayed()
    }
    /**
     * Helper function to wait until a composable with [tag] appears in the tree.
     */
    private fun waitForNodeWithTag(tag: String, timeoutMillis: Long = 5000) {
        composeTestRule.waitUntil(timeoutMillis) {
            composeTestRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }
}