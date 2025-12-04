package com.android.sample.model.userRepositoryTest

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.authentication.FakeAuthRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UsersRepositoryFirebase
import com.android.sample.model.constants.FirestoreConstants
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.FirebaseEmulator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserRepositoryFirebaseTest : FirebaseEmulatedTest() {

  private lateinit var repository: UserRepository
  private val organizationId = "org-test"

  @Before
  override fun setUp() {
    super.setUp()
    val authRepository = FakeAuthRepository(user = null)

    repository =
        UsersRepositoryFirebase(
            db = FirebaseEmulator.firestore,
            authRepository = authRepository,
        )
  }

  /** Helper to force Firestore consistency between writes and reads. */
  private suspend fun flushUser(userId: String) {
    FirebaseEmulator.firestore
        .collection(FirestoreConstants.COLLECTION_USERS)
        .document(userId)
        .get()
        .await()
  }

  @Test
  fun newUser_andGetUsers_shouldWork() = runBlocking {
    val user1 = User(id = "user-1", displayName = "Alice", email = "alice@example.com")
    val user2 = User(id = "user-2", displayName = "Bob", email = "bob@example.com")

    repository.newUser(user1)
    repository.newUser(user2)

    repository.addUserToOrganization("user-1", organizationId)
    repository.addUserToOrganization("user-2", organizationId)

    flushUser("user-1")
    flushUser("user-2")

    val userIds = repository.getUsersIds(organizationId)

    Assert.assertEquals(2, userIds.size)
    Assert.assertTrue(userIds.contains("user-1"))
    Assert.assertTrue(userIds.contains("user-2"))
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
  }

  @Test
  fun deleteUser_shouldRemoveUser_everywhere() = runBlocking {
    // Ensure organization exists so deleteUser can find it
    FirebaseEmulator.firestore
        .collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH)
        .document(organizationId)
        .set(mapOf("name" to "Test Org"))
        .await()

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

    flushUser("to-delete")

    // Must exist before deletion
    var userIds = repository.getUsersIds(organizationId)
    Assert.assertTrue(userIds.contains("to-delete"))

    // Delete the user fully
    repository.deleteUser("to-delete")

    flushUser("to-delete")

    // Must disappear from organization
    userIds = repository.getUsersIds(organizationId)
    Assert.assertFalse(userIds.contains("to-delete"))
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
    FirebaseEmulator.firestore
        .collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH)
        .document(organizationId)
        .set(mapOf("name" to "Test Org"))
        .await()

    val user1 = User(id = "admin-1", displayName = "Admin1", email = "a1@e.com")
    val user2 = User(id = "admin-2", displayName = "Admin2", email = "a2@e.com")

    repository.newUser(user1)
    repository.newUser(user2)

    // Add admins
    repository.addAdminToOrganization("admin-1", organizationId)
    repository.addAdminToOrganization("admin-2", organizationId)

    flushUser("admin-1")
    flushUser("admin-2")

    val adminIds = repository.getAdminsIds(organizationId)

    Assert.assertEquals(2, adminIds.size)
    Assert.assertTrue(adminIds.contains("admin-1"))
    Assert.assertTrue(adminIds.contains("admin-2"))
  }

  @Test
  fun modifyUser_shouldUpdateFields() = runBlocking {
    val user = User(id = "mod-1", displayName = "Original", email = "orig@e.com")
    repository.newUser(user)

    // Modify the user
    val updated = user.copy(displayName = "Updated Name")
    repository.modifyUser(updated)

    flushUser("mod-1")

    val result = repository.getUsersByIds(listOf("mod-1")).first()
    Assert.assertEquals("Updated Name", result.displayName)
  }

  @Test
  fun addAdminToOrganization_shouldWork() = runBlocking {
    // Prepare organization
    FirebaseEmulator.firestore
        .collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH)
        .document(organizationId)
        .set(mapOf("name" to "Test Org"))
        .await()

    val user = User(id = "new-admin", displayName = "NA", email = "na@e.com")
    repository.newUser(user)

    repository.addAdminToOrganization("new-admin", organizationId)
    flushUser("new-admin")

    val adminIds = repository.getAdminsIds(organizationId)
    Assert.assertEquals(1, adminIds.size)
    Assert.assertTrue(adminIds.contains("new-admin"))

    // Ensure user contains organization in org array
    val updatedUser = repository.getUsersByIds(listOf("new-admin")).first()
    Assert.assertTrue(updatedUser.organizations.contains(organizationId))
  }

  @Test
  fun deleteUser_shouldRemoveAdminEverywhere() = runBlocking {
    // Prepare organization
    FirebaseEmulator.firestore
        .collection(FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH)
        .document(organizationId)
        .set(mapOf("name" to "Test Org"))
        .await()

    val admin =
        User(
            id = "admin-del",
            displayName = "AdminDel",
            email = "ad@e.com",
            organizations = listOf(organizationId))

    repository.newUser(admin)
    repository.addAdminToOrganization("admin-del", organizationId)
    flushUser("admin-del")

    // Ensure exists before deletion
    Assert.assertTrue(repository.getAdminsIds(organizationId).contains("admin-del"))

    repository.deleteUser("admin-del")
    flushUser("admin-del")

    // Should no longer be admin
    val adminIds = repository.getAdminsIds(organizationId)
    Assert.assertFalse(adminIds.contains("admin-del"))
  }
}
