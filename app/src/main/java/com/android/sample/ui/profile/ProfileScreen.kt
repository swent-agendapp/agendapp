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
  const val DISPLAY_NAME_TEXT = "display_name_text"
  const val EMAIL_TEXT = "email_text"
  const val USER_ID_TEXT = "user_id_text"
  const val SHOW_ADMIN_CONTACT_BUTTON = "show_admin_contact_button"
  const val ADMIN_CONTACT_INFO = "admin_contact_info"
}

/**
 * Profile screen displaying user information and admin contact button.
 *
 * @param onNavigateBack Callback to navigate back to previous screen
 * @param profileViewModel ViewModel managing user state
 */
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
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

          uiState.user?.let { user ->
            // Display Name
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text("Display Name", style = MaterialTheme.typography.labelMedium)
                Text(
                    user.displayName ?: "Not set",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.testTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT))
              }
            }

            // Email
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text("Email", style = MaterialTheme.typography.labelMedium)
                Text(
                    user.email ?: "Not set",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.testTag(ProfileScreenTestTags.EMAIL_TEXT))
              }
            }

            // User ID
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text("User ID", style = MaterialTheme.typography.labelMedium)
                Text(
                    user.id,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag(ProfileScreenTestTags.USER_ID_TEXT))
              }
            }
          }
              ?: run {
                Text("No user information available", style = MaterialTheme.typography.bodyLarge)
              }

          Spacer(modifier = Modifier.height(24.dp))

          // Show Admin Contact Button
          Button(
              modifier =
                  Modifier.testTag(ProfileScreenTestTags.SHOW_ADMIN_CONTACT_BUTTON)
                      .fillMaxWidth(),
              onClick = { profileViewModel.toggleAdminContact() }) {
                Text(if (uiState.showAdminContact) "Hide Admin Contact" else "Show Admin Contact")
              }

          Spacer(modifier = Modifier.height(16.dp))

          // Admin Contact Information
          if (uiState.showAdminContact) {
            Card(
                modifier =
                    Modifier.testTag(ProfileScreenTestTags.ADMIN_CONTACT_INFO)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)) {
                  Column(modifier = Modifier.padding(16.dp)) {
                    Text("Admin Contact", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Email: admin@agendapp.com", style = MaterialTheme.typography.bodyMedium)
                    Text("Phone: +1 (555) 123-4567", style = MaterialTheme.typography.bodyMedium)
                  }
                }
          }
        }
  }
}
