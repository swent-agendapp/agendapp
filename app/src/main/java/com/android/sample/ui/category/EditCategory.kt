package com.android.sample.ui.category

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.model.category.EventCategory
import com.android.sample.model.category.mockData.getMockEventCategory
import com.android.sample.ui.category.EditCategoryScreenTestTags.CATEGORY_CARD_ADD_BUTTON
import com.android.sample.ui.category.components.CategoryEditBottomSheet
import com.android.sample.ui.category.components.CategoryListSection
import com.android.sample.ui.common.FloatingButton
import com.android.sample.ui.common.SecondaryPageTopBar
import com.android.sample.ui.theme.PaddingLarge
import org.burnoutcrew.reorderable.rememberReorderableLazyListState

// Assisted by AI

// --- Test tags to use in UI tests ---
object EditCategoryScreenTestTags {
  const val SCREEN_ROOT = "edit_category_screen_root"
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
 * Screen that lets the admin view, reorder and customize their event categories.
 *
 * NOW: uses local state with mock categories (getMockEventCategory()). LATER: this will be backed
 * by an EditCategoryViewModel and its uiState.
 *
 * @param onBack callback invoked when the user taps the top bar back button.
 */
// FOR NOW: use local state + mock categories (getMockEventCategory()).
// LATER: we will replace local state with an EditCategoryViewModel and its uiState.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryScreen(
    // LATER: editCategoryViewModel: EditCategoryViewModel
    onBack: () -> Unit = {},
) {
  // --- State setup ---

  // NOW: local mock categories stored as state
  // LATER: replace with editCategoryViewModel.uiState.categories
  var categories by remember { mutableStateOf(getMockEventCategory()) }

  // Selected category (for editing in bottom sheet)
  // LATER: exposed by the ViewModel's uiState as selectedCategory
  var selectedCategory by remember { mutableStateOf<EventCategory?>(null) }

  // Index of selected category in the current list
  // This is a temporary trick; LATER this will be handled entirely by the ViewModel
  var selectedCategoryIndex by remember { mutableIntStateOf(-1) }

  val showDeleteDialog = remember { mutableStateOf(false) }

  // Bottom sheet state.
  val sheetState = rememberModalBottomSheetState()
  var showBottomSheet by remember { mutableStateOf(false) }

  // For drag & drop reordering
  // NOW: move in local state list
  // LATER: call editCategoryViewModel.moveCategory(fromIndex, toIndex)
  val reorderState =
      rememberReorderableLazyListState(
          onMove = { from, to ->
            categories = categories.toMutableList().apply { move(from.index, to.index) }
          })

  // NOW: helper to clear the local selection state.
  // LATER: selection will be owned by the ViewModel and cleared via a ViewModel event.
  fun resetSelection() {
    selectedCategory = null
    selectedCategoryIndex = -1
  }

  // NOW: prepare local state before showing the delete dialog.
  // LATER: the ViewModel will expose an intent (for example onAskDeleteCategory(categoryId)) and
  // own both the selected category and the delete dialog visibility.
  fun askDeleteCategory(category: EventCategory) {
    selectedCategory = category
    selectedCategoryIndex = categories.indexOfFirst { it == category }
    showDeleteDialog.value = true
  }

  // NOW: close the delete dialog and reset local selection.
  // LATER: the ViewModel will handle closing the dialog and clearing the selection.
  fun dismissDeleteDialog() {
    showDeleteDialog.value = false
    resetSelection()
  }

  // NOW: delete the selected category from the local list and close the dialog.
  // LATER: the ViewModel will perform the deletion (for example onConfirmDelete()) and update its
  // uiState accordingly.
  fun deleteSelectedCategoryAndDismissDialog() {
    val categoryToDelete = selectedCategory
    showDeleteDialog.value = false
    if (categoryToDelete != null) {
      // LATER: call editCategoryViewModel.deleteCategory(category.id)
      categories = categories.filterNot { it.id == categoryToDelete.id }
    }
    resetSelection()
  }

  // NOW: close the bottom sheet and reset local selection.
  // LATER: the ViewModel will expose an intent to close the sheet and reset its uiState.
  fun dismissBottomSheet() {
    showBottomSheet = false
    resetSelection()
  }

  // NOW: create or update a category directly in the local list when the user saves.
  // LATER: the ViewModel will expose a save intent (for example onSaveCategory(label, color)) and
  // handle creation / edition, then the UI will only observe the new uiState.
  fun saveCategory(label: String, color: Color) {
    val trimmedLabel = label.trim()
    if (trimmedLabel.isEmpty()) return

    // NOW: update local list
    // LATER: call ViewModel to create or update the category
    if (selectedCategoryIndex in categories.indices) {
      // Edit existing category
      val updated =
          categories.mapIndexed { index, oldCategory ->
            if (index == selectedCategoryIndex) {
              oldCategory.copy(label = trimmedLabel, color = color)
            } else {
              oldCategory
            }
          }
      categories = updated
    } else {
      // Add new category
      categories =
          categories + EventCategory(label = trimmedLabel, color = color, isDefault = false)
    }

    showBottomSheet = false
    resetSelection()
  }

  // LATER: use a LaunchedEffect for displaying the error msg

  EditCategoryDeleteDialog(
      showDeleteDialog = showDeleteDialog.value,
      category = selectedCategory,
      onConfirm = { deleteSelectedCategoryAndDismissDialog() },
      onDismiss = { dismissDeleteDialog() })

  EditCategoryScaffold(
      onBack = onBack,
      categories = categories,
      reorderState = reorderState,
      onCreateCategory = {
        resetSelection()
        showBottomSheet = true
      },
      onEditCategory = { category ->
        selectedCategory = category
        selectedCategoryIndex = categories.indexOfFirst { it == category }
        showBottomSheet = true
      },
      onDeleteCategory = { category -> askDeleteCategory(category) },
      showBottomSheet = showBottomSheet,
      sheetState = sheetState,
      selectedCategory = selectedCategory,
      onDismissBottomSheet = { dismissBottomSheet() },
      onSaveCategory = { label, color -> saveCategory(label, color) },
  )
}

@Composable
private fun EditCategoryDeleteDialog(
    showDeleteDialog: Boolean,
    category: EventCategory?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
  // Delete confirmation dialog
  if (showDeleteDialog && category != null) {
    DeleteCategoryConfirmationDialog(
        category = category,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditCategoryScaffold(
    onBack: () -> Unit,
    categories: List<EventCategory>,
    reorderState: org.burnoutcrew.reorderable.ReorderableLazyListState,
    onCreateCategory: () -> Unit,
    onEditCategory: (EventCategory) -> Unit,
    onDeleteCategory: (EventCategory) -> Unit,
    showBottomSheet: Boolean,
    sheetState: androidx.compose.material3.SheetState,
    selectedCategory: EventCategory?,
    onDismissBottomSheet: () -> Unit,
    onSaveCategory: (String, Color) -> Unit,
) {
  Scaffold(
      topBar = {
        SecondaryPageTopBar(title = stringResource(R.string.edit_category_title), onClick = onBack)
      },
      floatingActionButton = {
        FloatingButton(
            modifier = Modifier.testTag(CATEGORY_CARD_ADD_BUTTON),
            onClick = {
              // Create a new category (no pre-filled data)
              onCreateCategory()
            })
      }) { paddingValues ->
        CategoryListSection(
            modifier =
                Modifier.padding(paddingValues)
                    .padding(horizontal = PaddingLarge, vertical = PaddingLarge)
                    .testTag(EditCategoryScreenTestTags.SCREEN_ROOT),
            categories = categories,
            reorderState = reorderState,
            onEditCategory = { category -> onEditCategory(category) },
            onDeleteCategory = { category -> onDeleteCategory(category) },
        )

        CategoryEditBottomSheet(
            showBottomSheet = showBottomSheet,
            sheetState = sheetState,
            initialLabel = selectedCategory?.label,
            initialColor = selectedCategory?.color,
            onDismiss = { onDismissBottomSheet() },
            onSave = { label, color -> onSaveCategory(label, color) },
        )
      }
}

@Composable
private fun DeleteCategoryConfirmationDialog(
    category: EventCategory,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
  AlertDialog(
      onDismissRequest = onDismiss,
      confirmButton = {
        TextButton(
            onClick = onConfirm,
        ) {
          Text(
              text = stringResource(R.string.delete),
              color = MaterialTheme.colorScheme.error,
              fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(
            onClick = onDismiss,
        ) {
          Text(stringResource(R.string.cancel))
        }
      },
      title = { Text("${stringResource(R.string.delete)} ${category.label}") },
      text = { Text(stringResource(R.string.delete_category_message)) })
}

/**
 * Simple helper for moving an item inside a mutable list. Used only for the local state version of
 * the screen.
 */
private fun <T> MutableList<T>.move(fromIndex: Int, toIndex: Int) {
  if (fromIndex == toIndex) return
  val item = removeAt(fromIndex)
  add(toIndex, item)
}

@Preview(showBackground = true)
@Composable
fun EditCategoryScreenPreview() {
  EditCategoryScreen()
}
