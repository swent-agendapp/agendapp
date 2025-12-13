package com.android.sample.ui.replacement.organize

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.ui.replacement.organize.components.SelectSubstitutedScreen
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SelectSubstitutedScreenTest : FirebaseEmulatedTest(), RequiresSelectedOrganizationTestBase {

  @get:Rule val composeTestRule = createComposeRule()

  override val organizationId: String = DEFAULT_TEST_ORG_ID
  private lateinit var members: List<User>
  private lateinit var fakeViewModel: ReplacementOrganizeViewModel

  @Before
  override fun setUp() {
    super.setUp()

    setSelectedOrganization()

    // Add test members to user repository and to the selected organization
    val testMembers =
        listOf(
            User(id = "1", displayName = "Alice Anderson", email = "alice@example.com"),
            User(id = "2", displayName = "Bob Brown", email = "bob@example.com"),
            User(id = "3", displayName = "Charlie Clark", email = "charlie@example.com"),
            User(id = "4", displayName = "Dana Davis", email = "dana@example.com"))
    runBlocking {
      testMembers.forEach { user ->
        UserRepositoryProvider.repository.newUser(user)
        UserRepositoryProvider.repository.addUserToOrganization(user.id, organizationId)
      }
    }

    fakeViewModel = ReplacementOrganizeViewModel()
    fakeViewModel.loadOrganizationMembers()

    composeTestRule.setContent {
      SelectSubstitutedScreen(
          replacementOrganizeViewModel = fakeViewModel,
          onSelectEvents = {},
          onSelectDateRange = {},
          onBack = {})
    }

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      fakeViewModel.uiState.value.memberList.isNotEmpty()
    }

    members = fakeViewModel.uiState.value.memberList
  }

  @After
  fun cleanUp() {
    runBlocking {
      members.forEach { user -> UserRepositoryProvider.repository.deleteUser(user.id) }
    }
  }

  private fun labelOf(user: User): String {
    return user.displayName ?: user.email ?: user.id
  }

  @Test
  fun screenElements_areDisplayedCorrectly() {
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.INSTRUCTION_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.SEARCH_BAR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.MEMBER_LIST).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SELECT_EVENT_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SELECT_DATE_RANGE_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SELECTED_MEMBER_INFO)
        .assertIsDisplayed()
  }

  @Test
  fun buttons_areDisabled_whenNoMemberSelected() {
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SELECT_EVENT_BUTTON)
        .assertIsNotEnabled()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SELECT_DATE_RANGE_BUTTON)
        .assertIsNotEnabled()
  }

  @Test
  fun memberSelection_enablesButtons() {
    val charlie = members.first { it.email == "charlie@example.com" }
    val charlieLabel = labelOf(charlie)

    composeTestRule.onNodeWithText(charlieLabel).performClick()

    assert(fakeViewModel.uiState.value.selectedMember?.id == charlie.id)

    composeTestRule.onNodeWithTag(ReplacementOrganizeTestTags.SELECT_EVENT_BUTTON).assertIsEnabled()
    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SELECT_DATE_RANGE_BUTTON)
        .assertIsEnabled()
  }

  @Test
  fun searchFilter_filtersList_with_email() {
    val alice = members.first { it.email == "alice@example.com" }
    val bob = members.first { it.email == "bob@example.com" }
    val charlie = members.first { it.email == "charlie@example.com" }
    val dana = members.first { it.email == "dana@example.com" }

    val aliceLabel = labelOf(alice)
    val bobLabel = labelOf(bob)
    val charlieLabel = labelOf(charlie)
    val danaLabel = labelOf(dana)

    composeTestRule.onNodeWithText(aliceLabel).assertIsDisplayed()
    composeTestRule.onNodeWithText(bobLabel).assertIsDisplayed()
    composeTestRule.onNodeWithText(charlieLabel).assertIsDisplayed()
    composeTestRule.onNodeWithText(danaLabel).assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SEARCH_BAR)
        .performClick()
        .performTextInput("ali")

    composeTestRule.onNodeWithText(aliceLabel).assertIsDisplayed()
    composeTestRule.onNodeWithText(bobLabel).assertDoesNotExist()
    composeTestRule.onNodeWithText(charlieLabel).assertDoesNotExist()
    composeTestRule.onNodeWithText(danaLabel).assertDoesNotExist()
  }

  @Test
  fun searchFilter_filtersList_with_id_like_query() {
    val alice = members.first { it.email == "alice@example.com" }
    val bob = members.first { it.email == "bob@example.com" }
    val charlie = members.first { it.email == "charlie@example.com" }
    val dana = members.first { it.email == "dana@example.com" }

    val aliceLabel = labelOf(alice)
    val bobLabel = labelOf(bob)
    val charlieLabel = labelOf(charlie)
    val danaLabel = labelOf(dana)

    composeTestRule.onNodeWithText(aliceLabel).assertIsDisplayed()
    composeTestRule.onNodeWithText(bobLabel).assertIsDisplayed()
    composeTestRule.onNodeWithText(charlieLabel).assertIsDisplayed()
    composeTestRule.onNodeWithText(danaLabel).assertIsDisplayed()

    val query = danaLabel.take(2)

    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SEARCH_BAR)
        .performClick()
        .performTextInput(query)

    composeTestRule.onNodeWithText(danaLabel).assertIsDisplayed()
    composeTestRule.onNodeWithText(aliceLabel).assertDoesNotExist()
  }

  @Test
  fun searchFilter_filterList_with_displayName() {
    val alice = members.first { it.email == "alice@example.com" }
    val bob = members.first { it.email == "bob@example.com" }
    val charlie = members.first { it.email == "charlie@example.com" }
    val dana = members.first { it.email == "dana@example.com" }

    val aliceLabel = labelOf(alice)
    val bobLabel = labelOf(bob)
    val charlieLabel = labelOf(charlie)
    val danaLabel = labelOf(dana)

    composeTestRule.onNodeWithText(aliceLabel).assertIsDisplayed()
    composeTestRule.onNodeWithText(bobLabel).assertIsDisplayed()
    composeTestRule.onNodeWithText(charlieLabel).assertIsDisplayed()
    composeTestRule.onNodeWithText(danaLabel).assertIsDisplayed()

    val query = bobLabel.lowercase()

    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SEARCH_BAR)
        .performClick()
        .performTextInput(query)

    composeTestRule.onNodeWithText(bobLabel).assertIsDisplayed()
    composeTestRule.onNodeWithText(aliceLabel).assertDoesNotExist()
    composeTestRule.onNodeWithText(charlieLabel).assertDoesNotExist()
    composeTestRule.onNodeWithText(danaLabel).assertDoesNotExist()
  }

  @Test
  fun searchFilter_filtersList_noMatch() {
    val alice = members.first { it.email == "alice@example.com" }
    val bob = members.first { it.email == "bob@example.com" }
    val charlie = members.first { it.email == "charlie@example.com" }
    val dana = members.first { it.email == "dana@example.com" }

    val aliceLabel = labelOf(alice)
    val bobLabel = labelOf(bob)
    val charlieLabel = labelOf(charlie)
    val danaLabel = labelOf(dana)

    composeTestRule.onNodeWithText(aliceLabel).assertIsDisplayed()
    composeTestRule.onNodeWithText(bobLabel).assertIsDisplayed()
    composeTestRule.onNodeWithText(charlieLabel).assertIsDisplayed()
    composeTestRule.onNodeWithText(danaLabel).assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(ReplacementOrganizeTestTags.SEARCH_BAR)
        .performClick()
        .performTextInput("unknown user")

    composeTestRule.onNodeWithText(aliceLabel).assertDoesNotExist()
    composeTestRule.onNodeWithText(bobLabel).assertDoesNotExist()
    composeTestRule.onNodeWithText(charlieLabel).assertDoesNotExist()
    composeTestRule.onNodeWithText(danaLabel).assertDoesNotExist()
  }
}
