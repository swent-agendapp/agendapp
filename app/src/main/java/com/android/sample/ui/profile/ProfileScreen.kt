package com.android.sample.ui.profile

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.SpacingExtraLarge
import com.android.sample.ui.theme.SpacingLarge
import com.github.se.bootcamp.ui.authentication.SignInViewModel

object ProfileScreenTestTags {
  const val PROFILE_SCREEN = "profile_screen"
  const val BACK_BUTTON = "back_button"
  const val DISPLAY_NAME_FIELD = "display_name_field"
  const val EMAIL_FIELD = "email_field"
  const val PHONE_FIELD = "phone_field"
  const val SAVE_BUTTON = "save_button"
  const val CANCEL_BUTTON = "cancel_button"
  const val EDIT_BUTTON = "edit_button"
  const val SIGN_OUT_BUTTON = "sign_out_button"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    profileViewModel: ProfileViewModel = rememberProfileViewModel(),
    authViewModel: SignInViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignOut: () -> Unit = {}
) {
  val uiState by profileViewModel.uiState.collectAsState()
  var isEditMode by remember { mutableStateOf(false) }
  var displayName by remember { mutableStateOf(uiState.displayName) }
  var email by remember { mutableStateOf(uiState.email) }
  var phone by remember { mutableStateOf(uiState.phoneNumber) }
  var emailError by remember { mutableStateOf<String?>(null) }
  var phoneError by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(uiState.displayName, uiState.email, uiState.phoneNumber, isEditMode) {
    if (!isEditMode) {
      displayName = uiState.displayName
      email = uiState.email
      phone = uiState.phoneNumber
    }
  }

  val emailErrorMessage = stringResource(R.string.profile_email_error)
  val phoneErrorMessage = stringResource(R.string.profile_phone_error)

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.profile_screen_title)) },
            navigationIcon = {
              IconButton(
                  onClick = onNavigateBack,
                  modifier = Modifier.testTag(ProfileScreenTestTags.BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back))
                  }
            })
      }) { innerPadding ->
        Column(
            modifier =
                Modifier.padding(innerPadding).padding(PaddingMedium).fillMaxSize().semantics {
                  testTag = ProfileScreenTestTags.PROFILE_SCREEN
                },
            horizontalAlignment = Alignment.CenterHorizontally) {
              ProfileHeader(
                  isEditMode = isEditMode,
                  onEdit = { isEditMode = true },
                  onCancel = {
                    displayName = uiState.displayName
                    email = uiState.email
                    phone = uiState.phoneNumber
                    emailError = null
                    phoneError = null
                    isEditMode = false
                  },
                  onSave = {
                    val (emailValid, phoneValid) = validateInputs(email, phone)
                    emailError = if (!emailValid) emailErrorMessage else null
                    phoneError = if (!phoneValid) phoneErrorMessage else null

                    if (emailValid && phoneValid) {
                      profileViewModel.updateDisplayName(displayName)
                      profileViewModel.updateEmail(email)
                      profileViewModel.updatePhoneNumber(phone)
                      profileViewModel.saveProfile()
                      isEditMode = false
                    }
                  })

              Spacer(Modifier.height(SpacingExtraLarge))

              ProfileTextField(
                  label = stringResource(R.string.profile_display_name_label),
                  value = displayName,
                  isEditMode = isEditMode,
                  onValueChange = { displayName = it },
                  testTag = ProfileScreenTestTags.DISPLAY_NAME_FIELD)

              Spacer(Modifier.height(SpacingLarge))

              ProfileTextField(
                  label = stringResource(R.string.profile_email_label),
                  value = email,
                  isEditMode = isEditMode,
                  onValueChange = {
                    email = it
                    emailError = null
                  },
                  error = emailError,
                  keyboardType = KeyboardType.Email,
                  testTag = ProfileScreenTestTags.EMAIL_FIELD)

              Spacer(Modifier.height(SpacingLarge))

              ProfileTextField(
                  label = stringResource(R.string.profile_phone_label),
                  value = phone,
                  isEditMode = isEditMode,
                  onValueChange = {
                    phone = it
                    phoneError = null
                  },
                  error = phoneError,
                  keyboardType = KeyboardType.Phone,
                  testTag = ProfileScreenTestTags.PHONE_FIELD)

              Spacer(Modifier.height(SpacingLarge))

              Button(
                  onClick = {
                    authViewModel.signOut(credentialManager)
                    onSignOut()
                  },
                  modifier = Modifier.testTag(ProfileScreenTestTags.SIGN_OUT_BUTTON)) {
                    Text(stringResource(R.string.sign_in_logout_content_description))
                  }
            }
      }
}

@Composable
private fun rememberProfileViewModel(): ProfileViewModel {
  val application = LocalContext.current.applicationContext as Application
  return viewModel(factory = ProfileViewModel.provideFactory(application))
}

@Composable
private fun ProfileHeader(
    isEditMode: Boolean,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.profile_title), style = MaterialTheme.typography.headlineMedium)

        if (isEditMode) {
          Row {
            IconButton(
                onClick = onCancel,
                modifier = Modifier.testTag(ProfileScreenTestTags.CANCEL_BUTTON)) {
                  Icon(
                      Icons.Default.Close,
                      contentDescription =
                          stringResource(R.string.profile_cancel_content_description))
                }

            IconButton(
                onClick = onSave, modifier = Modifier.testTag(ProfileScreenTestTags.SAVE_BUTTON)) {
                  Icon(
                      Icons.Default.Save,
                      contentDescription =
                          stringResource(R.string.profile_save_content_description))
                }
          }
        } else {
          IconButton(
              onClick = onEdit, modifier = Modifier.testTag(ProfileScreenTestTags.EDIT_BUTTON)) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.profile_edit_content_description))
              }
        }
      }
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    isEditMode: Boolean,
    onValueChange: (String) -> Unit,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String
) {
  OutlinedTextField(
      value = value,
      onValueChange = { if (isEditMode) onValueChange(it) },
      label = { Text(label) },
      isError = error != null,
      supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
      modifier = Modifier.fillMaxWidth().testTag(testTag),
      singleLine = true,
      enabled = isEditMode,
      keyboardOptions = KeyboardOptions(keyboardType = keyboardType))
}

private fun validateInputs(email: String, phone: String): Pair<Boolean, Boolean> {
  val emailValid = isValidEmail(email)
  val phoneValid = isValidPhone(phone)
  return emailValid to phoneValid
}
