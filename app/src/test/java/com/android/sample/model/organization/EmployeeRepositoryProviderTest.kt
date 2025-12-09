package com.android.sample.model.organization

import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UserRepositoryProvider
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Simple stub implementation of [UserRepository] for testing purposes.
 *
 * This stub:
 * - Stores no data
 * - Implements only the required functions of the current UserRepository contract
 */
private class StubUserRepo : UserRepository {
  override suspend fun getMembersIds(organizationId: String): List<String> {
    TODO("No need to implement for current tests")
  }

  override suspend fun getAdminsIds(organizationId: String): List<String> {
    TODO("No need to implement for current tests")
  }

  override suspend fun getUsersByIds(userIds: List<String>): List<User> = emptyList()

  override suspend fun modifyUser(user: User) {
    TODO("No need to implement for current tests")
  }

  override suspend fun newUser(user: User) {
    // No-op for stub
  }

  override suspend fun deleteUser(userId: String) {
    // No-op for stub
  }

  override suspend fun addUserToOrganization(userId: String, organizationId: String) {
    // No-op for stub
  }

  override suspend fun addAdminToOrganization(userId: String, organizationId: String) {
    // No-op for stub
  }
}

/**
 * Basic test ensuring that the repository provider can instantiate and call a UserRepository
 * implementation without throwing or misbehaving.
 */
class UserRepositoryProviderTest {

  @Test
  fun stubRepository_instantiates_and_calls_methods() = runBlocking {
    UserRepositoryProvider.repository = StubUserRepo()
    val repo = UserRepositoryProvider.repository

    // newUser should not throw
    repo.newUser(User("u1", "Alice", "alice@test.com"))

    // deleteUser should not throw
    repo.deleteUser("u1")
  }
}
