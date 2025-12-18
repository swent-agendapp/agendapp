package com.android.sample.model.eventCategoryRepositoryTest

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.authentication.UserRepositoryProvider
import com.android.sample.model.authentication.UsersRepositoryLocal
import com.android.sample.model.category.EventCategory
import com.android.sample.model.category.EventCategoryRepository
import com.android.sample.ui.theme.EventPalette
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventCategoryFirebaseRepositoryTest :
    FirebaseEmulatedTest(), RequiresSelectedOrganizationTestBase {

  private lateinit var repository: EventCategoryRepository
  private lateinit var category1: EventCategory
  private lateinit var category2: EventCategory

  override val organizationId: String = DEFAULT_TEST_ORG_ID

  @Before
  override fun setUp() {
    super.setUp()
    repository = createInitializedEventCategoryRepository()

    category1 =
        EventCategory(
            organizationId = organizationId, label = "Category 1", color = EventPalette.Blue)
    category2 =
        EventCategory(
            organizationId = organizationId, label = "Category 2", color = EventPalette.LightGreen)

    // Use local user repository for tests
    UserRepositoryProvider.repository = UsersRepositoryLocal()
  }

  @Test
  fun insertCategory_andGetAllCategories_returnsInsertedOnes() = runBlocking {
    repository.insertCategory(organizationId, category1)
    repository.insertCategory(organizationId, category2)

    val allCategories = repository.getAllCategories(organizationId)

    Assert.assertEquals(2, allCategories.size)
    Assert.assertEquals(setOf("Category 1", "Category 2"), allCategories.map { it.label }.toSet())
  }

  @Test
  fun insertCategory_andGetById_returnsInsertedCategory() = runBlocking {
    repository.insertCategory(organizationId, category1)

    val retrieved = repository.getCategoryById(organizationId, category1.id)

    Assert.assertNotNull(retrieved)
    Assert.assertEquals(category1.id, retrieved!!.id)
    Assert.assertEquals(category1.label, retrieved.label)
    Assert.assertEquals(category1.organizationId, retrieved.organizationId)
  }

  @Test
  fun getCategoryById_unknownId_returnsNull() = runBlocking {
    val result = repository.getCategoryById(organizationId, "unknown-id")

    Assert.assertNull(result)
  }

  @Test
  fun updateCategory_withValidCategory_overwritesExistingData() = runBlocking {
    repository.insertCategory(organizationId, category1)

    val updated = category1.copy(label = "Updated Category 1")
    repository.updateCategory(organizationId, category1.id, updated)

    val retrieved = repository.getCategoryById(organizationId, category1.id)

    Assert.assertNotNull(retrieved)
    Assert.assertEquals("Updated Category 1", retrieved!!.label)
  }

  @Test
  fun deleteCategory_withMatchingOrganizationId_removesCategory() = runBlocking {
    repository.insertCategory(organizationId, category1)

    repository.deleteCategory(organizationId, category1.id)

    val result = repository.getCategoryById(organizationId, category1.id)
    Assert.assertNull(result)
  }
}
