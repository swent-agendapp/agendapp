package com.android.sample.ui.profile

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.authentication.SignInViewModel
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.theme.AlphaExtraLow
import com.android.sample.ui.theme.CornerRadiusExtraLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.SizeHuge
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.SpacingSmall
import com.android.sample.ui.theme.WeightExtraHeavy

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

@Composable
fun LogoutRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val errorColor = MaterialTheme.colorScheme.error

  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(CornerRadiusExtraLarge))
              .clickable(onClick = onClick)
              .background(errorColor.copy(alpha = AlphaExtraLow))
              .padding(horizontal = PaddingMedium, vertical = PaddingMedium),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            tint = errorColor)
        Spacer(modifier = Modifier.width(SpacingSmall))

        Text(text = text, style = MaterialTheme.typography.bodyLarge, color = errorColor)
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    profileViewModel: ProfileViewModel = rememberProfileViewModel(),
    authViewModel: SignInViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignOut: () -> Unit = {}
) {
  val uiState by profileViewModel.uiState.collectAsState()
  val screenState = rememberProfileScreenState(uiState, profileViewModel)

  Scaffold(topBar = { ProfileTopBar(onNavigateBack) }) { innerPadding ->
    ProfileContent(
        modifier = Modifier.padding(innerPadding),
        screenState = screenState,
        authViewModel = authViewModel,
        credentialManager = credentialManager,
        onSignOut = onSignOut)
  }
}

@Stable
class ProfileScreenState(
    val uiState: ProfileUIState,
    private val profileViewModel: ProfileViewModel
) {
  var isEditMode by mutableStateOf(false)
  var displayName by mutableStateOf(uiState.displayName)
  var email by mutableStateOf(uiState.email)
  var phone by mutableStateOf(uiState.phoneNumber)
  var emailError by mutableStateOf<String?>(null)
  var phoneError by mutableStateOf<String?>(null)

  fun onEdit() {
    isEditMode = true
  }

  fun onCancel() {
    resetToOriginalState()
    clearErrors()
    isEditMode = false
  }

  fun onSave(emailErrorMessage: String, phoneErrorMessage: String) {
    if (validateAndSetErrors(emailErrorMessage, phoneErrorMessage)) {
      updateProfileData()
      profileViewModel.saveProfile()
      isEditMode = false
    }
  }

  fun onDisplayNameChange(newValue: String) {
    displayName = newValue
  }

  fun onEmailChange(newValue: String) {
    email = newValue
    emailError = null
  }

  fun onPhoneChange(newValue: String) {
    phone = newValue
    phoneError = null
  }

  private fun resetToOriginalState() {
    displayName = uiState.displayName
    email = uiState.email
    phone = uiState.phoneNumber
  }

  private fun clearErrors() {
    emailError = null
    phoneError = null
  }

  private fun validateAndSetErrors(emailErrorMessage: String, phoneErrorMessage: String): Boolean {
    val (emailValid, phoneValid) = validateInputs(email, phone)
    emailError = if (!emailValid) emailErrorMessage else null
    phoneError = if (!phoneValid) phoneErrorMessage else null
    return emailValid && phoneValid
  }

  private fun updateProfileData() {
    if (displayName.isBlank()) {
      displayName = uiState.displayName
    } else {
      profileViewModel.updateDisplayName(displayName)
    }

    profileViewModel.updateEmail(email)
    profileViewModel.updatePhoneNumber(phone)
  }
}

@Composable
private fun rememberProfileScreenState(
    uiState: ProfileUIState,
    profileViewModel: ProfileViewModel
): ProfileScreenState {
  val screenState =
      remember(uiState, profileViewModel) { ProfileScreenState(uiState, profileViewModel) }

  LaunchedEffect(uiState.displayName, uiState.email, uiState.phoneNumber) {
    if (!screenState.isEditMode) {
      screenState.displayName = uiState.displayName
      screenState.email = uiState.email
      screenState.phone = uiState.phoneNumber
    }
  }

  return screenState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopBar(onNavigateBack: () -> Unit) {
  SecondaryPageTopBar(
      title = stringResource(R.string.profile_screen_title),
      onClick = onNavigateBack,
      backButtonTestTags = ProfileScreenTestTags.BACK_BUTTON)
}

@Composable
private fun ProfileContent(
    modifier: Modifier = Modifier,
    screenState: ProfileScreenState,
    authViewModel: SignInViewModel,
    credentialManager: CredentialManager,
    onSignOut: () -> Unit
) {
  val emailErrorMessage = stringResource(R.string.profile_email_error)
  val phoneErrorMessage = stringResource(R.string.profile_phone_error)

  Column(
      modifier =
          modifier.padding(PaddingMedium).fillMaxSize().semantics {
            testTag = ProfileScreenTestTags.PROFILE_SCREEN
          },
      verticalArrangement = Arrangement.spacedBy(SpacingLarge)) {
        ProfileHeader(
            displayName = screenState.displayName,
            email = screenState.email,
            isEditMode = screenState.isEditMode,
            onEdit = screenState::onEdit,
            onCancel = screenState::onCancel,
            onSave = { screenState.onSave(emailErrorMessage, phoneErrorMessage) })

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(CornerRadiusExtraLarge),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
              Column(
                  modifier =
                      Modifier.padding(
                          start = PaddingMedium,
                          end = PaddingMedium,
                          top = SpacingLarge,
                          bottom = SpacingLarge),
                  verticalArrangement = Arrangement.spacedBy(SpacingLarge)) {
                    ProfileFieldsSection(screenState)
                  }
            }

        Spacer(modifier = Modifier.weight(WeightExtraHeavy))

        LogoutRow(
            text = stringResource(R.string.sign_in_logout_content_description),
            modifier = Modifier.testTag(ProfileScreenTestTags.SIGN_OUT_BUTTON),
            onClick = {
              authViewModel.signOut(credentialManager)
              onSignOut()
            })
      }
}

@Composable
private fun ProfileFieldsSection(screenState: ProfileScreenState) {
  Column(modifier = Modifier.fillMaxWidth()) {
    if (screenState.isEditMode) {
      ProfileTextField(
          label = stringResource(R.string.profile_display_name_label),
          value = screenState.displayName,
          isEditMode = screenState.isEditMode,
          onValueChange = screenState::onDisplayNameChange,
          testTag = ProfileScreenTestTags.DISPLAY_NAME_FIELD)

      Spacer(Modifier.height(SpacingLarge))

      ProfileTextField(
          label = stringResource(R.string.profile_email_label),
          value = screenState.email,
          isEditMode = screenState.isEditMode,
          onValueChange = screenState::onEmailChange,
          error = screenState.emailError,
          keyboardType = KeyboardType.Email,
          testTag = ProfileScreenTestTags.EMAIL_FIELD)

      Spacer(Modifier.height(SpacingLarge))

      ProfileTextField(
          label = stringResource(R.string.profile_phone_label),
          value = screenState.phone,
          isEditMode = screenState.isEditMode,
          onValueChange = screenState::onPhoneChange,
          error = screenState.phoneError,
          keyboardType = KeyboardType.Phone,
          testTag = ProfileScreenTestTags.PHONE_FIELD)
    } else {
      ProfileInfoRow(
          label = stringResource(R.string.profile_display_name_label),
          value = screenState.displayName,
          testTag = ProfileScreenTestTags.DISPLAY_NAME_FIELD)

      Spacer(Modifier.height(SpacingLarge))

      ProfileInfoRow(
          label = stringResource(R.string.profile_email_label),
          value = screenState.email,
          testTag = ProfileScreenTestTags.EMAIL_FIELD)

      Spacer(Modifier.height(SpacingLarge))

      ProfileInfoRow(
          label = stringResource(R.string.profile_phone_label),
          value = screenState.phone,
          testTag = ProfileScreenTestTags.PHONE_FIELD)
    }
  }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    testTag: String,
) {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(CornerRadiusExtraLarge))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AlphaExtraLow))
              .padding(PaddingMedium)
              .testTag(testTag)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(SpacingSmall))

        Text(
            text = if (value.isBlank()) "-" else value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface)
      }
}

@Composable
private fun rememberProfileViewModel(): ProfileViewModel {
  val application = LocalContext.current.applicationContext as Application
  return viewModel(factory = ProfileViewModel.provideFactory(application))
}

@Composable
private fun ProfileHeader(
    displayName: String,
    email: String,
    isEditMode: Boolean,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(WeightExtraHeavy)) {
              Box(
                  modifier =
                      Modifier.size(SizeHuge)
                          .clip(CircleShape)
                          .background(MaterialTheme.colorScheme.primaryContainer),
                  contentAlignment = Alignment.Center) {
                    Text(
                        text = initialsFromName(displayName),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                  }

              Spacer(modifier = Modifier.width(SpacingLarge))

              Column {
                Text(
                    text =
                        if (displayName.isBlank()) stringResource(R.string.profile_title)
                        else displayName,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)

                if (email.isNotBlank()) {
                  Text(
                      text = email,
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis)
                }
              }
            }

        if (isEditMode) {
          EditModeActions(onCancel = onCancel, onSave = onSave)
        } else {
          EditButton(onEdit = onEdit)
        }
      }
}

@Composable
private fun EditModeActions(onCancel: () -> Unit, onSave: () -> Unit) {
  Row {
    IconButton(
        onClick = onCancel, modifier = Modifier.testTag(ProfileScreenTestTags.CANCEL_BUTTON)) {
          Icon(
              Icons.Default.Close,
              contentDescription = stringResource(R.string.profile_cancel_content_description))
        }

    IconButton(onClick = onSave, modifier = Modifier.testTag(ProfileScreenTestTags.SAVE_BUTTON)) {
      Icon(
          Icons.Default.Save,
          contentDescription = stringResource(R.string.profile_save_content_description))
    }
  }
}

@Composable
private fun EditButton(onEdit: () -> Unit) {
  IconButton(onClick = onEdit, modifier = Modifier.testTag(ProfileScreenTestTags.EDIT_BUTTON)) {
    Icon(
        Icons.Default.Edit,
        contentDescription = stringResource(R.string.profile_edit_content_description))
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

private fun initialsFromName(name: String): String {
  if (name.isBlank()) return "U"
  return name
      .trim()
      .split(" ")
      .filter { it.isNotBlank() }
      .take(2)
      .joinToString("") { it.first().uppercase() }
}

private fun validateInputs(email: String, phone: String): Pair<Boolean, Boolean> {
  val emailValid = isValidEmail(email)
  val phoneValid = isValidPhone(phone)
  return emailValid to phoneValid
}
