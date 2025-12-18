package com.android.sample.ui.category

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.category.EventCategory
import com.android.sample.model.category.EventCategoryRepository
import com.android.sample.model.category.EventCategoryRepositoryProvider
import com.android.sample.ui.organization.SelectedOrganizationVMProvider
import com.android.sample.ui.organization.SelectedOrganizationViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ItemPosition

/**
 * UI state for the Edit Categories screen.
 *
 * This state centralizes:
 * - the ordered list of categories for the selected organization
 * - the currently selected / edited category (draft fields)
 * - transient UI flags (loading, dialogs, bottom sheet)
 * - error messages to be displayed by the UI
 *
 * The state is immutable and updated through copy operations.
 */
data class EditCategoryUIState(
    val categories: List<EventCategory> = emptyList(),
    val selectedCategoryId: String? = null,
    val selectedCategoryLabel: String? = null,
    val selectedCategoryColor: Color? = null,
    val selectedCategoryIndex: Int = -1,
    val showDeleteDialog: Boolean = false,
    val showBottomSheet: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * ViewModel responsible for category management.
 *
 * Responsibilities:
 * - Load and refresh categories for the selected organization
 * - Handle creation, update, deletion, and reordering of categories
 * - Maintain a draft state while editing or creating a category
 * - Expose a single [EditCategoryUIState] consumed by the UI
 *
 * The ViewModel ensures:
 * - category ordering is stable and persisted using an `index` field
 * - validation is applied consistently for create and update flows
 * - UI reacts immediately to local changes, while persistence happens asynchronously
 */
class EditCategoryViewModel(
    private val categoryRepository: EventCategoryRepository =
        EventCategoryRepositoryProvider.repository,
    private val selectedOrganizationViewModel: SelectedOrganizationViewModel =
        SelectedOrganizationVMProvider.viewModel
) : ViewModel() {
  private val _uiState = MutableStateFlow(EditCategoryUIState())
  val uiState: StateFlow<EditCategoryUIState> = _uiState.asStateFlow()

  // Wrap for brevity
  private fun requireOrgId(): String = selectedOrganizationViewModel.getSelectedOrganizationId()

  private fun validateSelectedCategory(): String? {
    val state = _uiState.value

    return if (state.selectedCategoryId == null) {
      "No category selected"
    } else if (state.selectedCategoryLabel == null ||
        state.selectedCategoryLabel.trim().isEmpty()) {
      "The selected category has no name"
    } else if (state.selectedCategoryColor == null) {
      "The selected category has no color"
    } else null
  }

  /**
   * Validates the editable fields (label + color).
   *
   * This is used for both creating a new category and updating an existing one.
   */
  private fun validateCategoryDraft(label: String?, color: Color?): String? {
    return if (label == null || label.trim().isEmpty()) {
      "The selected category has no name"
    } else if (color == null) {
      "The selected category has no color"
    } else null
  }

  private fun setErrorMsg(message: String) {
    _uiState.value = _uiState.value.copy(errorMessage = message)
  }

  fun clearErrorMsg() {
    _uiState.value = _uiState.value.copy(errorMessage = null)
  }

  /**
   * Reloads all categories from the repository for the selected organization.
   *
   * This method:
   * - sets a loading state
   * - fetches categories from the repository
   * - normalizes category indexes if needed
   * - persists repaired indexes to guarantee stable ordering
   * - resets any current selection or transient UI state
   *
   * It should be called when returning to this screen after external changes.
   */
  fun refreshUIState() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

      try {
        val orgId = requireOrgId()

        val rawCategories = categoryRepository.getAllCategories(orgId = orgId)

        // If indexes are inconsistent (duplicates/gaps), normalize them to [0..n-1]
        val normalized =
            if (hasDenseSequentialIndexes(rawCategories)) {
              rawCategories.sortedWith(compareBy<EventCategory> { it.index }.thenBy { it.id })
            } else {
              val fixed = reindexCategories(rawCategories)

              // Persist the repaired indexes so future orderBy(index) is stable.
              fixed.forEach { category ->
                categoryRepository.updateCategory(
                    orgId = orgId, item = category, itemId = category.id)
              }
              fixed
            }

        _uiState.value =
            _uiState.value.copy(
                categories = normalized,
                isLoading = false,
                selectedCategoryId = null,
                selectedCategoryLabel = null,
                selectedCategoryColor = null,
                selectedCategoryIndex = -1,
                showDeleteDialog = false,
                showBottomSheet = false,
            )
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(
                isLoading = false, errorMessage = "Failed to load categories: ${e.message}")
      }
    }
  }

  /**
   * Clears the currently selected category and its draft fields.
   *
   * This is typically called when:
   * - dismissing dialogs or bottom sheets
   * - after completing create, update, or delete operations
   */
  fun resetSelectedCategory() {
    _uiState.value =
        _uiState.value.copy(
            selectedCategoryId = null,
            selectedCategoryLabel = null,
            selectedCategoryColor = null,
            selectedCategoryIndex = -1,
        )
  }

  private fun loadSelectedCategory(categoryId: String) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
      try {
        val orgId = requireOrgId()

        val selectedCategory =
            categoryRepository.getCategoryById(orgId = orgId, itemId = categoryId)
        if (selectedCategory != null) {
          _uiState.value =
              _uiState.value.copy(
                  selectedCategoryId = selectedCategory.id,
                  selectedCategoryLabel = selectedCategory.label,
                  selectedCategoryColor = selectedCategory.color,
                  selectedCategoryIndex = selectedCategory.index,
                  isLoading = false,
              )
        }
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(
                isLoading = false, errorMessage = "Failed to load category: ${e.message}")
      }
    }
  }

  /**
   * Prepares deletion of a category.
   *
   * Loads the selected category into the draft state and opens the confirmation dialog.
   */
  fun askDeleteCategory(categoryId: String) {
    loadSelectedCategory(categoryId)
    _uiState.value = _uiState.value.copy(showDeleteDialog = true)
  }

  fun dismissDeleteDialog() {
    _uiState.value = _uiState.value.copy(showDeleteDialog = false)
    resetSelectedCategory()
  }

  /**
   * Deletes the currently selected category after validation.
   *
   * On success:
   * - the category is removed from persistence
   * - the UI state is refreshed
   *
   * On failure:
   * - an error message is exposed to the UI
   */
  fun confirmDeleteSelectedCategory() {
    val errorMessage = validateSelectedCategory()
    if (errorMessage != null) {
      setErrorMsg(errorMessage)
      return
    }

    viewModelScope.launch {
      try {
        _uiState.value =
            _uiState.value.copy(showDeleteDialog = false, isLoading = true, errorMessage = null)
        val orgId = requireOrgId()

        categoryRepository.deleteCategory(
            orgId = orgId, itemId = _uiState.value.selectedCategoryId!!)
        _uiState.value = _uiState.value.copy(isLoading = false)
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(
                showDeleteDialog = false,
                isLoading = false,
                errorMessage = "Failed to delete category: ${e.message}")
      } finally {
        resetSelectedCategory()
        refreshUIState()
      }
    }
  }

  /**
   * Reorders a category in response to a drag-and-drop action.
   *
   * The reordering is applied immediately to the UI for responsiveness, then persisted
   * asynchronously by updating all affected indexes.
   *
   * @param fromIndex original position of the dragged item
   * @param toIndex target position of the dragged item
   */
  fun moveCategory(fromIndex: ItemPosition, toIndex: ItemPosition) {
    if (fromIndex.index == toIndex.index) return

    val current = _uiState.value.categories.toMutableList()

    // Reorder locally (UI instant).
    val moved = current.removeAt(fromIndex.index)
    current.add(toIndex.index, moved)

    // Re-assign indexes locally so UI order and persisted order match.
    val reindexed =
        current.mapIndexed { index, category ->
          if (category.index == index) category else category.copy(index = index)
        }

    _uiState.value = _uiState.value.copy(categories = reindexed)

    // Persist new indexes (later: batch/transaction + dedicated updateCategoryIndex).
    viewModelScope.launch {
      try {
        val orgId = requireOrgId()
        reindexed.forEach { category ->
          categoryRepository.updateCategory(orgId = orgId, item = category, itemId = category.id)
        }
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(errorMessage = "Failed to reorder categories: ${e.message}")
      }
    }
  }

  /**
   * Opens the bottom sheet in "create category" mode.
   *
   * Any previously selected category is cleared.
   */
  fun openCreateCategoryBottomSheet() {
    resetSelectedCategory()
    _uiState.value = _uiState.value.copy(showBottomSheet = true, errorMessage = null)
  }

  /**
   * Opens the bottom sheet in "edit category" mode.
   *
   * Loads the selected category and populates the draft fields.
   */
  fun openEditCategoryBottomSheet(categoryId: String) {
    loadSelectedCategory(categoryId)
    _uiState.value = _uiState.value.copy(showBottomSheet = true, errorMessage = null)
  }

  /** Closes the bottom sheet and clears any draft state. */
  fun dismissBottomSheet() {
    _uiState.value = _uiState.value.copy(showBottomSheet = false)
    resetSelectedCategory()
  }

  /**
   * Persists the current category draft.
   *
   * Behavior:
   * - If no category is selected, a new category is created and appended at the end of the ordered
   *   list.
   * - If a category is selected, it is updated while preserving its index.
   *
   * Validation is applied before persistence. On success, the bottom sheet is closed and the draft
   * state is reset.
   */
  fun saveCategory() {
    val state = _uiState.value

    // Validate draft fields (works for both create and update).
    val draftError = validateCategoryDraft(state.selectedCategoryLabel, state.selectedCategoryColor)
    if (draftError != null) {
      setErrorMsg(draftError)
      return
    }

    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

      try {
        val orgId = requireOrgId()

        val isCreate = state.selectedCategoryId == null

        // For a new category, default to appending at the end.
        // For updates, keep the existing index.
        val targetIndex =
            if (isCreate) {
              state.categories.size
            } else {
              state.selectedCategoryIndex
            }

        if (isCreate) {
          val newCategory =
              EventCategory(
                  organizationId = orgId,
                  index = targetIndex,
                  label = state.selectedCategoryLabel!!.trim(),
                  color = state.selectedCategoryColor!!,
              )

          categoryRepository.insertCategory(orgId = orgId, item = newCategory)

          val updated = (state.categories + newCategory).sortedBy { it.index }

          _uiState.value =
              _uiState.value.copy(
                  categories = updated,
                  isLoading = false,
                  showBottomSheet = false,
              )
        } else {
          val updatedCategory =
              EventCategory(
                  id = state.selectedCategoryId!!,
                  organizationId = orgId,
                  index = targetIndex,
                  label = state.selectedCategoryLabel!!.trim(),
                  color = state.selectedCategoryColor!!,
              )

          categoryRepository.updateCategory(
              orgId = orgId, item = updatedCategory, itemId = state.selectedCategoryId)

          val updated =
              state.categories
                  .map { existing ->
                    if (existing.id == updatedCategory.id) updatedCategory else existing
                  }
                  .sortedBy { it.index }

          _uiState.value =
              _uiState.value.copy(categories = updated, isLoading = false, showBottomSheet = false)
        }

        resetSelectedCategory()
      } catch (e: Exception) {
        _uiState.value =
            _uiState.value.copy(
                isLoading = false, errorMessage = "Failed to save category: ${e.message}")
      }
    }
  }

  /**
   * Updates the draft label of the currently edited category.
   *
   * This does not persist anything; it only updates UI state.
   */
  fun setSelectedCategoryLabel(label: String) {
    _uiState.value = _uiState.value.copy(selectedCategoryLabel = label)
  }

  /**
   * Updates the draft color of the currently edited category.
   *
   * This does not persist anything; it only updates UI state.
   */
  fun setSelectedCategoryColor(color: Color) {
    _uiState.value = _uiState.value.copy(selectedCategoryColor = color)
  }

  /**
   * Assisted by AI
   *
   * Normalizes category indexes to a dense range [0..n-1] while keeping a deterministic order.
   *
   * We sort by (index, id) to break ties deterministically, then re-assign indexes sequentially.
   * This prevents non-deterministic ordering when multiple items share the same index.
   */
  private fun reindexCategories(categories: List<EventCategory>): List<EventCategory> {
    val sorted = categories.sortedWith(compareBy<EventCategory> { it.index }.thenBy { it.id })
    return sorted.mapIndexed { newIndex, category ->
      if (category.index == newIndex) category else category.copy(index = newIndex)
    }
  }

  /**
   * Assisted by AI
   *
   * Returns true if indexes are exactly [0..n-1] (dense, unique, no gaps).
   */
  private fun hasDenseSequentialIndexes(categories: List<EventCategory>): Boolean {
    val indexes = categories.map { it.index }
    if (indexes.size != indexes.toSet().size) return false
    val sorted = indexes.sorted()
    return sorted.firstOrNull() == 0 &&
        sorted.lastOrNull() == categories.lastIndex &&
        sorted.zipWithNext().all { (a, b) -> b == a + 1 }
  }
}
