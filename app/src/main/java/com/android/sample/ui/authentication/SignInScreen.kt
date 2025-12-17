package com.android.sample.ui.authentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.authentication.SignInScreenTestTags.END_SNACK_BAR
import com.android.sample.ui.common.Loading
import com.android.sample.ui.common.PrimaryButton
import com.android.sample.ui.theme.FontSizeExtraHuge
import com.android.sample.ui.theme.FontSizeLarge
import com.android.sample.ui.theme.FontSizeMassive
import com.android.sample.ui.theme.GeneralPalette
import com.android.sample.ui.theme.SizeExtraLarge
import com.android.sample.ui.theme.SizeMassive
import com.android.sample.ui.theme.SpacingMassive

object SignInScreenTestTags {
  const val APP_LOGO = "appLogo"
  const val LOGIN_TITLE = "loginTitle"
  const val LOGIN_MESSAGE = "loginMessage"
  const val LOGIN_WELCOME = "loginWelcome"
  const val LOGIN_BUTTON = "loginButton"
  const val LOGOUT_BUTTON = "logoutButton"
  const val END_SNACK_BAR = "snackBar"
  const val NO_NETWORK_MESSAGE = "noNetworkMessage"
}

@Composable
fun SignInScreen(
    authViewModel: SignInViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedIn: () -> Unit = {},
) {

  val context = LocalContext.current
  val uiState by authViewModel.uiState.collectAsState()
  val snackBarHostState = remember { SnackbarHostState() }

  LaunchedEffect(uiState.user) { uiState.user?.let { onSignedIn() } }

  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      snackBarHostState.showSnackbar(uiState.errorMsg.toString(), duration = SnackbarDuration.Long)
      authViewModel.clearErrorMsg()
    }
  }

  // The main container for the screen
  // A surface container using the 'background' color from the theme
  Scaffold(
      modifier = Modifier.fillMaxSize(),
      snackbarHost = {
        SnackbarHost(snackBarHostState, modifier = Modifier.testTag(END_SNACK_BAR))
      },
      content = { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
          // App Logo Image
          Image(
              painter = painterResource(id = R.drawable.app_logo), // Ensure this drawable exists
              contentDescription = stringResource(R.string.sign_in_logo_content_description),
              modifier = Modifier.size(SizeMassive).testTag(SignInScreenTestTags.APP_LOGO))

          Text(
              modifier = Modifier.testTag(SignInScreenTestTags.LOGIN_TITLE),
              text = stringResource(R.string.sign_in_app_title),
              style =
                  MaterialTheme.typography.headlineLarge.copy(
                      fontSize = FontSizeExtraHuge, color = GeneralPalette.Primary),
              fontWeight = FontWeight.Bold,
              // center the text
              textAlign = TextAlign.Center)
          Text(
              modifier = Modifier.testTag(SignInScreenTestTags.LOGIN_MESSAGE),
              text = stringResource(R.string.sign_in_message),
              style =
                  MaterialTheme.typography.headlineLarge.copy(
                      fontSize = FontSizeLarge, color = GeneralPalette.Primary),
              fontWeight = FontWeight.Bold,
              // center the text
              textAlign = TextAlign.Center)

          Spacer(modifier = Modifier.height(SpacingMassive))

          // Welcome Text
          Text(
              modifier = Modifier.testTag(SignInScreenTestTags.LOGIN_WELCOME),
              text = stringResource(R.string.sign_in_welcome),
              style = MaterialTheme.typography.headlineLarge.copy(fontSize = FontSizeMassive),
              fontWeight = FontWeight.Bold,
              // center the text
              textAlign = TextAlign.Center)

          Spacer(modifier = Modifier.height(SpacingMassive))

          // Authenticate With Google Button
          if (uiState.isLoading) {
            Loading(modifier = Modifier.size(SizeExtraLarge))
          } else if (uiState.signedOut) {
            GoogleSignInButton(
                onSignInClick = { authViewModel.signIn(context, credentialManager) },
                enabled = uiState.isNetworkAvailable)
          }
        }
      })
}

@Composable
fun GoogleSignInButton(onSignInClick: () -> Unit, enabled: Boolean = true) {
  PrimaryButton(
      modifier = Modifier.testTag(tag = SignInScreenTestTags.LOGIN_BUTTON),
      text = stringResource(R.string.sign_in_button_text),
      onClick = onSignInClick,
      enabled = enabled,
      unabledText = stringResource(R.string.network_error_message),
      showUnabledText = true,
      unabledTextTestTag = SignInScreenTestTags.NO_NETWORK_MESSAGE)
}
