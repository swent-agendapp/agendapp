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
import com.android.sample.model.authentication.AuthRepository
import com.android.sample.model.authentication.FakeAuthRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UsersRepositoryLocal
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.repository.FakeOrganizationRepository
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import com.android.sample.ui.organization.SelectedOrganizationViewModel
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest : FirebaseEmulatedTest(), RequiresSelectedOrganizationTestBase {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var application: Application
  private lateinit var fakeOrganizationRepository: FakeOrganizationRepository
  private lateinit var fakeAuthRepository: FakeAuthRepository
  private lateinit var fakeUserRepository: UsersRepositoryLocal
  private lateinit var selectedOrgVM: SelectedOrganizationViewModel

  override val organizationId: String = DEFAULT_TEST_ORG_ID

  private val currentUser: User =
      User(
          id = "test123",
          displayName = "Test User",
          email = "test@example.com",
          phoneNumber = "123-456-7890")
  private val adminUser: User =
      User(id = "admin123", displayName = "Admin User", email = "admin@example.com")

  private val employeeUser: User =
      User(id = "employee456", displayName = "Employee User", email = "employee@gmail.com")

  private val employeeUser2: User =
      User(id = "employee789", displayName = "Employee User 2", email = "employee2@gmail.com")

  private val organization: Organization =
      Organization(id = organizationId, name = "Test Organization")

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  override fun setUp() = runBlocking {
    application =
        InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    clearPreferences()
    fakeOrganizationRepository = FakeOrganizationRepository()
    fakeOrganizationRepository.insertOrganization(organization)

    setSelectedOrganization()

    fakeAuthRepository = FakeAuthRepository(currentUser)

    fakeUserRepository = UsersRepositoryLocal()
    fakeUserRepository.newUser(employeeUser)
    fakeUserRepository.newUser(employeeUser2)
    fakeUserRepository.newUser(adminUser)
    fakeUserRepository.newUser(currentUser)

    fakeUserRepository.addUserToOrganization(
        userId = employeeUser.id, organizationId = organization.id)
    fakeUserRepository.addUserToOrganization(
        userId = employeeUser2.id, organizationId = organization.id)

    fakeUserRepository.addAdminToOrganization(
        userId = adminUser.id, organizationId = organization.id)

    selectedOrgVM = SelectedOrganizationVMProvider.viewModel
    selectedOrgVM.selectOrganization(organization.id)
  }

  @After
  override fun tearDown() {
    clearPreferences()
  }

  private fun clearPreferences() {
    val prefs = application.getSharedPreferences(ProfileViewModel.PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().clear().apply()
  }

  private fun createViewModel(authRepository: AuthRepository) =
      ProfileViewModel(
          application,
          authRepository,
          userRepository = fakeUserRepository,
          selectedOrganizationViewModel = selectedOrgVM)

  @Test
  fun profileScreen_displaysUserInformation_inReadOnlyMode() {
    val viewModel = createViewModel(fakeAuthRepository)

    composeTestRule.setContent {
      ProfileScreen(
          onNavigateBack = {}, profileOwnerId = currentUser.id, profileViewModel = viewModel)
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
    val viewModel = createViewModel(fakeAuthRepository)

    composeTestRule.setContent {
      ProfileScreen(
          onNavigateBack = {}, profileOwnerId = currentUser.id, profileViewModel = viewModel)
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
    val viewModel = createViewModel(fakeRepository)

    composeTestRule.setContent {
      ProfileScreen(onNavigateBack = {}, profileOwnerId = testUser.id, profileViewModel = viewModel)
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
    val viewModel = createViewModel(fakeRepository)
    var backClicked = false

    composeTestRule.setContent {
      ProfileScreen(
          onNavigateBack = { backClicked = true },
          profileOwnerId = currentUser.id,
          profileViewModel = viewModel)
    }

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.BACK_BUTTON).performClick()
    assert(backClicked)
  }

  @Test
  fun profileScreen_editAndCancel_restoresOriginalValues() {
    val viewModel = createViewModel(fakeAuthRepository)

    composeTestRule.setContent {
      ProfileScreen(
          onNavigateBack = {}, profileOwnerId = currentUser.id, profileViewModel = viewModel)
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
        .assertTextContains("Test User")
  }

  @Test
  fun profileScreen_savingBlankDisplayName_keepsPreviousValue() = runTest {
    val viewModel = createViewModel(fakeAuthRepository)

    composeTestRule.setContent {
      ProfileScreen(
          onNavigateBack = {}, profileOwnerId = currentUser.id, profileViewModel = viewModel)
    }

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).performTextClearance()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SAVE_BUTTON).performClick()

    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .assertTextContains("Test User")
  }

  @Test
  fun profileScreen_displayRootIsDisplayed() {
    val fakeRepository = FakeAuthRepository(null)
    val viewModel = createViewModel(fakeRepository)

    composeTestRule.setContent {
      ProfileScreen(
          onNavigateBack = {}, profileOwnerId = currentUser.id, profileViewModel = viewModel)
    }

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_SCREEN).assertIsDisplayed()
  }

  @Test
  fun all_components_Displayed_inReadOnlyMode() {
    val fakeRepository = FakeAuthRepository(null)
    val viewModel = createViewModel(fakeRepository)

    composeTestRule.setContent {
      ProfileScreen(profileOwnerId = currentUser.id, profileViewModel = viewModel)
    }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EMAIL_FIELD).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PHONE_FIELD).assertIsDisplayed()
  }

  @Test
  fun profileScreen_signOutButtonWorks() {
    var signOutClicked = false

    val viewModel = createViewModel(fakeAuthRepository)

    composeTestRule.setContent {
      ProfileScreen(
          onSignOut = { signOutClicked = true },
          profileOwnerId = currentUser.id,
          profileViewModel = viewModel)
    }

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SIGN_OUT_BUTTON).performClick()
    assert(signOutClicked)
  }

  @Test
  fun profileScreen_promoteToAdminButtonWorks() {
    var promoteToAdminClicked = false

    val fakeAuthRepository = FakeAuthRepository(adminUser)
    val viewModel = createViewModel(fakeAuthRepository)

    composeTestRule.setContent {
      ProfileScreen(
          onPromoteToAdmin = { promoteToAdminClicked = true },
          profileOwnerId = employeeUser.id,
          profileViewModel = viewModel)
    }

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROMOTE_TO_ADMIN_BUTTON).performClick()
    assert(promoteToAdminClicked)
  }

  @Test
  fun onlyDisplayNameIsEditableWhenAdminToEmployee() {
    val fakeAuthRepository = FakeAuthRepository(adminUser)
    val viewModel = createViewModel(fakeAuthRepository)

    composeTestRule.setContent {
      ProfileScreen(profileOwnerId = employeeUser.id, profileViewModel = viewModel)
    }
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .assertIsDisplayed()
        .assertIsEnabled()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.EMAIL_FIELD)
        .assertIsDisplayed()
        .assertIsNotEnabled()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.PHONE_FIELD)
        .assertIsDisplayed()
        .assertIsNotEnabled()
  }

  @Test
  fun editButtonIsNotDisplayedWhenEmployeeToEmployee() {
    val fakeAuthRepository = FakeAuthRepository(employeeUser)
    val viewModel = createViewModel(fakeAuthRepository)

    composeTestRule.setContent {
      ProfileScreen(profileOwnerId = employeeUser2.id, profileViewModel = viewModel)
    }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).assertIsNotDisplayed()
  }

  @Test
  fun editButtonIsNotDisplayedWhenEmployeeToAdmin() {
    val fakeAuthRepository = FakeAuthRepository(employeeUser)
    val viewModel = createViewModel(fakeAuthRepository)

    composeTestRule.setContent {
      ProfileScreen(profileOwnerId = adminUser.id, profileViewModel = viewModel)
    }
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).assertIsNotDisplayed()
  }

  @Test
  fun profileScreen_savedChangesPersistAfterRecreation() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sharedPrefs = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)
    sharedPrefs.edit().clear().commit()

    fun createViewModel(): ProfileViewModel =
        ProfileViewModel(
            application = context as Application,
            authRepository = fakeAuthRepository,
            userRepository = fakeUserRepository,
            preferences = sharedPrefs)

    lateinit var setVm: (ProfileViewModel) -> Unit

    composeTestRule.setContent {
      var vm by remember { mutableStateOf(createViewModel()) }
      setVm = { newVm ->
        vm = newVm
        vm.loadProfile(currentUser.id)
      } // <-- assign into real compose state

      ProfileScreen(onNavigateBack = {}, profileOwnerId = currentUser.id, profileViewModel = vm)
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

    // --- Simulate Process Death / Recreation ---
    composeTestRule.runOnUiThread {
      setVm(createViewModel()) // <-- this NOW triggers recomposition
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
