package com.android.sample.ui.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.model.authentification.User
import com.github.se.bootcamp.model.authentication.AuthRepository
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {

  // --- Fake repository for testing ---
  class FakeAuthRepository(private val user: User? = null) : AuthRepository {

    override fun getCurrentUser(): User? = user

    override suspend fun signInWithGoogle(
        credential: androidx.credentials.Credential
    ): Result<User> {
      return user?.let { Result.success(it) } ?: Result.failure(Exception("No user"))
    }

    override fun signOut(): Result<Unit> = Result.success(Unit)
  }

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun profileScreen_displaysUserInformation() {
    val testUser =
        User(
            id = "test123",
            displayName = "Test User",
            email = "test@example.com",
            phoneNumber = "123-456-7890")

    val fakeRepository = FakeAuthRepository(testUser)
    val viewModel = ProfileViewModel(fakeRepository)

    composeTestRule.setContent {
      ProfileScreen(
          onNavigateBack = {}, onNavigateToAdminContact = {}, profileViewModel = viewModel)
    }

    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .assertIsDisplayed()
        .assertTextContains("Test User")

    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.EMAIL_FIELD)
        .assertIsDisplayed()
        .assertTextContains("test@example.com")

    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.PHONE_FIELD)
        .assertIsDisplayed()
        .assertTextContains("123-456-7890")
  }

  @Test
  fun profileScreen_allowsEditingFields() {
    val testUser = User(id = "test123", displayName = "Test User", email = "test@example.com")
    val fakeRepository = FakeAuthRepository(testUser)
    val viewModel = ProfileViewModel(fakeRepository)

    composeTestRule.setContent {
      ProfileScreen(
          onNavigateBack = {}, onNavigateToAdminContact = {}, profileViewModel = viewModel)
    }

    // Clear and type new display name
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).performTextClearance()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .performTextInput("New Name")
  }

  @Test
  fun profileScreen_backButtonWorks() {
    val fakeRepository = FakeAuthRepository(null)
    val viewModel = ProfileViewModel(fakeRepository)
    var backClicked = false

    composeTestRule.setContent {
      ProfileScreen(
          onNavigateBack = { backClicked = true },
          onNavigateToAdminContact = {},
          profileViewModel = viewModel)
    }

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.BACK_BUTTON).performClick()
    assert(backClicked)
  }

  @Test
  fun profileScreen_adminContactButtonWorks() {
    val fakeRepository = FakeAuthRepository(null)
    val viewModel = ProfileViewModel(fakeRepository)
    var adminContactClicked = false

    composeTestRule.setContent {
      ProfileScreen(
          onNavigateBack = {},
          onNavigateToAdminContact = { adminContactClicked = true },
          profileViewModel = viewModel)
    }

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.ADMIN_CONTACT_BUTTON).performClick()
    assert(adminContactClicked)
  }

  @Test
  fun profileScreen_saveButtonIsDisplayed() {
    val fakeRepository = FakeAuthRepository(null)
    val viewModel = ProfileViewModel(fakeRepository)

    composeTestRule.setContent {
      ProfileScreen(
          onNavigateBack = {}, onNavigateToAdminContact = {}, profileViewModel = viewModel)
    }

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun profileScreen_displayRootIsDisplayed() {
    val fakeRepository = FakeAuthRepository(null)
    val viewModel = ProfileViewModel(fakeRepository)

    composeTestRule.setContent {
      ProfileScreen(
          onNavigateBack = {}, onNavigateToAdminContact = {}, profileViewModel = viewModel)
    }

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_SCREEN).assertIsDisplayed()
  }

  @Test
  fun all_components_Displayed() {
    composeTestRule.setContent { ProfileScreen() }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EMAIL_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PHONE_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.ADMIN_CONTACT_BUTTON).assertIsDisplayed()
  }
}
