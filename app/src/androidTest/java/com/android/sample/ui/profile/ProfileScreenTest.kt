package com.android.sample.ui.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.model.authentification.User
import com.github.se.bootcamp.model.authentication.AuthRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun profileScreen_displaysUserInformation() {
    val mockRepository = mockk<AuthRepository>()
    val testUser = User(id = "test123", displayName = "Test User", email = "test@example.com")
    every { mockRepository.getCurrentUser() } returns testUser

    val viewModel = ProfileViewModel(mockRepository)

    composeTestRule.setContent { ProfileScreen(onNavigateBack = {}, profileViewModel = viewModel) }

    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT)
        .assertIsDisplayed()
        .assertTextContains("Test User")
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.EMAIL_TEXT)
        .assertIsDisplayed()
        .assertTextContains("test@example.com")
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.USER_ID_TEXT)
        .assertIsDisplayed()
        .assertTextContains("test123")
  }

  @Test
  fun profileScreen_displaysNoUserInfoWhenUserIsNull() {
    val mockRepository = mockk<AuthRepository>()
    every { mockRepository.getCurrentUser() } returns null

    val viewModel = ProfileViewModel(mockRepository)

    composeTestRule.setContent { ProfileScreen(onNavigateBack = {}, profileViewModel = viewModel) }

    composeTestRule.onNodeWithText("No user information available").assertIsDisplayed()
  }

  @Test
  fun profileScreen_backButtonWorks() {
    val mockRepository = mockk<AuthRepository>()
    every { mockRepository.getCurrentUser() } returns null

    val viewModel = ProfileViewModel(mockRepository)
    var backClicked = false

    composeTestRule.setContent {
      ProfileScreen(onNavigateBack = { backClicked = true }, profileViewModel = viewModel)
    }

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.BACK_BUTTON).performClick()

    assert(backClicked)
  }

  @Test
  fun profileScreen_showAdminContactButtonTogglesContact() {
    val mockRepository = mockk<AuthRepository>()
    every { mockRepository.getCurrentUser() } returns null

    val viewModel = ProfileViewModel(mockRepository)

    composeTestRule.setContent { ProfileScreen(onNavigateBack = {}, profileViewModel = viewModel) }

    // Initially admin contact should not be visible
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.ADMIN_CONTACT_INFO).assertDoesNotExist()

    // Click the button to show admin contact
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SHOW_ADMIN_CONTACT_BUTTON).performClick()

    // Admin contact should now be visible
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.ADMIN_CONTACT_INFO).assertIsDisplayed()
    composeTestRule.onNodeWithText("admin@agendapp.com").assertIsDisplayed()

    // Click the button again to hide admin contact
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SHOW_ADMIN_CONTACT_BUTTON).performClick()

    // Admin contact should be hidden again
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.ADMIN_CONTACT_INFO).assertDoesNotExist()
  }

  @Test
  fun profileScreen_displayRootIsDisplayed() {
    val mockRepository = mockk<AuthRepository>()
    every { mockRepository.getCurrentUser() } returns null

    val viewModel = ProfileViewModel(mockRepository)

    composeTestRule.setContent { ProfileScreen(onNavigateBack = {}, profileViewModel = viewModel) }

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.ROOT).assertIsDisplayed()
  }
}
