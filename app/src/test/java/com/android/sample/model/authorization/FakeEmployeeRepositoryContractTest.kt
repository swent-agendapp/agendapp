package com.android.sample.model.authorization

import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Contract tests for a simple in-memory fake implementation of [UserRepository].
 *
 * Validates:
 * - newUser() upsert logic
 * - getUsers()
 * - deleteUser()
 */
class FakeEmployeeRepositoryContractTest {

  /** Simple in-memory fake for testing. */
  private class Fake : UserRepository {
    private val storage = mutableListOf<User>()

    override suspend fun getUsersIds(organizationId: String): List<String> = storage.map { it.id }

    override suspend fun getAdminsIds(organizationId: String): List<String> = storage.map { it.id }

    override suspend fun getUsersByIds(userIds: List<String>): List<User> {
      return storage.filter { userIds.contains(it.id) }
    }

    override suspend fun modifyUser(user: User) {
      storage.removeAll { it.id == user.id }
      storage.add(user)
    }

    override suspend fun newUser(user: User) {
      storage.removeAll { it.id == user.id }
      storage.add(user)
    }

    override suspend fun deleteUser(userId: String) {
      storage.removeAll { it.id == userId }
    }

    override suspend fun addUserToOrganization(userId: String, organizationId: String) {
      // No-op for fake
    }

    override suspend fun addAdminToOrganization(userId: String, organizationId: String) {
      // No-op for fake
    }
  }

  @Test
  fun newUser_upserts_and_getUsers_returnsList() = runTest {
    val fake = Fake()
    val orgId = "org1"

    // Insert two distinct users
    fake.newUser(User(id = "u1", displayName = "Nathan", email = "nathan@rien.com"))
    fake.newUser(User(id = "u2", displayName = "Emilien", email = "emilien@rien.com"))

    assertThat(fake.getUsersIds(orgId)).hasSize(2)

    // Upsert user with ID "u2"
    fake.newUser(User(id = "u2", displayName = "Emi2", email = "emi2@rien.com"))

    val updated = fake.getUsersByIds(listOf("u2")).first()
    assertThat(updated.displayName).isEqualTo("Emi2")
  }

  @Test
  fun deleteUser_removesUser() = runTest {
    val fake = Fake()
    val orgId = "org1"

    fake.newUser(User(id = "u1", displayName = "Nathan", email = "nathan@rien.com"))
    fake.deleteUser("u1")

    assertThat(fake.getUsersIds(orgId)).isEmpty()
  }
}
