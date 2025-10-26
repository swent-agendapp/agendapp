package com.android.sample.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp

object AdminContactScreenTestTags {
    const val ADMIN_SCREEN_PROFILE = "admin_contact_screen"
    const val BACK_BUTTON = "back_button"

    const val ADMIN_CONTACT = "Admin Contact"
    const val ADMIN_EMAIL_TEXT = "admin_email_text"
    const val ADMIN_PHONE_TEXT = "admin_phone_text"
}

object AdminInformation {
    const val EMAIL = "admin@agendapp.com"
    const val PHONE = "+1 (555) 123-4567"
}

/**
 * Admin contact screen displaying administrator contact information.
 *
 * @param onNavigateBack Callback to navigate back to previous screen
 */
@Composable
fun AdminContactScreen(onNavigateBack: () -> Unit = {}) {
    val context = LocalContext.current

    Surface(
        modifier =
            Modifier.fillMaxSize().semantics {
                testTag = AdminContactScreenTestTags.ADMIN_SCREEN_PROFILE
            }) {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(16.dp)
                    .testTag(AdminContactScreenTestTags.ADMIN_CONTACT),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally) {
            // Back Button
            Button(
                modifier =
                    Modifier.testTag(AdminContactScreenTestTags.BACK_BUTTON)
                        .align(Alignment.Start),
                onClick = onNavigateBack) {
                Text("Back")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text("Admin Contact", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(24.dp))

            // Admin Email
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Email", style = MaterialTheme.typography.labelMedium)
                    Text(
                        AdminInformation.EMAIL,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier =
                            Modifier
                                .testTag(AdminContactScreenTestTags.ADMIN_EMAIL_TEXT)
                                .clickable {
                                    val intent =
                                        Intent(
                                            Intent.ACTION_SENDTO,
                                            Uri.parse("mailto:${AdminInformation.EMAIL}"))
                                            .apply {
                                                putExtra(Intent.EXTRA_EMAIL, arrayOf(AdminInformation.EMAIL))
                                                putExtra(Intent.EXTRA_SUBJECT, "Contact from Agendapp")
                                            }
                                    context.startActivity(intent)
                                })
                }
            }

            // Admin Phone
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Phone", style = MaterialTheme.typography.labelMedium)
                    Text(
                        AdminInformation.PHONE,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier =
                            Modifier
                                .testTag(AdminContactScreenTestTags.ADMIN_PHONE_TEXT)
                                .clickable {
                                    val intent =
                                        Intent(
                                            Intent.ACTION_DIAL,
                                            Uri.parse("tel:${AdminInformation.PHONE.replace(" ", "")}"))
                                    context.startActivity(intent)
                                })
                }
            }
        }
    }
}