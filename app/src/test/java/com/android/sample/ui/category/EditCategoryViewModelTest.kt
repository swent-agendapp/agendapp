package com.android.sample.ui.category

import androidx.compose.ui.graphics.Color
import com.android.sample.model.category.EventCategory
import com.android.sample.model.category.EventCategoryRepository
import com.android.sample.model.category.EventCategoryRepositoryLocal
import com.android.sample.model.organization.repository.SelectedOrganizationRepository
import com.android.sample.ui.organization.SelectedOrganizationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.burnoutcrew.reorderable.ItemPosition
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditCategoryViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  private lateinit var categoryRepository: EventCategoryRepository
  private lateinit var selectedOrganizationViewModel: SelectedOrganizationViewModel
  private lateinit var vm: EditCategoryViewModel

  private val selectedOrganizationID: String = "org123"

  @Before
  fun setUp() = runBlocking {
    Dispatchers.setMain(testDispatcher)

    categoryRepository = EventCategoryRepositoryLocal()

    // Keep tests close to app behavior: ViewModel reads from SelectedOrganizationRepository.
    SelectedOrganizationRepository.changeSelectedOrganization(selectedOrganizationID)
    selectedOrganizationViewModel = SelectedOrganizationViewModel()
    vm =
        EditCategoryViewModel(
            categoryRepository = categoryRepository,
            selectedOrganizationViewModel = selectedOrganizationViewModel,
        )
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initial UI state has default values`() {
    val state = vm.uiState.value

    assertTrue(state.categories.isEmpty())
    assertNull(state.selectedCategoryId)
    assertNull(state.selectedCategoryLabel)
    assertNull(state.selectedCategoryColor)
    assertEquals(-1, state.selectedCategoryIndex)
    assertFalse(state.showDeleteDialog)
    assertFalse(state.showBottomSheet)
    assertFalse(state.isLoading)
    assertNull(state.errorMessage)
  }

  @Test
  fun `setSelectedCategoryLabel updates the draft label`() {
    vm.setSelectedCategoryLabel("My Category")

    assertEquals("My Category", vm.uiState.value.selectedCategoryLabel)
  }

  @Test
  fun `setSelectedCategoryColor updates the draft color`() {
    vm.setSelectedCategoryColor(Color.Red)

    assertEquals(Color.Red, vm.uiState.value.selectedCategoryColor)
  }

  @Test
  fun `refreshUIState loads categories sorted by index and clears selection`() = runTest {
    val c0 = makeCategory(id = "c0", label = "A", color = Color.Red, index = 1)
    val c1 = makeCategory(id = "c1", label = "B", color = Color.Blue, index = 0)

    insertCategories(c0, c1)

    vm.setSelectedCategoryLabel("should be cleared")
    vm.setSelectedCategoryColor(Color.Green)
    vm.openCreateCategoryBottomSheet()
    assertTrue(vm.uiState.value.showBottomSheet)

    refresh()

    val state = vm.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.errorMessage)

    assertEquals(listOf("c1", "c0"), state.categories.map { it.id }) // sorted by index
    assertNull(state.selectedCategoryId)
    assertNull(state.selectedCategoryLabel)
    assertNull(state.selectedCategoryColor)
    assertEquals(-1, state.selectedCategoryIndex)
    assertFalse(state.showDeleteDialog)
    assertFalse(state.showBottomSheet)
  }

  @Test
  fun `openCreateCategoryBottomSheet resets selection and opens bottom sheet`() {
    vm.setSelectedCategoryLabel("X")
    vm.setSelectedCategoryColor(Color.Red)

    vm.openCreateCategoryBottomSheet()

    val state = vm.uiState.value
    assertTrue(state.showBottomSheet)
    assertNull(state.selectedCategoryId)
    assertNull(state.selectedCategoryLabel)
    assertNull(state.selectedCategoryColor)
    assertEquals(-1, state.selectedCategoryIndex)
    assertNull(state.errorMessage)
  }

  @Test
  fun `openEditCategoryBottomSheet loads selected category and opens bottom sheet`() = runTest {
    val c0 = makeCategory(id = "c0", label = "A", color = Color.Red, index = 0)
    insertCategories(c0)

    runAndIdle { vm.openEditCategoryBottomSheet("c0") }

    val state = vm.uiState.value
    assertTrue(state.showBottomSheet)
    assertFalse(state.isLoading)
    assertEquals("c0", state.selectedCategoryId)
    assertEquals("A", state.selectedCategoryLabel)
    assertEquals(Color.Red, state.selectedCategoryColor)
    assertEquals(0, state.selectedCategoryIndex)
  }

  @Test
  fun `dismissBottomSheet closes bottom sheet and resets selection`() {
    vm.openCreateCategoryBottomSheet()
    assertTrue(vm.uiState.value.showBottomSheet)

    vm.setSelectedCategoryLabel("X")
    vm.setSelectedCategoryColor(Color.Red)

    vm.dismissBottomSheet()

    val state = vm.uiState.value
    assertFalse(state.showBottomSheet)
    assertNull(state.selectedCategoryId)
    assertNull(state.selectedCategoryLabel)
    assertNull(state.selectedCategoryColor)
    assertEquals(-1, state.selectedCategoryIndex)
  }

  @Test
  fun `saveCategory creates new category at end and closes bottom sheet`() = runTest {
    val c0 = makeCategory(id = "c0", label = "A", color = Color.Red, index = 0)
    insertCategories(c0)

    refresh()
    assertEquals(1, vm.uiState.value.categories.size)

    vm.openCreateCategoryBottomSheet()
    vm.setSelectedCategoryLabel("New")
    vm.setSelectedCategoryColor(Color.Blue)

    runAndIdle { vm.saveCategory() }

    val state = vm.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.errorMessage)
    assertFalse(state.showBottomSheet)

    assertEquals(2, state.categories.size)
    assertEquals(listOf("A", "New"), state.categories.sortedBy { it.index }.map { it.label })
    assertEquals(listOf(0, 1), state.categories.sortedBy { it.index }.map { it.index })
  }

  @Test
  fun `saveCategory updates existing category and closes bottom sheet`() = runTest {
    val c0 = makeCategory(id = "c0", label = "Old", color = Color.Red, index = 0)
    insertCategories(c0)

    refresh()

    runAndIdle { vm.openEditCategoryBottomSheet("c0") }

    vm.setSelectedCategoryLabel("Updated")
    vm.setSelectedCategoryColor(Color.Green)

    runAndIdle { vm.saveCategory() }

    val state = vm.uiState.value
    assertFalse(state.isLoading)
    assertNull(state.errorMessage)
    assertFalse(state.showBottomSheet)

    val updated = state.categories.single { it.id == "c0" }
    assertEquals("Updated", updated.label)
    assertEquals(Color.Green, updated.color)
    assertEquals(0, updated.index)
  }

  @Test
  fun `saveCategory sets error when label is blank and does not save`() = runTest {
    vm.openCreateCategoryBottomSheet()
    vm.setSelectedCategoryLabel("   ")
    vm.setSelectedCategoryColor(Color.Red)

    runAndIdle { vm.saveCategory() }

    val state = vm.uiState.value
    assertEquals("The selected category has no name", state.errorMessage)
    assertTrue(state.categories.isEmpty())
    assertTrue(state.showBottomSheet) // still open since validation failed
  }

  @Test
  fun `saveCategory sets error when color is null and does not save`() = runTest {
    vm.openCreateCategoryBottomSheet()
    vm.setSelectedCategoryLabel("Name")
    // Do not set color.

    runAndIdle { vm.saveCategory() }

    val state = vm.uiState.value
    assertEquals("The selected category has no color", state.errorMessage)
    assertTrue(state.categories.isEmpty())
    assertTrue(state.showBottomSheet)
  }

  @Test
  fun `askDeleteCategory opens dialog and loads selected category`() = runTest {
    val c0 = makeCategory(id = "c0", label = "A", color = Color.Red, index = 0)
    insertCategories(c0)

    runAndIdle { vm.askDeleteCategory("c0") }

    val state = vm.uiState.value
    assertTrue(state.showDeleteDialog)
    assertEquals("c0", state.selectedCategoryId)
    assertEquals("A", state.selectedCategoryLabel)
    assertEquals(Color.Red, state.selectedCategoryColor)
    assertEquals(0, state.selectedCategoryIndex)
  }

  @Test
  fun `dismissDeleteDialog closes dialog and resets selection`() = runTest {
    val c0 = makeCategory(id = "c0", label = "A", color = Color.Red, index = 0)
    insertCategories(c0)

    runAndIdle { vm.askDeleteCategory("c0") }
    assertTrue(vm.uiState.value.showDeleteDialog)

    vm.dismissDeleteDialog()

    val state = vm.uiState.value
    assertFalse(state.showDeleteDialog)
    assertNull(state.selectedCategoryId)
    assertNull(state.selectedCategoryLabel)
    assertNull(state.selectedCategoryColor)
    assertEquals(-1, state.selectedCategoryIndex)
  }

  @Test
  fun `confirmDeleteSelectedCategory deletes category, closes dialog and refreshes list`() =
      runTest {
        val c0 = makeCategory(id = "c0", label = "A", color = Color.Red, index = 0)
        val c1 = makeCategory(id = "c1", label = "B", color = Color.Blue, index = 1)
        insertCategories(c0, c1)

        refresh()
        assertEquals(2, vm.uiState.value.categories.size)

        runAndIdle { vm.askDeleteCategory("c0") }

        runAndIdle { vm.confirmDeleteSelectedCategory() }

        val state = vm.uiState.value
        assertFalse(state.showDeleteDialog)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)

        assertEquals(1, state.categories.size)
        assertEquals("c1", state.categories.single().id)
      }

  @Test
  fun `confirmDeleteSelectedCategory sets error when nothing selected`() = runTest {
    runAndIdle { vm.confirmDeleteSelectedCategory() }

    assertEquals("No category selected", vm.uiState.value.errorMessage)
  }

  @Test
  fun `moveCategory reorders list and reindexes sequentially`() = runTest {
    val c0 = makeCategory(id = "c0", label = "A", color = Color.Red, index = 0)
    val c1 = makeCategory(id = "c1", label = "B", color = Color.Blue, index = 1)
    val c2 = makeCategory(id = "c2", label = "C", color = Color.Green, index = 2)

    insertCategories(c0, c1, c2)

    refresh()

    assertEquals(listOf("c0", "c1", "c2"), vm.uiState.value.categories.map { it.id })

    runAndIdle { vm.moveCategory(fromIndex = ItemPosition(0, 0), toIndex = ItemPosition(2, 2)) }

    val ids = vm.uiState.value.categories.map { it.id }
    assertEquals(listOf("c1", "c2", "c0"), ids)

    val indexes = vm.uiState.value.categories.map { it.index }
    assertEquals(listOf(0, 1, 2), indexes)

    // Also persisted (best-effort): refresh and see stable order by index.
    refresh()
    assertEquals(listOf("c1", "c2", "c0"), vm.uiState.value.categories.map { it.id })
  }

  @Test
  fun `refreshUIState repairs duplicate or gapped indexes to be dense`() = runTest {
    // Duplicate index + gap: should trigger reindexCategories + persistence in refreshUIState.
    val c0 = makeCategory(id = "c0", label = "A", color = Color.Red, index = 0)
    val c1 = makeCategory(id = "c1", label = "B", color = Color.Blue, index = 0)
    val c2 = makeCategory(id = "c2", label = "C", color = Color.Green, index = 5)

    insertCategories(c0, c1, c2)

    refresh()

    val state = vm.uiState.value
    assertEquals(3, state.categories.size)
    assertEquals(listOf(0, 1, 2), state.categories.map { it.index })

    // Deterministic order (by previous (index,id) then reassigned):
    assertEquals(listOf("c0", "c1", "c2"), state.categories.map { it.id })
  }

  /* Helper functions */

  private suspend fun insertCategories(vararg categories: EventCategory) {
    categories.forEach { categoryRepository.insertCategory(selectedOrganizationID, it) }
  }

  private fun refresh() {
    vm.refreshUIState()
    testDispatcher.scheduler.advanceUntilIdle()
  }

  private suspend fun runAndIdle(block: suspend () -> Unit) {
    block()
    testDispatcher.scheduler.advanceUntilIdle()
  }

  private fun makeCategory(id: String, label: String, color: Color, index: Int): EventCategory {
    return EventCategory(
        id = id,
        organizationId = selectedOrganizationID,
        index = index,
        label = label,
        color = color,
    )
  }
}
