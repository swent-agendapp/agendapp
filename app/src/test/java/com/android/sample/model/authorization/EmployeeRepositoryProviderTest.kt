package com.android.sample.model.authorization

import com.android.sample.model.organization.Employee
import com.android.sample.model.organization.EmployeeRepository
import com.android.sample.model.organization.EmployeeRepositoryProvider
import com.android.sample.model.organization.Role
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test

private class StubEmployeeRepo(private val role: Role?) : EmployeeRepository {
  override suspend fun getEmployees(): List<Employee> = emptyList()

  override suspend fun newEmployee(employee: Employee) {}

  override suspend fun deleteEmployee(userId: String) {}

  override suspend fun getMyRole(): Role? = role
}

class EmployeeRepositoryProviderTest {

  @Test
  fun provider_holds_assigned_instance() = runBlocking {
    val stub = StubEmployeeRepo(Role.ADMIN)
    EmployeeRepositoryProvider.repository = stub

    val result = EmployeeRepositoryProvider.repository.getMyRole()
    assertThat(result).isEqualTo(Role.ADMIN)
  }
}
