package com.android.sample.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

object ProfileScreenTestTags {
  const val ROOT = "profile_screen"
  const val BACK_BUTTON = "back_button"
  const val DISPLAY_NAME_FIELD = "display_name_field"
  const val EMAIL_FIELD = "email_field"
  const val PHONE_FIELD = "phone_field"
  const val SAVE_BUTTON = "save_button"
  const val ADMIN_CONTACT_BUTTON = "admin_contact_button"
}

/**
 * Profile screen allowing user to view and edit their contact information.
 *
 * @param onNavigateBack Callback to navigate back to previous screen
 * @param onNavigateToAdminContact Callback to navigate to admin contact screen
 * @param profileViewModel ViewModel managing user state
 */
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToAdminContact: () -> Unit = {},
    profileViewModel: ProfileViewModel = viewModel()
) {
  val uiState by profileViewModel.uiState.collectAsState()

  Surface(modifier = Modifier.fillMaxSize().semantics { testTag = ProfileScreenTestTags.ROOT }) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally) {
          // Back Button
          Button(
              modifier = Modifier.testTag(ProfileScreenTestTags.BACK_BUTTON).align(Alignment.Start),
              onClick = onNavigateBack) {
                Text("Back")
              }

          Spacer(modifier = Modifier.height(24.dp))

          // User Information Section
          Text("User Profile", style = MaterialTheme.typography.headlineMedium)

          Spacer(modifier = Modifier.height(24.dp))

          // Display Name Field
          OutlinedTextField(
              value = uiState.displayName,
              onValueChange = { profileViewModel.updateDisplayName(it) },
              label = { Text("Display Name") },
              modifier = Modifier.fillMaxWidth().testTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD),
              singleLine = true)

          Spacer(modifier = Modifier.height(16.dp))

          // Email Field
          OutlinedTextField(
              value = uiState.email,
              onValueChange = { profileViewModel.updateEmail(it) },
              label = { Text("Email") },
              modifier = Modifier.fillMaxWidth().testTag(ProfileScreenTestTags.EMAIL_FIELD),
              singleLine = true)

          Spacer(modifier = Modifier.height(16.dp))

          // Phone Number Field
          OutlinedTextField(
              value = uiState.phoneNumber,
              onValueChange = { profileViewModel.updatePhoneNumber(it) },
              label = { Text("Phone Number") },
              modifier = Modifier.fillMaxWidth().testTag(ProfileScreenTestTags.PHONE_FIELD),
              singleLine = true)

          Spacer(modifier = Modifier.height(24.dp))

          // Save Button
          Button(
              modifier = Modifier.testTag(ProfileScreenTestTags.SAVE_BUTTON).fillMaxWidth(),
              onClick = { profileViewModel.saveProfile() }) {
                Text("Save Profile")
              }

          Spacer(modifier = Modifier.height(16.dp))

          // Admin Contact Button
          OutlinedButton(
              modifier =
                  Modifier.testTag(ProfileScreenTestTags.ADMIN_CONTACT_BUTTON).fillMaxWidth(),
              onClick = onNavigateToAdminContact) {
                Text("View Admin Contact")
              }
        }
  }
}
