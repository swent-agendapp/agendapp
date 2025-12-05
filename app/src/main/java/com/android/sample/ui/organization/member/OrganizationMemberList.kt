package com.android.sample.ui.organization.member

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun OrganizationMemberList(organizationId: String, onInvitationCodesClick: () -> Unit = {}) {

  // Placeholder for a future member management screen for an organization
  Button(onClick = onInvitationCodesClick) { Text("Create Invitation") }
}
