package com.android.sample.model.authorization

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.organization.Employee
import com.android.sample.model.organization.EmployeeRepository
import com.android.sample.model.organization.EmployeeRepositoryProvider
import com.android.sample.model.organization.Role
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(value = AndroidJUnit4::class)
private class FakeEmployeeRepository(
    var roleForCurrentUser: Role? = null,
    var employees: MutableList<Employee> = mutableListOf()
) : EmployeeRepository {

    override suspend fun getEmployees(): List<Employee> = employees

    override suspend fun newEmployee(employee: Employee) {
        employees.removeAll { it.userId == employee.userId }
        employees.add(employee)
    }

    override suspend fun deleteEmployee(userId: String) {
        employees.removeAll { it.userId == userId }
    }

    override suspend fun getMyRole(): Role? = roleForCurrentUser
}

class AuthorizationServiceTest {

    private lateinit var fakeRepo: FakeEmployeeRepository
    private lateinit var authz: AuthorizationService

    @Before
    fun setUp() {
        fakeRepo = FakeEmployeeRepository()
        EmployeeRepositoryProvider.repository = fakeRepo
        authz = AuthorizationService()
    }

    @Test
    fun getMyRole_returns_null_when_user_not_registered() = runTest {
        fakeRepo.roleForCurrentUser = null
        val role = authz.getMyRole()
        assertThat(role).isNull()
    }

    @Test
    fun canEditCourses_is_true_for_ADMIN() = runTest {
        fakeRepo.roleForCurrentUser = Role.ADMIN
        assertThat(authz.canEditCourses()).isTrue()
    }

    @Test
    fun canEditCourses_is_false_for_EMPLOYEE() = runTest {
        fakeRepo.roleForCurrentUser = Role.EMPLOYEE
        assertThat(authz.canEditCourses()).isFalse()
    }

    @Test(expected = IllegalAccessException::class)
    fun requireAdmin_throws_when_not_admin() = runTest {
        fakeRepo.roleForCurrentUser = Role.EMPLOYEE
        authz.requireAdmin()
    }

    @Test
    fun requireAdmin_does_not_throw_for_admin() = runTest {
        fakeRepo.roleForCurrentUser = Role.ADMIN
        authz.requireAdmin()
    }
}