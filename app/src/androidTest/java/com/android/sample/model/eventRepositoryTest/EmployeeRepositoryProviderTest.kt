package com.android.sample.model.eventRepositoryTest

import com.android.sample.model.organization.data.Employee
import com.android.sample.model.organization.data.Role
import com.android.sample.model.organization.repository.EmployeeRepository
import com.android.sample.model.organization.repository.EmployeeRepositoryProvider
import org.junit.Assert.assertSame
import org.junit.Test

class EmployeeRepositoryProviderTest {
  @Test
  fun repository_returns_the_instance_we_set() {
    val fake =
        object : EmployeeRepository {
          override suspend fun getEmployees(): List<Employee> = emptyList()

          override suspend fun newEmployee(employee: Employee) {}

          override suspend fun deleteEmployee(userId: String) {}

          override suspend fun getMyRole(): Role? = Role.ADMIN
        }
    EmployeeRepositoryProvider.repository = fake
    assertSame(fake, EmployeeRepositoryProvider.repository)
  }
}
