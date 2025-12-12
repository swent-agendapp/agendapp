package com.android.sample.model.authentication

class UsersRepositoryLocal : UserRepository {

  /** admins[organizationId] = list of userIds employees[organizationId] = list of userIds */
  private val admins: MutableMap<String, MutableList<String>> = mutableMapOf()
  private val employees: MutableMap<String, MutableList<String>> = mutableMapOf()
  private val users: MutableMap<String, User> = mutableMapOf()

  /** -------------------------- USERS IDS -------------------------- */
  override suspend fun getAdminsIds(organizationId: String): List<String> {
    return admins[organizationId]?.toList() ?: emptyList()
  }

  override suspend fun getMembersIds(organizationId: String): List<String> {
    return employees[organizationId]?.toList() ?: emptyList()
  }

  /** -------------------------- USERS DETAILS -------------------------- */
  override suspend fun getUsersByIds(userIds: List<String>): List<User> {
    return userIds.mapNotNull { users[it] }
  }

  /** -------------------------- UPSERT / MODIFY -------------------------- */
  override suspend fun newUser(user: User) {
    require(user.id.isNotBlank()) { "userId is required" }
    users[user.id] = user
  }

  /** -------------------------- DELETE USER -------------------------- */
  override suspend fun deleteUser(userId: String) {
    val user = users[userId] ?: return

    // Remove user from all organizations
    user.organizations.forEach { orgId ->
      admins[orgId]?.remove(userId)
      employees[orgId]?.remove(userId)
    }

    // Delete user
    users.remove(userId)
  }

  /** -------------------------- ADD MEMBER -------------------------- */
  override suspend fun addUserToOrganization(userId: String, organizationId: String) {
    // 1. Add userId to employees list for this org
    val list = employees.getOrPut(organizationId) { mutableListOf() }
    if (!list.contains(userId)) list.add(userId)

    // 2. Add organization to user.organizations
    val user = users[userId] ?: return
    if (!user.organizations.contains(organizationId)) {
      users[userId] = user.copy(organizations = user.organizations + organizationId)
    }
  }

  /** -------------------------- ADD ADMIN -------------------------- */
  override suspend fun addAdminToOrganization(userId: String, organizationId: String) {
    // 1. Ensure user is member (Firebase does this)
    addUserToOrganization(userId, organizationId)

    // 2. Add admin
    val list = admins.getOrPut(organizationId) { mutableListOf() }
    if (!list.contains(userId)) list.add(userId)
  }
}
