package com.android.sample.ui.organization.member

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

object OrganizationMemberListTestTags {
  const val ROOT = "organizationMemberListRoot"
  const val CREATE_INVITATION_BUTTON = "createInvitationButton"
}

// Unused parameter organizationId will be useful in the future
@Composable
fun OrganizationMemberList(organizationId: String, onInvitationCodesClick: () -> Unit = {}) {
  // Placeholder for a future member management screen for an organization
  Scaffold(
      modifier = Modifier.testTag(OrganizationMemberListTestTags.ROOT),
      content = { innerPadding ->
        Button(
            onClick = onInvitationCodesClick,
            modifier =
                Modifier.padding(innerPadding)
                    .testTag(
                        // Hardcoded string for now, since it is temporary and for testing purpose
                        OrganizationMemberListTestTags.CREATE_INVITATION_BUTTON)) {
              Text("Create Invitation")
            }
      })
}
