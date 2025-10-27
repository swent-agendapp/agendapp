package com.android.sample.model.organization

interface EmployeeRepository {

  /** @return The list of employees. */
  suspend fun getEmployees(): List<Employee>

  /**
   * Create or update a employee
   *
   * @param employee The employee to create
   */
  suspend fun newEmployee(employee: Employee)

  /**
   * Delete an employee by ID
   *
   * @param userId The employee's user ID to delete
   */
  suspend fun deleteEmployee(userId: String)

  /**
   * Get the current user's role, or null if not in the employees list
   *
   * @return The current user's Role, or null
   */
  suspend fun getMyRole(): Role?
}
