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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.android.sample.R
import com.android.sample.model.category.EventCategory
import com.android.sample.model.category.mockData.getMockEventCategory
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
  const val CATEGORY_CARD_EDIT_BUTTON = "edit_category_card_edit_button"
  const val CATEGORY_CARD_DELETE_BUTTON = "edit_category_card_delete_button"
  const val EMPTY_STATE_TITLE = "edit_category_empty_state_title"
  const val EMPTY_STATE_MESSAGE = "edit_category_empty_state_message"
  const val BOTTOM_SHEET = "edit_category_bottom_sheet"
  const val BOTTOM_SHEET_LABEL_TEXT_FIELD = "edit_category_label_text_field"
  const val BOTTOM_SHEET_SAVE_BUTTON = "edit_category_save_button"
}

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

  // LATER: use a LaunchedEffect for displaying the error msg

  // Delete confirmation dialog
  if (showDeleteDialog.value && selectedCategory != null) {
    DeleteCategoryConfirmationDialog(
        category = selectedCategory!!,
        onConfirm = {
          val categoryToDelete = selectedCategory
          showDeleteDialog.value = false
          if (categoryToDelete != null) {
            // LATER: call editCategoryViewModel.deleteCategory(category.id)
            categories = categories.filterNot { it.id == categoryToDelete.id }
          }
          selectedCategory = null
          selectedCategoryIndex = -1
        },
        onDismiss = {
          showDeleteDialog.value = false
          selectedCategory = null
          selectedCategoryIndex = -1
        })
  }

  Scaffold(
      topBar = {
        SecondaryPageTopBar(title = stringResource(R.string.edit_category_title), onClick = onBack)
      },
      floatingActionButton = {
        FloatingButton(
            onClick = {
              // Create a new category (no pre-filled data)
              selectedCategory = null
              selectedCategoryIndex = -1
              showBottomSheet = true
            })
      }) { paddingValues ->
        CategoryListSection(
            modifier =
                Modifier.padding(paddingValues)
                    .padding(horizontal = PaddingLarge, vertical = PaddingLarge)
                    .testTag(EditCategoryScreenTestTags.SCREEN_ROOT),
            categories = categories,
            reorderState = reorderState,
            onEditCategory = { category ->
              // LATER: call editCategoryViewModel.selectCategory(category.id)
              selectedCategory = category
              selectedCategoryIndex = categories.indexOfFirst { it == category }
              showBottomSheet = true
            },
            onDeleteCategory = { category ->
              selectedCategory = category
              selectedCategoryIndex = categories.indexOfFirst { it == category }
              showDeleteDialog.value = true
            },
        )

        CategoryEditBottomSheet(
            showBottomSheet = showBottomSheet,
            sheetState = sheetState,
            initialLabel = selectedCategory?.label,
            initialColor = selectedCategory?.color,
            onDismiss = {
              showBottomSheet = false
              selectedCategory = null
              selectedCategoryIndex = -1
            },
            onSave = { label, color ->
              val trimmedLabel = label.trim()
              if (trimmedLabel.isEmpty()) return@CategoryEditBottomSheet

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
                    categories +
                        EventCategory(label = trimmedLabel, color = color, isDefault = false)
              }

              showBottomSheet = false
              selectedCategory = null
              selectedCategoryIndex = -1
            },
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
