package com.android.sample.model.authentication

class UsersRepositoryLocal() : UserRepository {

  private val admins: MutableMap<String, MutableList<String>> = mutableMapOf()
  private val employees: MutableMap<String, MutableList<String>> = mutableMapOf()
  private val users: MutableMap<String, User> = mutableMapOf()
  /** -------------------------- USERS IDS -------------------------- */
  override suspend fun getAdminsIds(organizationId: String): List<String> {
    return admins[organizationId] ?: emptyList()
  }

  override suspend fun getMembersIds(organizationId: String): List<String> {
    return employees[organizationId] ?: emptyList()
  }

  /** -------------------------- USERS DETAILS -------------------------- */
  override suspend fun getUsersByIds(userIds: List<String>): List<User> {
    return userIds.mapNotNull { it -> users[it] }
  }

  /** -------------------------- UPSERT / MODIFY -------------------------- */
  override suspend fun newUser(user: User) {
    require(user.id.isNotBlank()) { "userId is required" }
    users[user.id] = user
  }

  override suspend fun modifyUser(user: User) {
    require(user.id.isNotBlank()) { "userId is required" }
    users[user.id] = user
  }

  /** -------------------------- DELETE USER -------------------------- */
  override suspend fun deleteUser(userId: String) {
    val toDelete = users[userId]
    toDelete?.organizations?.forEach { it -> admins[it]?.remove(userId) }
    toDelete?.organizations?.forEach { it -> employees[it]?.remove(userId) }
    users.remove(userId)
  }

  override suspend fun addUserToOrganization(userId: String, organizationId: String) {
    employees.getOrPut(userId) { mutableListOf(userId) }

    val user = users[userId] ?: return
    val updatedOrg = user.organizations + "newOrg"
    users[userId] = user.copy(organizations = updatedOrg)
  }

  override suspend fun addAdminToOrganization(userId: String, organizationId: String) {
    admins.getOrPut(userId) { mutableListOf(userId) }
  }
}
