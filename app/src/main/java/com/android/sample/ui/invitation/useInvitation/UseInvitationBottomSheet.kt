package com.android.sample.ui.invitation.useInvitation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.model.organization.invitation.Invitation.Companion.INVITATION_CODE_LENGTH
import com.android.sample.ui.common.BottomNavigationButtons
import com.android.sample.ui.theme.PaddingLarge
import com.android.sample.ui.theme.PaddingSmall
import com.android.sample.ui.theme.SpacingSmall

// Assisted by AI

/** Contains UI test tags used throughout the Use Invitation bottom sheet. */
object UseInvitationTestTags {
  const val INVITATION_CODE_TEXT_FIELD = "invitation_code_text_field"
  const val CANCEL_BUTTON = "cancel_button"
  const val JOIN_BUTTON = "join_button"
}

/**
 * A bottom-sheet UI component that allows the user to enter an invitation code to join an
 * organization.
 *
 * @param onCancel Callback invoked when the user presses the cancel button.
 * @param onJoin Callback invoked when the user presses the join button.
 */
@Composable
fun UseInvitationBottomSheet(
    useInvitationViewModel: UseInvitationViewModel = viewModel(),
    onCancel: () -> Unit = {},
    onJoin: (String) -> Unit = {}
) {

  val uiState by useInvitationViewModel.uiState.collectAsStateWithLifecycle()

  Column(
      modifier = Modifier.fillMaxWidth().padding(PaddingLarge),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        OutlinedTextField(
            value = uiState.code,
            onValueChange = { useInvitationViewModel.setCode(it) },
            placeholder = { Text(stringResource(R.string.enter_invitation_code)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = PaddingSmall)
                    .testTag(UseInvitationTestTags.INVITATION_CODE_TEXT_FIELD))

        Spacer(Modifier.height(SpacingSmall))

        BottomNavigationButtons(
            onNext = { useInvitationViewModel.joinWithCode() },
            onBack = onCancel,
            backButtonText = stringResource(R.string.cancel),
            nextButtonText = stringResource(R.string.join_button_text),
            canGoNext = uiState.code.length == INVITATION_CODE_LENGTH,
            backButtonTestTag = UseInvitationTestTags.CANCEL_BUTTON,
            nextButtonTestTag = UseInvitationTestTags.JOIN_BUTTON)
      }
}

@Preview(showBackground = true)
@Composable
fun UseInvitationBottomSheetPreview() {
  UseInvitationBottomSheet()
}
