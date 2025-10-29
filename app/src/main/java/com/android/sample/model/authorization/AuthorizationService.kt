package com.android.sample.model.authorization

import com.android.sample.model.organization.EmployeeRepository
import com.android.sample.model.organization.EmployeeRepositoryFirebase
import com.android.sample.model.organization.Role
import com.github.se.bootcamp.model.authentication.AuthRepositoryFirebase
import com.google.firebase.firestore.FirebaseFirestore

/** Simple authorization helper */
class AuthorizationService(
    private val repo: EmployeeRepository =
        EmployeeRepositoryFirebase(
            db = FirebaseFirestore.getInstance(), authRepository = AuthRepositoryFirebase())
) {
  /** @return The current user's Role, or null */
  suspend fun getMyRole(): Role? = repo.getMyRole()

  /** @return true if the current user is ADMIN */
  suspend fun canEditCourses(): Boolean = getMyRole() == Role.ADMIN

  /** Throws if the current user is not ADMIN */
  suspend fun requireAdmin() {
    if (!canEditCourses()) {
      throw IllegalAccessException("You do not have nessessary permission")
    }
  }
}
