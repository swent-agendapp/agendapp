package com.android.sample.ui.replacement

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.R
import com.android.sample.model.authentication.User
import com.android.sample.model.replacement.mockData.getMockReplacements
import com.android.sample.ui.theme.SampleAppTheme
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProcessReplacementScreenTest : FirebaseEmulatedTest(), RequiresSelectedOrganizationTestBase {

    @get:Rule val composeTestRule = createComposeRule()

    override val organizationId: String = DEFAULT_TEST_ORG_ID

    private val replacement = getMockReplacements().first()

    private val candidates =
        listOf(
            User(id = "1", displayName = "Alice", email = "alice@example.com"),
            User(id = "2", displayName = "Bob", email = "bob@example.com"),
            User(id = "3", displayName = "Charlie", email = "charlie@example.com"),
        )

    @Before
    override fun setUp() {
        super.setUp()
        setSelectedOrganization()
    }

    @Test
    fun screen_displaysBasicElements() {
        composeTestRule.setContent {
            SampleAppTheme { ProcessReplacementScreen(replacement = replacement, candidates = candidates) }
        }

        composeTestRule
            .onNodeWithTag(ProcessReplacementTestTags.ROOT, useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(ProcessReplacementTestTags.SEARCH_BAR, useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(ProcessReplacementTestTags.MEMBER_LIST, useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(ProcessReplacementTestTags.SELECTED_SUMMARY, useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(ProcessReplacementTestTags.SEND_BUTTON, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun noSelection_buttonDisabled_andSummaryShowsNone() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val noneText = context.getString(R.string.replacement_selected_members_none)

        composeTestRule.setContent {
            SampleAppTheme { ProcessReplacementScreen(replacement = replacement, candidates = candidates) }
        }

        composeTestRule.onNodeWithTag(ProcessReplacementTestTags.SEND_BUTTON).assertIsNotEnabled()

        composeTestRule
            .onNodeWithTag(ProcessReplacementTestTags.SELECTED_SUMMARY)
            .assertIsDisplayed()
            .assert(hasTextExactly(noneText))
    }

    @Test
    fun selectingMembers_enablesButton_andCallsCallback() {
        var sentMembers: List<User>? = null

        composeTestRule.setContent {
            SampleAppTheme {
                ProcessReplacementScreen(
                    replacement = replacement,
                    candidates = candidates,
                    onSendRequests = { sentMembers = it },
                )
            }
        }

        composeTestRule
            .onNodeWithTag(ProcessReplacementTestTags.memberTag(User(id = "1", displayName = "Alice", email = "alice@example.com")), useUnmergedTree = true)
            .performClick()

        composeTestRule.onNodeWithTag(ProcessReplacementTestTags.SEND_BUTTON).assertIsEnabled()
        composeTestRule.onNodeWithText("Alice").assertIsDisplayed()

        composeTestRule.onNodeWithTag(ProcessReplacementTestTags.SEND_BUTTON).performClick()

        assertNotNull(sentMembers)
        assertEquals(listOf("1"), sentMembers!!.map { it.id })
    }

    @Test
    fun searchFilter_filtersList() {
        composeTestRule.setContent {
            SampleAppTheme { ProcessReplacementScreen(replacement = replacement, candidates = candidates) }
        }

        composeTestRule
            .onNodeWithTag(ProcessReplacementTestTags.SEARCH_BAR)
            .performClick()
            .performTextInput("Bob")

        composeTestRule
            .onNodeWithTag(ProcessReplacementTestTags.memberTag(User(id = "2", displayName = "Bob", email = "bob@example.com")), useUnmergedTree = true)
            .assertExists()

        composeTestRule
            .onNodeWithTag(ProcessReplacementTestTags.memberTag(User(id = "1", displayName = "Alice", email = "alice@example.com")), useUnmergedTree = true)
            .assertDoesNotExist()
    }
}
