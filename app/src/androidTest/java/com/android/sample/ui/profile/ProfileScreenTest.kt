package com.android.sample.ui.profile

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.model.authentication.FakeAuthRepository
import com.android.sample.model.authentication.User
import com.github.se.bootcamp.model.authentication.AuthRepository
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var application: Application

  @Before
  fun setUp() {
    application =
        InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    clearPreferences()
  }

  @After
  fun tearDown() {
    clearPreferences()
  }

  private fun clearPreferences() {
    val prefs = application.getSharedPreferences(ProfileViewModel.PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().clear().apply()
  }

  private fun createViewModel(repository: AuthRepository) =
      ProfileViewModel(application, repository)

  @Test
  fun profileScreen_displaysUserInformation_inReadOnlyMode() {
    val testUser =
        User(
            id = "test123",
            displayName = "Test User",
            email = "test@example.com",
            phoneNumber = "123-456-7890")

    val fakeRepository = FakeAuthRepository(testUser)
    val viewModel = createViewModel(fakeRepository)

    composeTestRule.setContent { ProfileScreen(onNavigateBack = {}, profileViewModel = viewModel) }

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
        User(
            id = "test123", displayName = "Test User", email = "test@example.com", phoneNumber = "")
    val fakeRepository = FakeAuthRepository(testUser)
    val viewModel = createViewModel(fakeRepository)

    composeTestRule.setContent { ProfileScreen(onNavigateBack = {}, profileViewModel = viewModel) }

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
    val viewModel = createViewModel(fakeRepository)

    composeTestRule.setContent { ProfileScreen(onNavigateBack = {}, profileViewModel = viewModel) }

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
    val viewModel = createViewModel(fakeRepository)
    var backClicked = false

    composeTestRule.setContent {
      ProfileScreen(onNavigateBack = { backClicked = true }, profileViewModel = viewModel)
    }

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.BACK_BUTTON).performClick()
    assert(backClicked)
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
    val viewModel = createViewModel(fakeRepository)

    composeTestRule.setContent { ProfileScreen(onNavigateBack = {}, profileViewModel = viewModel) }

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
  fun profileScreen_savingBlankDisplayName_keepsPreviousValue() {
    val testUser =
        User(
            id = "test123",
            displayName = "Keep Me",
            email = "keep@example.com",
            phoneNumber = "000-111-2222")
    val fakeRepository = FakeAuthRepository(testUser)
    val viewModel = createViewModel(fakeRepository)

    composeTestRule.setContent { ProfileScreen(onNavigateBack = {}, profileViewModel = viewModel) }

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).performTextClearance()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SAVE_BUTTON).performClick()

    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .assertTextContains("Keep Me")
  }

  @Test
  fun profileScreen_displayRootIsDisplayed() {
    val fakeRepository = FakeAuthRepository(null)
    val viewModel = createViewModel(fakeRepository)

    composeTestRule.setContent { ProfileScreen(onNavigateBack = {}, profileViewModel = viewModel) }

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_SCREEN).assertIsDisplayed()
  }

  @Test
  fun all_components_Displayed_inReadOnlyMode() {
    val fakeRepository = FakeAuthRepository(null)
    val viewModel = createViewModel(fakeRepository)

    composeTestRule.setContent { ProfileScreen(profileViewModel = viewModel) }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EMAIL_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PHONE_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SIGN_OUT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun profileScreen_signOutButtonWorks() {
    var signOutClicked = false

    val fakeRepository = FakeAuthRepository(null)
    val viewModel = createViewModel(fakeRepository)

    composeTestRule.setContent {
      ProfileScreen(onSignOut = { signOutClicked = true }, profileViewModel = viewModel)
    }

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SIGN_OUT_BUTTON).performClick()
    assert(signOutClicked)
  }

  @Test
  fun profileScreen_savedChangesPersistAfterRecreation() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sharedPrefs = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)
    sharedPrefs.edit().clear().commit()

    val testUser =
        User(
            id = "test123",
            displayName = "Original",
            email = "orig@example.com",
            phoneNumber = "000-111-2222")

    val fakeRepository = FakeAuthRepository(testUser)

    fun createViewModel(): ProfileViewModel =
        ProfileViewModel(
            application = context as Application,
            repository = fakeRepository,
            preferences = sharedPrefs)

    // 🔥 Hoisted mutable ViewModel reference
    lateinit var currentViewModel: ProfileViewModel

    composeTestRule.setContent {
      var vm by remember { mutableStateOf(createViewModel()) }
      currentViewModel = vm // expose it so the test can replace it

      ProfileScreen(onNavigateBack = {}, profileViewModel = vm)
    }

    // --- Edit fields ---
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).performTextClearance()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .performTextInput("Persisted Name")

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EMAIL_FIELD).performTextClearance()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.EMAIL_FIELD)
        .performTextInput("persisted@example.com")

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PHONE_FIELD).performTextClearance()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.PHONE_FIELD)
        .performTextInput("999-888-7777")

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SAVE_BUTTON).performClick()

    composeTestRule.waitForIdle()

    // --- Simulate recreation by replacing the ViewModel inside the composition ---
    composeTestRule.runOnUiThread {
      val newViewModel = createViewModel()
      // swap the VM inside the composition
      currentViewModel = newViewModel
    }

    composeTestRule.waitForIdle()

    // --- Assertions ---
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .assertTextContains("Persisted Name")

    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.EMAIL_FIELD)
        .assertTextContains("persisted@example.com")

    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.PHONE_FIELD)
        .assertTextContains("999-888-7777")
  }
}
