package com.android.sample.model.eventCategoryRepositoryTest

import com.android.sample.model.category.EventCategory
import com.android.sample.model.category.EventCategoryRepositoryLocal
import com.android.sample.ui.theme.EventPalette
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/** Unit tests for [EventCategoryRepositoryLocal]. */
class EventCategoryRepositoryLocalTest {

  private lateinit var repository: EventCategoryRepositoryLocal
  private lateinit var category1: EventCategory
  private lateinit var category2: EventCategory

  private val selectedOrganizationId = "orgTest"

  @Before
  fun setUp() {
    repository = EventCategoryRepositoryLocal()

    category1 =
        EventCategory(
            organizationId = selectedOrganizationId,
            label = "Category 1",
            color = EventPalette.Blue)

    category2 =
        EventCategory(
            organizationId = selectedOrganizationId,
            label = "Category 2",
            color = EventPalette.LightGreen)
  }

  @Test
  fun insertCategory_andGetAllCategories_returnsInsertedOnes() = runBlocking {
    repository.insertCategory(selectedOrganizationId, category1)
    repository.insertCategory(selectedOrganizationId, category2)

    val allCategories = repository.getAllCategories(selectedOrganizationId)

    Assert.assertEquals(2, allCategories.size)
    Assert.assertEquals(setOf("Category 1", "Category 2"), allCategories.map { it.label }.toSet())
  }

  @Test
  fun insertCategory_andGetById_returnsInsertedCategory() = runBlocking {
    repository.insertCategory(selectedOrganizationId, category1)

    val retrieved = repository.getCategoryById(selectedOrganizationId, category1.id)

    Assert.assertNotNull(retrieved)
    Assert.assertEquals(category1.id, retrieved!!.id)
    Assert.assertEquals(category1.label, retrieved.label)
    Assert.assertEquals(category1.organizationId, retrieved.organizationId)
  }

  @Test
  fun getCategoryById_unknownId_returnsNull() = runBlocking {
    val result = repository.getCategoryById(selectedOrganizationId, "unknown-id")

    Assert.assertNull(result)
  }

  @Test
  fun updateCategory_withValidCategory_overwritesExistingData() = runBlocking {
    repository.insertCategory(selectedOrganizationId, category1)

    val updated = category1.copy(label = "Updated Category 1")
    repository.updateCategory(selectedOrganizationId, category1.id, updated)

    val retrieved = repository.getCategoryById(selectedOrganizationId, category1.id)

    Assert.assertNotNull(retrieved)
    Assert.assertEquals("Updated Category 1", retrieved!!.label)
  }

  @Test
  fun deleteCategory_withMatchingOrganizationId_removesCategory() = runBlocking {
    repository.insertCategory(selectedOrganizationId, category1)

    repository.deleteCategory(selectedOrganizationId, category1.id)

    val result = repository.getCategoryById(selectedOrganizationId, category1.id)
    Assert.assertNull(result)
  }
}
