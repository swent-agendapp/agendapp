package com.android.sample.model.userRepositoryTest

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.organization.data.Organization
import com.android.sample.model.organization.repository.OrganizationRepositoryProvider
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.RequiresSelectedOrganizationTestBase
import com.android.sample.utils.RequiresSelectedOrganizationTestBase.Companion.DEFAULT_TEST_ORG_ID
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserRepositoryFirebaseTest : FirebaseEmulatedTest(), RequiresSelectedOrganizationTestBase {

  override val organizationId: String = DEFAULT_TEST_ORG_ID

  private lateinit var repository: UserRepository

  @Before
  override fun setUp() {
    super.setUp()

    setSelectedOrganization()

    // Use Firebase user repository for tests
    repository = createInitializedUserRepository()
  }

  @Test
  fun newUser_andGetUsers_shouldWork() = runBlocking {
    val user1 = User(id = "user-1", displayName = "Alice", email = "alice@example.com")
    val user2 = User(id = "user-2", displayName = "Bob", email = "bob@example.com")

    repository.newUser(user1)
    repository.newUser(user2)

    repository.addUserToOrganization("user-1", organizationId)
    repository.addUserToOrganization("user-2", organizationId)

    val userIds = repository.getMembersIds(organizationId)

    Assert.assertEquals(2, userIds.size)
    Assert.assertTrue(userIds.contains("user-1"))
    Assert.assertTrue(userIds.contains("user-2"))

    // Clean up
    repository.deleteUser("user-1")
    repository.deleteUser("user-2")
  }

  @Test
  fun getUsersByIds_shouldWork() = runBlocking {
    val user1 = User(id = "u1", displayName = "U1", email = "u1@e.com")
    val user2 = User(id = "u2", displayName = "U2", email = "u2@e.com")
    val user3 = User(id = "u3", displayName = "U3", email = "u3@e.com")

    repository.newUser(user1)
    repository.newUser(user2)
    repository.newUser(user3)

    val results = repository.getUsersByIds(listOf("u1", "u3"))

    Assert.assertEquals(2, results.size)
    Assert.assertTrue(results.any { it.id == "u1" })
    Assert.assertTrue(results.any { it.id == "u3" })
    Assert.assertFalse(results.any { it.id == "u2" })

    // Clean up
    repository.deleteUser("u1")
    repository.deleteUser("u2")
    repository.deleteUser("u3")
  }

  @Test
  fun deleteUser_shouldRemoveUser_everywhere() = runBlocking {
    val userAdmin = User(id = "user-admin")
    OrganizationRepositoryProvider.repository.insertOrganization(
        Organization(id = organizationId, name = "Test Org"))
    repository.newUser(userAdmin)
    repository.addAdminToOrganization(userAdmin.id, organizationId)

    val user =
        User(
            id = "to-delete",
            displayName = "Charlie",
            email = "charlie@example.com",
            organizations = listOf(organizationId))

    // Create user document with organizations included
    repository.newUser(user)

    // Ensure org membership is written inside Firestore structure
    repository.addUserToOrganization("to-delete", organizationId)

    // Must exist before deletion
    var userIds = repository.getMembersIds(organizationId)
    Assert.assertTrue(userIds.contains("to-delete"))

    // Delete the user fully
    repository.deleteUser("to-delete")

    // Must disappear from organization
    userIds = repository.getMembersIds(organizationId)
    Assert.assertFalse(userIds.contains("to-delete"))

    // Clean up
    repository.deleteUser("user-admin")
  }

  @Test
  fun newUser_withBlankUserId_shouldThrow() = runBlocking {
    val invalidUser = User(id = "", displayName = "NoId", email = "noid@example.com")

    try {
      repository.newUser(invalidUser)
      Assert.fail("Expected IllegalArgumentException for blank userId")
    } catch (_: IllegalArgumentException) {
      // expected
    }
  }

  @Test
  fun getAdminsIds_shouldWork() = runBlocking {
    // Prepare organization
    val userAdmin = User(id = "user-admin")
    OrganizationRepositoryProvider.repository.insertOrganization(
        Organization(id = organizationId, name = "Test Org"))

    val user1 = User(id = "admin-1", displayName = "Admin1", email = "a1@e.com")
    val user2 = User(id = "admin-2", displayName = "Admin2", email = "a2@e.com")

    repository.newUser(user1)
    repository.newUser(user2)

    // Add admins
    repository.addAdminToOrganization("admin-1", organizationId)
    repository.addAdminToOrganization("admin-2", organizationId)

    val adminIds = repository.getAdminsIds(organizationId)

    Assert.assertEquals(2, adminIds.size)
    Assert.assertTrue(adminIds.contains("admin-1"))
    Assert.assertTrue(adminIds.contains("admin-2"))

    // Clean up
    repository.deleteUser("admin-1")
    repository.deleteUser("admin-2")
  }

  @Test
  fun addAdminToOrganization_shouldWork() = runBlocking {
    // Prepare organization
    val userAdmin = User(id = "user-admin")
    OrganizationRepositoryProvider.repository.insertOrganization(
        Organization(id = organizationId, name = "Test Org"))

    val user = User(id = "new-admin", displayName = "NA", email = "na@e.com")
    repository.newUser(user)

    repository.addAdminToOrganization("new-admin", organizationId)

    val adminIds = repository.getAdminsIds(organizationId)
    Assert.assertEquals(1, adminIds.size)
    Assert.assertTrue(adminIds.contains("new-admin"))

    // Ensure user contains organization in org array
    val updatedUser = repository.getUsersByIds(listOf("new-admin")).first()
    Assert.assertTrue(updatedUser.organizations.contains(organizationId))

    // Clean up
    repository.deleteUser("new-admin")
  }

  @Test
  fun deleteUser_shouldRemoveAdminEverywhere() = runBlocking {
    // Prepare organization
    val userAdmin = User(id = "user-admin", organizations = listOf(organizationId))
    OrganizationRepositoryProvider.repository.insertOrganization(
        Organization(id = organizationId, name = "Test Org"))

    val admin =
        User(
            id = "admin-del",
            displayName = "AdminDel",
            email = "ad@e.com",
            organizations = listOf(organizationId))

    repository.newUser(admin)
    repository.addAdminToOrganization("admin-del", organizationId)

    // Ensure exists before deletion
    Assert.assertTrue(repository.getAdminsIds(organizationId).contains("admin-del"))

    repository.deleteUser("admin-del")

    // Should no longer be admin
    val adminIds = repository.getAdminsIds(organizationId)
    Assert.assertFalse(adminIds.contains("admin-del"))

    // Clean up
    repository.deleteUser("user-admin")
  }

  @Test
  fun getUserById_correctly_retrieves() = runBlocking {
    val user =
        User(
            id = "user-get",
            displayName = "Get User",
            email = "get@user.com",
            organizations = listOf(organizationId))

    // Insert user
    repository.newUser(user)

    // Retrieve user
    val retrieved = repository.getUserById("user-get")

    Assert.assertNotNull(retrieved)
    Assert.assertEquals("user-get", retrieved!!.id)
    Assert.assertEquals("Get User", retrieved.displayName)
    Assert.assertEquals("get@user.com", retrieved.email)
    Assert.assertTrue(retrieved.organizations.contains(organizationId))

    // Non-existing user should return null
    val missing = repository.getUserById("does-not-exist")
    Assert.assertNull(missing)

    // Clean up
    repository.deleteUser("user-get")
  }
}
