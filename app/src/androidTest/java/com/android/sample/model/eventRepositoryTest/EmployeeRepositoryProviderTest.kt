package com.android.sample.model.eventRepositoryTest

import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UserRepositoryProvider
import org.junit.Assert.assertSame
import org.junit.Test

class EmployeeRepositoryProviderTest {
  @Test
  fun repository_returns_the_instance_we_set() {
    val fake =
        object : UserRepository {
          override suspend fun getUsers(): List<Employee> = emptyList()

          override suspend fun newEmployee(employee: Employee) {}

          override suspend fun deleteUser(userId: String) {}

          override suspend fun getMyRole(): Role? = Role.ADMIN
        }
    UserRepositoryProvider.repository = fake
    assertSame(fake, UserRepositoryProvider.repository)
  }
}
