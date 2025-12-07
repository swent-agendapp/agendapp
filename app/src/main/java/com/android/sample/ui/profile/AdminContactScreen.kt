package com.android.sample.ui.profile

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.core.net.toUri
import com.android.sample.R
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.theme.AlphaExtraLow
import com.android.sample.ui.theme.CornerRadiusExtraLarge
import com.android.sample.ui.theme.PaddingMedium
import com.android.sample.ui.theme.SpacingLarge
import com.android.sample.ui.theme.SpacingSmall

object AdminContactScreenTestTags {
  const val ADMIN_SCREEN_PROFILE = "admin_contact_screen"
  const val BACK_BUTTON = "back_button"
  const val ADMIN_EMAIL_TEXT = "admin_email_text"
  const val ADMIN_PHONE_TEXT = "admin_phone_text"
}

object AdminInformation {
  const val EMAIL = "admin@agendapp.com"
  const val PHONE = "079 123 45 67"
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
        SecondaryPageTopBar(
            title = stringResource(R.string.admin_contact_title),
            onClick = onNavigateBack,
            backButtonTestTags = AdminContactScreenTestTags.BACK_BUTTON)
      }) { innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize().padding(innerPadding).padding(PaddingMedium).semantics {
                  testTag = AdminContactScreenTestTags.ADMIN_SCREEN_PROFILE
                },
            horizontalAlignment = Alignment.CenterHorizontally) {

              // Email admin
              AdminInfoRow(
                  label = stringResource(R.string.admin_contact_email_label),
                  value = AdminInformation.EMAIL,
                  testTag = AdminContactScreenTestTags.ADMIN_EMAIL_TEXT,
                  onClick = {
                    val intent =
                        Intent(Intent.ACTION_SENDTO, "mailto:${AdminInformation.EMAIL}".toUri())
                            .apply {
                              putExtra(Intent.EXTRA_EMAIL, arrayOf(AdminInformation.EMAIL))
                              putExtra(
                                  Intent.EXTRA_SUBJECT,
                                  context.getString(R.string.admin_contact_email_subject))
                            }
                    context.startActivity(intent)
                  })

              Spacer(modifier = Modifier.height(SpacingLarge))

              // Phone number admin
              AdminInfoRow(
                  label = stringResource(R.string.admin_contact_phone_label),
                  value = AdminInformation.PHONE,
                  testTag = AdminContactScreenTestTags.ADMIN_PHONE_TEXT,
                  onClick = {
                    val intent =
                        Intent(
                            Intent.ACTION_DIAL,
                            "tel:${AdminInformation.PHONE.replace(" ", "")}".toUri())
                    context.startActivity(intent)
                  })
            }
      }
}

@Composable
private fun AdminInfoRow(
    label: String,
    value: String,
    testTag: String,
    onClick: () -> Unit,
) {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(CornerRadiusExtraLarge))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AlphaExtraLow))
              .clickable(onClick = onClick)
              .padding(PaddingMedium)
              .testTag(testTag)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(SpacingSmall))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface)
      }
}
