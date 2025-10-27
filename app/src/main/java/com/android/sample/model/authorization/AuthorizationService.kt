package com.android.sample.model.authorization

import com.android.sample.model.organization.EmployeeRepositoryProvider
import com.android.sample.model.organization.Role

/** Simple authorization helper */
class AuthorizationService {

  /** @return The current user's Role, or null */
  suspend fun getMyRole(): Role? = EmployeeRepositoryProvider.repository.getMyRole()

  /** @return true if the current user is ADMIN */
  suspend fun canEditCourses(): Boolean = getMyRole() == Role.ADMIN

  /** Throws if the current user is not ADMIN */
  suspend fun requireAdmin() {
    if (!canEditCourses()) {
      throw IllegalAccessException("You do not have nessessary permission")
    }
  }
}
