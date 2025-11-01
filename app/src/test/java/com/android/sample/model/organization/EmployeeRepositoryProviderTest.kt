package com.android.sample.model.organization

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
    EmployeeRepositoryProvider.init(stub)

    val result = EmployeeRepositoryProvider.repository.getMyRole()
    assertThat(result).isEqualTo(Role.ADMIN)
  }

  @Test(expected = IllegalStateException::class)
  fun provider_throws_if_not_initialized() {
    val field = EmployeeRepositoryProvider::class.java.getDeclaredField("_repository")
    field.isAccessible = true
    field.set(EmployeeRepositoryProvider, null)

    EmployeeRepositoryProvider.repository
  }
}
