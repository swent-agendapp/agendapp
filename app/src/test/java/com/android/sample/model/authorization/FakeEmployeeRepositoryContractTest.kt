package com.android.sample.model.authorization

import com.android.sample.model.authentication.User
import com.android.sample.model.organization.data.Employee
import com.android.sample.model.organization.data.Role
import com.android.sample.model.organization.repository.EmployeeRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FakeEmployeeRepositoryContractTest {

  private class Fake : EmployeeRepository {
    val l = mutableListOf<Employee>()
    var role: Role? = null

    override suspend fun getEmployees(): List<Employee> = l

    override suspend fun newEmployee(employee: Employee) {
      l.removeAll { it.user.id == employee.user.id }
      l.add(employee)
    }

    override suspend fun deleteEmployee(userId: String) {
      l.removeAll { it.user.id == userId }
    }

    override suspend fun getMyRole(): Role? = role
  }

  @Test
  fun newEmployee_upserts_and_getEmployees_returnsList() = runTest {
    val fake = Fake()
    fake.newEmployee(Employee(User("u1", "Nathan", "nathan@rien.com"), Role.EMPLOYEE))
    fake.newEmployee(Employee(User("u2", "Emilien", "emilien@rien.com"), Role.ADMIN))
    assertThat(fake.getEmployees()).hasSize(2)

    fake.newEmployee(Employee(User("u2", "Emi2", "emi2@rien.com"), role = Role.ADMIN))
    assertThat(fake.getEmployees().first { it.user.id == "u2" }.user.displayName).isEqualTo("Emi2")
  }

  @Test
  fun deleteEmployee_removesUser() = runTest {
    val fake = Fake()
    fake.newEmployee(Employee(User("u1", "Nathan", "nathan@rien.com"), Role.EMPLOYEE))
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
