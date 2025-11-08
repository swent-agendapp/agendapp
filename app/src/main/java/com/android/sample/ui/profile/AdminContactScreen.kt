package com.android.sample.ui.profile

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.core.net.toUri
import com.android.sample.R
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.SpacingSmall

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminContactScreen(onNavigateBack: () -> Unit = {}) {
  val context = LocalContext.current

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.admin_contact_title)) },
            navigationIcon = {
              IconButton(
                  onClick = onNavigateBack,
                  modifier = Modifier.testTag(AdminContactScreenTestTags.BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back))
                  }
            })
      }) { innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize().padding(innerPadding).padding(PaddingMedium).semantics {
                  testTag = AdminContactScreenTestTags.ADMIN_SCREEN_PROFILE
                },
            horizontalAlignment = Alignment.CenterHorizontally) {

              // Admin Email
              Card(modifier = Modifier.fillMaxWidth().padding(vertical = SpacingSmall)) {
                Column(modifier = Modifier.padding(SpacingLarge)) {
                  Text(
                      stringResource(R.string.admin_contact_email_label),
                      style = MaterialTheme.typography.labelMedium)
                  Text(
                      AdminInformation.EMAIL,
                      style = MaterialTheme.typography.bodyLarge,
                      modifier =
                          Modifier.testTag(AdminContactScreenTestTags.ADMIN_EMAIL_TEXT).clickable {
                            val intent =
                                Intent(
                                        Intent.ACTION_SENDTO,
                                        "mailto:${AdminInformation.EMAIL}".toUri())
                                    .apply {
                                      putExtra(Intent.EXTRA_EMAIL, arrayOf(AdminInformation.EMAIL))
                                      putExtra(
                                          Intent.EXTRA_SUBJECT,
                                          context.getString(R.string.admin_contact_email_subject))
                                    }
                            context.startActivity(intent)
                          })
                }
              }

              // Admin Phone
              Card(modifier = Modifier.fillMaxWidth().padding(vertical = SpacingSmall)) {
                Column(modifier = Modifier.padding(SpacingLarge)) {
                  Text(
                      stringResource(R.string.admin_contact_phone_label),
                      style = MaterialTheme.typography.labelMedium)
                  Text(
                      AdminInformation.PHONE,
                      style = MaterialTheme.typography.bodyLarge,
                      modifier =
                          Modifier.testTag(AdminContactScreenTestTags.ADMIN_PHONE_TEXT).clickable {
                            val intent =
                                Intent(
                                    Intent.ACTION_DIAL,
                                    "tel:${AdminInformation.PHONE.replace(" ", "")}".toUri())
                            context.startActivity(intent)
                          })
                }
              }
            }
      }
}
