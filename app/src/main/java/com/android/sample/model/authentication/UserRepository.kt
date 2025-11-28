package com.android.sample.model.authentication

interface EmployeeRepository {

  /** @return The list of employees. */
  suspend fun getUsers(): List<User>

  /**
   * Create or update an employee
   *
   * @param user The employee to create
   */
  suspend fun newUser(user: User)

  /**
   * Delete an employee by ID
   *
   * @param userId The employee's user ID to delete
   */
  suspend fun deleteUser(userId: String)
}
