package com.android.sample.ui.navigation

import androidx.navigation.NavHostController

class NavigationActions(val navController: NavHostController) {

  fun navigateToEdit(eventId: String) {
    navController.navigate("edit_event/$eventId")
  }

  fun navigateBack() {
    navController.popBackStack()
  }
}
