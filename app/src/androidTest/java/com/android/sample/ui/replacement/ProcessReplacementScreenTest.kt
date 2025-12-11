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
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import com.android.sample.model.replacement.mockData.getMockReplacements
import com.android.sample.ui.theme.SampleAppTheme
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.OrganizationTestHelper
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProcessReplacementScreenTest : FirebaseEmulatedTest() {

  @get:Rule val composeTestRule = createComposeRule()

  private val replacementId = getMockReplacements().first().id

  @Before
  override fun setUp() = runBlocking {
    super.setUp()

    val orgId = "orgTest"
    val helper = OrganizationTestHelper()
    helper.setupOrganizationWithUsers(orgId)
    Unit
  }

    @Test
  fun screen_displaysBasicElements() {
    composeTestRule.setContent {
      SampleAppTheme { ProcessReplacementScreen(replacementId = replacementId) }
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
      SampleAppTheme { ProcessReplacementScreen(replacementId = replacementId) }
    }

    composeTestRule.onNodeWithTag(ProcessReplacementTestTags.SEND_BUTTON).assertIsNotEnabled()

    composeTestRule
        .onNodeWithTag(ProcessReplacementTestTags.SELECTED_SUMMARY)
        .assertIsDisplayed()
        .assert(hasTextExactly(noneText))
  }

  @Test
  fun selectingMembers_andEnablesButton_andCallsCallback() {
    var sentMembers: List<User>? = null

    composeTestRule.setContent {
      SampleAppTheme {
        ProcessReplacementScreen(
            replacementId = replacementId,
            onSendRequests = { sentMembers = it },
            candidates = listOf(
              User(id = "1", displayName = "Alice", email = "alice@example.com"),
              User(id = "2", displayName = "Bob", email = "bob@example.com"),
              User(id = "3", displayName = "Charlie", email = "charlie@example.com")
            )
        )
      }
    }

    composeTestRule
        .onNodeWithTag(ProcessReplacementTestTags.memberTag("Alice"), useUnmergedTree = true)
        .performClick()

    composeTestRule.onNodeWithTag(ProcessReplacementTestTags.SEND_BUTTON).assertIsEnabled()

    composeTestRule.onNodeWithTag(ProcessReplacementTestTags.SELECTED_SUMMARY).assertIsDisplayed()
    composeTestRule.onNodeWithText("Alice").assertIsDisplayed()

    composeTestRule.onNodeWithTag(ProcessReplacementTestTags.SEND_BUTTON).performClick()

    assertTrue(sentMembers != null)
    assertEquals(listOf("1"), sentMembers?.map { it.id })
  }

  @Test
  fun searchFilter_filtersList() {
    composeTestRule.setContent {
      SampleAppTheme {
        ProcessReplacementScreen(
          replacementId = replacementId,
          candidates = listOf(
            User(id = "1", displayName = "Alice", email = "alice@example.com"),
            User(id = "2", displayName = "Bob", email = "bob@example.com"),
            User(id = "3", displayName = "Charlie", email = "charlie@example.com")
          )
        )
      }
    }

    composeTestRule
        .onNodeWithTag(ProcessReplacementTestTags.SEARCH_BAR)
        .performClick()
        .performTextInput("Bob")

    composeTestRule
        .onNodeWithTag(ProcessReplacementTestTags.memberTag("Bob"), useUnmergedTree = true)
        .assertExists()
    composeTestRule
        .onNodeWithTag(ProcessReplacementTestTags.memberTag("Alice"), useUnmergedTree = true)
        .assertDoesNotExist()
  }
}
