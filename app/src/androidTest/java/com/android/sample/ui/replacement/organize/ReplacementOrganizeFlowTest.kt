package com.android.sample.ui.replacement.organize

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReplacementOrganizeFlowTest : FirebaseEmulatedTest(), RequiresSelectedOrganizationTestBase {

  @get:Rule val composeTestRule = createComposeRule()

  override val organizationId: String = DEFAULT_TEST_ORG_ID

  private lateinit var fakeViewModel: ReplacementOrganizeViewModel

  @Before
  override fun setUp() {
    super.setUp()
    setSelectedOrganization()
    fakeViewModel = ReplacementOrganizeViewModel()
    composeTestRule.setContent {
      ReplacementOrganizeScreen(
          replacementOrganizeViewModel = fakeViewModel,
          onCancel = {},
          onProcessNow = {},
          onProcessLater = {})
    }
  }

  @Test
  fun fullFlow_displaysCorrectScreensAndNavigates() {

    // Add Alice to participants to verify she appears in the list
    runBlocking {
      val user = User(id = "1", displayName = "Alice", email = "alice@example.com")
      UserRepositoryProvider.repository.newUser(user)
      UserRepositoryProvider.repository.addUserToOrganization(user.id, organizationId)
      fakeViewModel.loadOrganizationMembers()
    }

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      fakeViewModel.uiState.value.memberList.isNotEmpty()
    }

    val members = fakeViewModel.uiState.value.memberList
    val alice = members.first { it.email == "alice@example.com" }
    val aliceLabel = alice.displayName ?: alice.email ?: alice.id

    val selectEventsText = "Select the events for which $aliceLabel needs a replacement"
    val selectDateRangeText = "Select the date range for which $aliceLabel needs a replacement"

    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.MEMBER_LIST).assertIsDisplayed()
    composeTestRule.onNodeWithText(aliceLabel).performClick()

    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.SELECT_EVENT_BUTTON).assertIsEnabled()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SELECT_DATE_RANGE_BUTTON)
        .assertIsEnabled()

    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.SELECT_EVENT_BUTTON).performClick()
    composeTestRule.onNodeWithText(selectEventsText).assertIsDisplayed()

    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.BACK_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.MEMBER_LIST).assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SELECT_DATE_RANGE_BUTTON)
        .performClick()
    composeTestRule.onNodeWithText(selectDateRangeText).assertIsDisplayed()

    fakeViewModel.setStartInstant(Instant.parse("2024-01-05T00:00:00Z"))
    fakeViewModel.setEndInstant(Instant.parse("2024-01-01T00:00:00Z"))

    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.NEXT_BUTTON).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.DATE_RANGE_INVALID_TEXT)
        .assertIsDisplayed()

    fakeViewModel.setStartInstant(Instant.parse("2024-01-01T00:00:00Z"))
    fakeViewModel.setEndInstant(Instant.parse("2024-01-05T00:00:00Z"))
  }

  @Test
  fun backFromFirstStep_callsOnCancel() {
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.BACK_BUTTON)
        .assertIsDisplayed()
        .performClick()
  }
}
