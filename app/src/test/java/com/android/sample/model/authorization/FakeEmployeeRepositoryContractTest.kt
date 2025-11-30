package com.android.sample.model.authorization

import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Contract tests for a simple in-memory fake implementation of [UserRepository].
 *
 * This verifies the expected behavior of:
 * - Upsert logic (newUser replaces existing ID)
 * - getUsers()
 * - deleteUser()
 *
 * Since the authorization system (Role, Employee, getMyRole) has been removed, this test only
 * checks the UserRepository contract.
 */
class FakeEmployeeRepositoryContractTest {

  /** Basic fake implementation used for tests. */
  private class Fake : UserRepository {
    val storage = mutableListOf<User>()

    override suspend fun getUsers(): List<User> = storage

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

    // Insert two distinct users
    fake.newUser(User(id = "u1", displayName = "Nathan", email = "nathan@rien.com"))
    fake.newUser(User(id = "u2", displayName = "Emilien", email = "emilien@rien.com"))

    assertThat(fake.getUsers()).hasSize(2)

    // Upsert: replace user with ID "u2"
    fake.newUser(User(id = "u2", displayName = "Emi2", email = "emi2@rien.com"))

    val updated = fake.getUsers().first { it.id == "u2" }
    assertThat(updated.displayName).isEqualTo("Emi2")
  }

  @Test
  fun deleteUser_removesUser() = runTest {
    val fake = Fake()

    fake.newUser(User(id = "u1", displayName = "Nathan", email = "nathan@rien.com"))
    fake.deleteUser("u1")

    assertThat(fake.getUsers()).isEmpty()
  }
}
