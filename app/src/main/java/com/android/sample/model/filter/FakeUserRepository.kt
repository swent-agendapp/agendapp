package com.android.sample.model.filter

import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository

// Assisted by AI

/**
 * Fake implementation of [UserRepository] used for unit tests.
 *
 * This repository provides a minimal, in-memory implementation that supports **read-only access**
 * to organization members.
 *
 * It is designed specifically for tests that need user display data (e.g. FilterViewModel tests)
 * without depending on Firebase or backend logic.
 *
 * All write operations are implemented as no-ops because they are not required for the tested
 * scenarios.
 */
class FakeUserRepository : UserRepository {

  /**
   * Returns a fixed list of member IDs for the organization.
   *
   * @param organizationId Organization identifier (ignored in this fake)
   */
  override suspend fun getMembersIds(organizationId: String): List<String> = listOf("u1", "u2")

  /**
   * Returns fake users corresponding to the provided IDs.
   *
   * @param userIds List of user IDs
   */
  override suspend fun getUsersByIds(userIds: List<String>): List<User> =
      listOf(User(id = "u1", displayName = "Alice"), User(id = "u2", displayName = "Bob"))

  /**
   * Returns an empty list of admin IDs.
   *
   * Admin functionality is not required by the current test scenarios.
   */
  override suspend fun getAdminsIds(organizationId: String): List<String> = emptyList()

  /**
   * No-op.
   *
   * User creation is intentionally not supported in this fake repository, as it is not required for
   * unit tests.
   */
  override suspend fun newUser(user: User) {
    // no-op for fake
  }

  /**
   * No-op.
   *
   * User deletion is intentionally not supported in this fake repository, as it is not required for
   * unit tests.
   */
  override suspend fun deleteUser(userId: String) {
    // no-op for fake
  }

  /**
   * No-op.
   *
   * Organization membership mutations are intentionally ignored in this fake repository.
   */
  override suspend fun addUserToOrganization(userId: String, organizationId: String) {
    // no-op for fake
  }

  /**
   * No-op.
   *
   * Admin role assignment is intentionally ignored in this fake repository.
   */
  override suspend fun addAdminToOrganization(userId: String, organizationId: String) {
    // no-op for fake
  }
}
