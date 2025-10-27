package com.android.sample.model.authorization

import com.android.sample.model.organization.Employee
import com.android.sample.model.organization.EmployeeRepository
import com.android.sample.model.organization.Role
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FakeEmployeeRepositoryContractTest {

  private class Fake : EmployeeRepository {
    val l = mutableListOf<Employee>()
    var role: Role? = null

    override suspend fun getEmployees(): List<Employee> = l

    override suspend fun newEmployee(employee: Employee) {
      l.removeAll { it.userId == employee.userId }
      l.add(employee)
    }

    override suspend fun deleteEmployee(userId: String) {
      l.removeAll { it.userId == userId }
    }

    override suspend fun getMyRole(): Role? = role
  }

  @Test
  fun newEmployee_upserts_and_getEmployees_returnsList() = runTest {
    val fake = Fake()
    fake.newEmployee(
        Employee(
            userId = "u1", displayName = "Nathan", email = "nathan@rien.com", role = Role.EMPLOYEE))
    fake.newEmployee(
        Employee(
            userId = "u2", displayName = "Emilien", email = "emilien@rien.com", role = Role.ADMIN))
    assertThat(fake.getEmployees()).hasSize(2)

    fake.newEmployee(
        Employee(userId = "u2", displayName = "Emi2", email = "emi2@rien.com", role = Role.ADMIN))
    assertThat(fake.getEmployees().first { it.userId == "u2" }.displayName).isEqualTo("Emi2")
  }

  @Test
  fun deleteEmployee_removesUser() = runTest {
    val fake = Fake()
    fake.newEmployee(
        Employee(
            userId = "u1", displayName = "Nathan", email = "nathan@rien.com", role = Role.EMPLOYEE))
    fake.deleteEmployee("u1")
    assertThat(fake.getEmployees()).isEmpty()
  }

  @Test
  fun getMyRole_returnsRole() = runTest {
    val fake = Fake()
    fake.role = Role.ADMIN
    assertThat(fake.getMyRole()).isEqualTo(Role.ADMIN)
  }
}
