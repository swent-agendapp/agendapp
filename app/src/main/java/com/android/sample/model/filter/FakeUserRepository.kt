package com.android.sample.model.filter

import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository

class FakeUserRepository : UserRepository {

  override suspend fun getMembersIds(organizationId: String): List<String> = listOf("u1", "u2")

  override suspend fun getUsersByIds(userIds: List<String>): List<User> =
      listOf(User(id = "u1", displayName = "Alice"), User(id = "u2", displayName = "Bob"))

  override suspend fun getAdminsIds(organizationId: String): List<String> = emptyList()

  override suspend fun newUser(user: User) {}

  override suspend fun deleteUser(userId: String) {}

  override suspend fun addUserToOrganization(userId: String, organizationId: String) {}

  override suspend fun addAdminToOrganization(userId: String, organizationId: String) {}
}
