package com.android.sample.ui.category

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.R
import com.android.sample.model.category.EventCategory
import com.android.sample.ui.category.EditCategoryScreenTestTags.CATEGORY_CARD_ADD_BUTTON
import com.android.sample.ui.category.components.CategoryEditBottomSheet
import com.android.sample.ui.category.components.CategoryListSection
import com.android.sample.ui.common.FloatingButton
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.theme.PaddingLarge
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.rememberReorderableLazyListState

// Assisted by AI

// --- Test tags to use in UI tests ---
object EditCategoryScreenTestTags {
  const val SCREEN_ROOT = "edit_category_screen_root"
  const val LOADING = "edit_category_loading"
  const val CATEGORY_LIST = "edit_category_list"
  const val CATEGORY_CARD = "edit_category_card"
  const val CATEGORY_CARD_LABEL = "edit_category_card_label"
  const val CATEGORY_CARD_ADD_BUTTON = "edit_category_card_add_button"
  const val CATEGORY_CARD_EDIT_BUTTON = "edit_category_card_edit_button"
  const val CATEGORY_CARD_DELETE_BUTTON = "edit_category_card_delete_button"
  const val EMPTY_STATE_TITLE = "edit_category_empty_state_title"
  const val EMPTY_STATE_MESSAGE = "edit_category_empty_state_message"
  const val BOTTOM_SHEET = "edit_category_bottom_sheet"
  const val BOTTOM_SHEET_LABEL_TEXT_FIELD = "edit_category_label_text_field"
  const val BOTTOM_SHEET_SAVE_BUTTON = "edit_category_save_button"
}

/**
 * Screen that lets an admin view and manage event categories.
 *
 * This screen displays the current list of categories and allows the user to:
 * - Reorder categories via drag & drop.
 * - Create a new category (FAB opens a bottom sheet with an empty draft).
 * - Edit an existing category (opens the same bottom sheet pre-filled).
 * - Delete a category (shows a confirmation dialog).
 *
 * All UI state and actions are driven by [EditCategoryViewModel]: the list of categories, the
 * currently selected category (draft), the bottom sheet visibility, and the delete confirmation
 * dialog state.
 *
 * @param editCategoryViewModel ViewModel that owns the UI state and handles all user actions.
 * @param onBack Callback invoked when the user presses the back button in the top bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryScreen(
    editCategoryViewModel: EditCategoryViewModel = viewModel(),
    onBack: () -> Unit = {},
) {
  val uiState by editCategoryViewModel.uiState.collectAsState()
  val errorMsg = uiState.errorMessage
  val isLoading = uiState.isLoading
  val context = LocalContext.current

  // Bottom sheet is controlled by the Compose sheetState (not by local boolean flags).
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  // Load categories when the screen is first shown.
  LaunchedEffect(Unit) { editCategoryViewModel.refreshUIState() }

  LaunchedEffect(errorMsg) {
    if (errorMsg != null) {
      Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
      editCategoryViewModel.clearErrorMsg()
    }
  }

  val categories: List<EventCategory> = uiState.categories

  val reorderState: ReorderableLazyListState =
      rememberReorderableLazyListState(
          onMove = { from, to -> editCategoryViewModel.moveCategory(from, to) })

  // Delete dialog (driven by ViewModel state).
  EditCategoryDeleteDialog(
      showDeleteDialog = uiState.showDeleteDialog,
      category = categories.firstOrNull { it.id == uiState.selectedCategoryId },
      onConfirm = { editCategoryViewModel.confirmDeleteSelectedCategory() },
      onDismiss = { editCategoryViewModel.dismissDeleteDialog() },
  )

  Scaffold(
      topBar = {
        SecondaryPageTopBar(
            title = stringResource(R.string.edit_category_title),
            onClick = onBack,
        )
      },
      floatingActionButton = {
        FloatingButton(
            modifier = Modifier.testTag(CATEGORY_CARD_ADD_BUTTON),
            onClick = {
              // Start a new draft.
              editCategoryViewModel.resetSelectedCategory()
              editCategoryViewModel.openCreateCategoryBottomSheet()
            },
        )
      },
  ) { paddingValues ->
    Column {
      CategoryListSection(
          modifier =
              Modifier.padding(paddingValues)
                  .padding(PaddingLarge)
                  .testTag(EditCategoryScreenTestTags.SCREEN_ROOT),
          categories = categories,
          reorderState = reorderState,
          // Pass the category id string directly to the ViewModel.
          onEditCategory = { categoryId ->
            editCategoryViewModel.openEditCategoryBottomSheet(categoryId)
          },
          onDeleteCategory = { category ->
            // This will load the category into the ViewModel (async) and show the dialog.
            editCategoryViewModel.askDeleteCategory(category.id)
          },
          isLoading = isLoading,
      )
    }

    CategoryEditBottomSheet(
        // Use the ViewModel's showBottomSheet state for visibility.
        showBottomSheet = uiState.showBottomSheet,
        sheetState = sheetState,
        initialLabel = uiState.selectedCategoryLabel,
        initialColor = uiState.selectedCategoryColor,
        onDismiss = {
          // Close the sheet and clear the draft.
          editCategoryViewModel.dismissBottomSheet()
        },
        // Save callback receives label and color, updates the ViewModel, then saves.
        onSave = { label, color ->
          editCategoryViewModel.setSelectedCategoryLabel(label)
          editCategoryViewModel.setSelectedCategoryColor(color)
          editCategoryViewModel.saveCategory()
        },
    )
  }
}

@Composable
private fun EditCategoryDeleteDialog(
    showDeleteDialog: Boolean = false,
    category: EventCategory? = null,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
  if (showDeleteDialog && category != null) {
    DeleteCategoryConfirmationDialog(
        category = category,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
  }
}

@Composable
private fun DeleteCategoryConfirmationDialog(
    category: EventCategory = EventCategory.defaultCategory(),
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
  AlertDialog(
      onDismissRequest = onDismiss,
      confirmButton = {
        TextButton(onClick = onConfirm) {
          Text(
              text = stringResource(R.string.delete),
              color = MaterialTheme.colorScheme.error,
              fontWeight = FontWeight.Bold,
          )
        }
      },
      dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
      title = { Text("${stringResource(R.string.delete)} ${category.label}") },
      text = { Text(stringResource(R.string.delete_category_message)) },
  )
}

@Preview(showBackground = true)
@Composable
fun EditCategoryScreenPreview() {
  EditCategoryScreen()
}
