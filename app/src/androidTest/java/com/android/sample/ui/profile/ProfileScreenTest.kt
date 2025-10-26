package com.android.sample.ui.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.android.sample.model.authentification.FakeAuthRepository
import com.android.sample.model.authentification.User
import com.github.se.bootcamp.model.authentication.AuthRepository
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun profileScreen_displaysUserInformation_inReadOnlyMode() {
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

        // Check that the fields are displayed in read-only mode
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

        // Edit button should be visible
        composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).assertIsDisplayed()
    }

    @Test
    fun profileScreen_entersEditMode_whenEditButtonClicked() {
        val testUser =
            User(id = "test123", displayName = "Test User", email = "test@example.com", phoneNumber = "")
        val fakeRepository = FakeAuthRepository(testUser)
        val viewModel = ProfileViewModel(fakeRepository)

        composeTestRule.setContent {
            ProfileScreen(
                onNavigateBack = {}, onNavigateToAdminContact = {}, profileViewModel = viewModel)
        }

        // Initially, the save button should not be visible
        composeTestRule.onNodeWithTag(ProfileScreenTestTags.SAVE_BUTTON).assertDoesNotExist()

        // Click the edit button
        composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()

        // Now the save and cancel buttons should appear
        composeTestRule.onNodeWithTag(ProfileScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ProfileScreenTestTags.CANCEL_BUTTON).assertIsDisplayed()
    }

    @Test
    fun profileScreen_allowsEditingFields_afterEnteringEditMode() {
        val testUser =
            User(id = "test123", displayName = "Old Name", email = "old@example.com", phoneNumber = "")
        val fakeRepository = FakeAuthRepository(testUser)
        val viewModel = ProfileViewModel(fakeRepository)

        composeTestRule.setContent {
            ProfileScreen(
                onNavigateBack = {}, onNavigateToAdminContact = {}, profileViewModel = viewModel)
        }

        // Enter edit mode
        composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()

        // Clear and type new display name
        composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).performTextClearance()
        composeTestRule
            .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
            .performTextInput("New Name")

        // Clear and type new email
        composeTestRule.onNodeWithTag(ProfileScreenTestTags.EMAIL_FIELD).performTextClearance()
        composeTestRule
            .onNodeWithTag(ProfileScreenTestTags.EMAIL_FIELD)
            .performTextInput("new@example.com")

        // Save
        composeTestRule.onNodeWithTag(ProfileScreenTestTags.SAVE_BUTTON).performClick()
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
    fun profileScreen_editAndCancel_restoresOriginalValues() {
        val testUser =
            User(
                id = "test123",
                displayName = "Original",
                email = "orig@example.com",
                phoneNumber = "000-111-2222")
        val fakeRepository = FakeAuthRepository(testUser)
        val viewModel = ProfileViewModel(fakeRepository)

        composeTestRule.setContent {
            ProfileScreen(
                onNavigateBack = {}, onNavigateToAdminContact = {}, profileViewModel = viewModel)
        }

        composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()

        // Change display name
        composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).performTextClearance()
        composeTestRule
            .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
            .performTextInput("Changed Name")

        // Cancel edits
        composeTestRule.onNodeWithTag(ProfileScreenTestTags.CANCEL_BUTTON).performClick()

        // Verify original value restored
        composeTestRule
            .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
            .assertTextContains("Original")
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
    fun all_components_Displayed_inReadOnlyMode() {
        composeTestRule.setContent { ProfileScreen() }
        composeTestRule.onNodeWithTag(ProfileScreenTestTags.BACK_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ProfileScreenTestTags.EMAIL_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ProfileScreenTestTags.PHONE_FIELD).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ProfileScreenTestTags.ADMIN_CONTACT_BUTTON).assertIsDisplayed()
    }
}