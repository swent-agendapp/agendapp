package com.github.se.bootcamp.ui.authentication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.ui.theme.Salmon
import com.github.se.bootcamp.ui.authentication.SignInScreenTestTags.END_SNACK_BAR

object SignInScreenTestTags {
  const val APP_LOGO = "appLogo"
  const val LOGIN_TITLE = "loginTitle"
  const val LOGIN_MESSAGE = "loginMessage"
  const val LOGIN_WELCOME = "loginWelcome"
  const val LOGIN_BUTTON = "loginButton"
  const val END_SNACK_BAR = "snackBar"
}

@Composable
fun SignInScreen(
    authViewModel: SignInViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedIn: () -> Unit = {},
) {

  val context = LocalContext.current
  val uiState by authViewModel.uiState.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(uiState.user) {
    uiState.user?.let {
      snackbarHostState.showSnackbar("Login successful!", duration = SnackbarDuration.Long)
      onSignedIn()
    }
  }

  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      snackbarHostState.showSnackbar(uiState.errorMsg.toString(), duration = SnackbarDuration.Long)
    }
  }

  // The main container for the screen
  // A surface container using the 'background' color from the theme
  Scaffold(
      modifier = Modifier.fillMaxSize(),
      snackbarHost = {
        SnackbarHost(snackbarHostState, modifier = Modifier.testTag(END_SNACK_BAR))
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
              contentDescription = "App Logo",
              modifier = Modifier.size(250.dp).testTag(SignInScreenTestTags.APP_LOGO))

          Text(
              modifier = Modifier.testTag(SignInScreenTestTags.LOGIN_TITLE),
              text = "Agendapp",
              style =
                  MaterialTheme.typography.headlineLarge.copy(
                      fontSize = 57.sp, lineHeight = 50.sp, color = Salmon),
              fontWeight = FontWeight.Bold,
              // center the text
              textAlign = TextAlign.Center)
          Text(
              modifier = Modifier.testTag(SignInScreenTestTags.LOGIN_MESSAGE),
              text = "Plan, track and manage",
              style =
                  MaterialTheme.typography.headlineLarge.copy(
                      fontSize = 20.sp, lineHeight = 24.sp, color = Salmon),
              fontWeight = FontWeight.Bold,
              // center the text
              textAlign = TextAlign.Center)

          Spacer(modifier = Modifier.height(48.dp))

          // Welcome Text
          Text(
              modifier = Modifier.testTag(SignInScreenTestTags.LOGIN_WELCOME),
              text = "Welcome",
              style =
                  MaterialTheme.typography.headlineLarge.copy(fontSize = 57.sp, lineHeight = 64.sp),
              fontWeight = FontWeight.Bold,
              // center the text
              textAlign = TextAlign.Center)

          Spacer(modifier = Modifier.height(48.dp))

          // Authenticate With Google Button
          if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
          } else {
            GoogleSignInButton(onSignInClick = { authViewModel.signIn(context, credentialManager) })
          }
        }
      })
}

@Composable
fun GoogleSignInButton(onSignInClick: () -> Unit) {
  Button(
      onClick = onSignInClick,
      colors = ButtonDefaults.buttonColors(containerColor = Salmon), // Button color
      shape = RoundedCornerShape(50), // Circular edges for the button
      border = BorderStroke(1.dp, Color.Transparent),
      modifier =
          Modifier.padding(8.dp)
              .height(48.dp)
              .width(250.dp)
              .testTag(SignInScreenTestTags.LOGIN_BUTTON)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()) {

              // Text for the button
              Text(
                  text = "Sign in with Google",
                  color = Color.White, // Text color
                  fontSize = 16.sp, // Font size
                  fontWeight = FontWeight.Medium)
            }
      }
}
