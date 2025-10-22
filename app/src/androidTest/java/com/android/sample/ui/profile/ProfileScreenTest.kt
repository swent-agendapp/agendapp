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
    val testUser =
        User(
            id = "test123",
            displayName = "Test User",
            email = "test@example.com",
            phoneNumber = "123-456-7890")
    every { mockRepository.getCurrentUser() } returns testUser

    val viewModel = ProfileViewModel(mockRepository)

    composeTestRule.setContent {
      ProfileScreen(onNavigateBack = {}, onNavigateToAdminContact = {}, profileViewModel = viewModel)
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
    val mockRepository = mockk<AuthRepository>()
    val testUser = User(id = "test123", displayName = "Test User", email = "test@example.com")
    every { mockRepository.getCurrentUser() } returns testUser

    val viewModel = ProfileViewModel(mockRepository)

    composeTestRule.setContent {
      ProfileScreen(onNavigateBack = {}, onNavigateToAdminContact = {}, profileViewModel = viewModel)
    }

    // Type in display name field
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .performTextClearance()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .performTextInput("New Name")
  }

  @Test
  fun profileScreen_backButtonWorks() {
    val mockRepository = mockk<AuthRepository>()
    every { mockRepository.getCurrentUser() } returns null

    val viewModel = ProfileViewModel(mockRepository)
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
    val mockRepository = mockk<AuthRepository>()
    every { mockRepository.getCurrentUser() } returns null

    val viewModel = ProfileViewModel(mockRepository)
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
    val mockRepository = mockk<AuthRepository>()
    every { mockRepository.getCurrentUser() } returns null

    val viewModel = ProfileViewModel(mockRepository)

    composeTestRule.setContent {
      ProfileScreen(onNavigateBack = {}, onNavigateToAdminContact = {}, profileViewModel = viewModel)
    }

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun profileScreen_displayRootIsDisplayed() {
    val mockRepository = mockk<AuthRepository>()
    every { mockRepository.getCurrentUser() } returns null

    val viewModel = ProfileViewModel(mockRepository)

    composeTestRule.setContent {
      ProfileScreen(onNavigateBack = {}, onNavigateToAdminContact = {}, profileViewModel = viewModel)
    }

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.ROOT).assertIsDisplayed()
  }
}
