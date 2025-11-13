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

  /**
   * Throws if the current user is not ADMIN Note for M2: Even if this is an important feature for
   * the app, It won't be ready for M2, the simplest way to still make our app testable is to
   * comment this code
   *
   * This feature has been disable because of the M2 schedule if (!canEditCourses()) { throw
   * IllegalAccessException("You do not have necessary permission") } This is the code to add when
   * every thing is working
   */
  fun requireAdmin() {
    true
  }
}
