package com.android.sample.model.authorization

import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Simple in-memory fake implementation of [UserRepository] used for unit testing.
 *
 * This fake:
 * - Stores users inside a mutable list
 * - Overrides CRUD methods without side effects
 * - Allows quick assertions without hitting any database or backend
 */
private class FakeUserRepository(var users: MutableList<User> = mutableListOf()) : UserRepository {

  /** Returns all stored users. */
  override suspend fun getUsers(organizationId: String): List<User> = users

  override suspend fun getAdmins(organizationId: String): List<User> {
    TODO("Not yet implemented")
  }

  override suspend fun modifyUser(user: User) {
    TODO("Not yet implemented")
  }

  /** Inserts or replaces a user with the same ID. */
  override suspend fun newUser(user: User) {
    users.removeAll { it.id == user.id }
    users.add(user)
  }

  /** Deletes a user by ID. */
  override suspend fun deleteUser(userId: String) {
    users.removeAll { it.id == userId }
  }
}

/**
 * Unit tests for [FakeUserRepository].
 *
 * These tests ensure that the fake repository behaves consistently and can be used in other
 * ViewModel or domain tests without unexpected behavior.
 */
class FakeUserRepositoryTest {

  private lateinit var repo: FakeUserRepository

  @Before
  fun setUp() {
    repo = FakeUserRepository()
  }

  @Test
  fun getUsers_returns_empty_list_initially() = runTest {
    val result = repo.getUsers("")
    assertThat(result).isEmpty()
  }

  @Test
  fun newUser_adds_user_to_repository() = runTest {
    val user = User(id = "1", email = "a@test.com", displayName = "Alice")
    repo.newUser(user)

    assertThat(repo.getUsers("")).containsExactly(user)
  }

  @Test
  fun newUser_replaces_existing_user_with_same_id() = runTest {
    val user1 = User(id = "1", email = "a@test.com", displayName = "Alice")
    val user2 = User(id = "1", email = "b@test.com", displayName = "Bob")

    repo.newUser(user1)
    repo.newUser(user2)

    assertThat(repo.getUsers("")).containsExactly(user2)
  }

  @Test
  fun deleteUser_removes_user_with_matching_id() = runTest {
    val user = User(id = "1", email = "a@test.com", displayName = "Alice")
    repo.newUser(user)

    repo.deleteUser("1")

    assertThat(repo.getUsers("")).isEmpty()
  }
}
