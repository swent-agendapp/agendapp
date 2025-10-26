package com.android.sample.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

object ProfileScreenTestTags {
    const val PROFILE_SCREEN = "profile_screen"
    const val BACK_BUTTON = "back_button"
    const val DISPLAY_NAME_FIELD = "display_name_field"
    const val EMAIL_FIELD = "email_field"
    const val PHONE_FIELD = "phone_field"
    const val SAVE_BUTTON = "save_button"
    const val CANCEL_BUTTON = "cancel_button"
    const val EDIT_BUTTON = "edit_button"
    const val ADMIN_CONTACT_BUTTON = "admin_contact_button"
}

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToAdminContact: () -> Unit = {},
    profileViewModel: ProfileViewModel = viewModel()
) {
    val uiState by profileViewModel.uiState.collectAsState()

    var isEditMode by remember { mutableStateOf(false) }

    var displayName by remember { mutableStateOf(uiState.displayName) }
    var email by remember { mutableStateOf(uiState.email) }
    var phoneNumber by remember { mutableStateOf(uiState.phoneNumber) }

    // Validation error states
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier =
            Modifier.fillMaxSize().semantics { testTag = ProfileScreenTestTags.PROFILE_SCREEN }) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally) {
            // Back Button
            Button(
                modifier =
                    Modifier.testTag(ProfileScreenTestTags.BACK_BUTTON).align(Alignment.Start),
                onClick = onNavigateBack) {
                Text("Back")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title Row with edit/save/cancel icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("User Profile", style = MaterialTheme.typography.headlineMedium)

                if (!isEditMode) {
                    IconButton(
                        onClick = { isEditMode = true },
                        modifier = Modifier.testTag(ProfileScreenTestTags.EDIT_BUTTON)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                } else {
                    Row {
                        IconButton(
                            onClick = {
                                // Cancel edits → reset local values
                                displayName = uiState.displayName
                                email = uiState.email
                                phoneNumber = uiState.phoneNumber
                                emailError = null
                                phoneError = null
                                isEditMode = false
                            },
                            modifier = Modifier.testTag(ProfileScreenTestTags.CANCEL_BUTTON)) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                        IconButton(
                            onClick = {
                                // Validate before saving
                                val emailValid = isValidEmail(email)
                                val phoneValid = isValidPhone(phoneNumber)

                                emailError =
                                    if (!emailValid) "Please enter a valid email address" else null
                                phoneError =
                                    if (!phoneValid)
                                        "Please enter a valid phone number (min 7 digits)"
                                    else null

                                if (emailValid && phoneValid) {
                                    profileViewModel.updateDisplayName(displayName)
                                    profileViewModel.updateEmail(email)
                                    profileViewModel.updatePhoneNumber(phoneNumber)
                                    profileViewModel.saveProfile()
                                    isEditMode = false
                                }
                            },
                            modifier = Modifier.testTag(ProfileScreenTestTags.SAVE_BUTTON)) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display Name Field
            OutlinedTextField(
                value = if (isEditMode) displayName else uiState.displayName,
                onValueChange = { if (isEditMode) displayName = it },
                label = { Text("Display Name") },
                modifier =
                    Modifier.fillMaxWidth().testTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD),
                singleLine = true,
                enabled = isEditMode)

            Spacer(modifier = Modifier.height(16.dp))

            // Email Field
            OutlinedTextField(
                value = if (isEditMode) email else uiState.email,
                onValueChange = {
                    if (isEditMode) {
                        email = it
                        emailError = null
                    }
                },
                label = { Text("Email") },
                isError = emailError != null,
                supportingText = {
                    emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth().testTag(ProfileScreenTestTags.EMAIL_FIELD),
                singleLine = true,
                enabled = isEditMode,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Field
            OutlinedTextField(
                value = if (isEditMode) phoneNumber else uiState.phoneNumber,
                onValueChange = {
                    if (isEditMode) {
                        phoneNumber = it
                        phoneError = null
                    }
                },
                label = { Text("Phone Number") },
                isError = phoneError != null,
                supportingText = {
                    phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                },
                modifier = Modifier.fillMaxWidth().testTag(ProfileScreenTestTags.PHONE_FIELD),
                singleLine = true,
                enabled = isEditMode,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))

            Spacer(modifier = Modifier.height(24.dp))

            // Admin Contact Button
            OutlinedButton(
                modifier =
                    Modifier.testTag(ProfileScreenTestTags.ADMIN_CONTACT_BUTTON).fillMaxWidth(),
                onClick = onNavigateToAdminContact,
                enabled = !isEditMode) {
                Text("View Admin Contact")
            }
        }
    }
}

/** Utility validation functions */
private fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

private fun isValidPhone(phone: String): Boolean {
    val digits = phone.filter { it.isDigit() }
    return digits.length >= 7 // basic length check
}