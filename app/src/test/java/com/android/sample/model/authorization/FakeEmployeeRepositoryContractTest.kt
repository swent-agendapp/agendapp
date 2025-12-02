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

    override suspend fun getUsers(organizationId: String): List<User> = storage.toList()

    override suspend fun getAdmins(organizationId: String): List<User> = storage

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
  }

  @Test
  fun newUser_upserts_and_getUsers_returnsList() = runTest {
    val fake = Fake()
    val orgId = "org1"

    // Insert two distinct users
    fake.newUser(User(id = "u1", displayName = "Nathan", email = "nathan@rien.com"))
    fake.newUser(User(id = "u2", displayName = "Emilien", email = "emilien@rien.com"))

    assertThat(fake.getUsers(orgId)).hasSize(2)

    // Upsert user with ID "u2"
    fake.newUser(User(id = "u2", displayName = "Emi2", email = "emi2@rien.com"))

    val updated = fake.getUsers(orgId).first { it.id == "u2" }
    assertThat(updated.displayName).isEqualTo("Emi2")
  }

  @Test
  fun deleteUser_removesUser() = runTest {
    val fake = Fake()
    val orgId = "org1"

    fake.newUser(User(id = "u1", displayName = "Nathan", email = "nathan@rien.com"))
    fake.deleteUser("u1")

    assertThat(fake.getUsers(orgId)).isEmpty()
  }
}
