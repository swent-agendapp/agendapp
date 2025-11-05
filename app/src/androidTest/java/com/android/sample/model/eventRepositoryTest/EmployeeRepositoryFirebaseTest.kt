package com.android.sample.model.eventRepositoryTest

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.authentification.FakeAuthRepository
import com.android.sample.model.authentification.User
import com.android.sample.model.organization.Employee
import com.android.sample.model.organization.EmployeeRepository
import com.android.sample.model.organization.EmployeeRepositoryFirebase
import com.android.sample.model.organization.Role
import com.android.sample.utils.FirebaseEmulatedTest
import com.android.sample.utils.FirebaseEmulator
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(value = AndroidJUnit4::class)
class EmployeeRepositoryFirebaseTest : FirebaseEmulatedTest() {

  private lateinit var repository: EmployeeRepository

  @Before
  override fun setUp() {
    super.setUp()

    val authRepository = FakeAuthRepository(user = null)

    repository =
        EmployeeRepositoryFirebase(
            db = FirebaseEmulator.firestore,
            authRepository = authRepository,
        )
  }

  @Test
  fun newEmployee_andGetEmployees_shouldWork() = runBlocking {
    val employee1 =
        Employee(
            user = User("user-1", "Alice", "alice@example.com"),
            role = Role.ADMIN,
        )
    val employee2 =
        Employee(
            user = User("user-2", "Bob", "bob@example.com"),
            role = Role.EMPLOYEE,
        )

    repository.newEmployee(employee1)
    repository.newEmployee(employee2)

    val employees = repository.getEmployees()

    Assert.assertEquals(2, employees.size)
    Assert.assertTrue(
        employees.any { it.user.id == employee1.user.id && it.role == employee1.role })
    Assert.assertTrue(
        employees.any { it.user.id == employee2.user.id && it.role == employee2.role })
  }

  @Test
  fun deleteEmployee_shouldRemoveEmployee() = runBlocking {
    val employee =
        Employee(
            user = User("to-delete", "Charlie", "charlie@example.com"),
            role = Role.EMPLOYEE,
        )

    repository.newEmployee(employee)

    var employees = repository.getEmployees()
    Assert.assertTrue(employees.any { it.user.id == employee.user.id })

    repository.deleteEmployee(employee.user.id)

    employees = repository.getEmployees()
    Assert.assertFalse(employees.any { it.user.id == employee.user.id })
  }

  @Test
  fun newEmployee_withBlankUserId_shouldThrow() = runBlocking {
    val invalidEmployee =
        Employee(
            user = User("", "NoId", "noid@example.com"),
            role = Role.EMPLOYEE,
        )

    try {
      repository.newEmployee(invalidEmployee)
      Assert.fail("Expected IllegalArgumentException for blank userId")
    } catch (_: IllegalArgumentException) {}
  }

  @Test
  fun getEmployees_shouldIgnoreMalformedDocuments() = runBlocking {
    val validEmployee =
        Employee(
            user = User("valid-user", "Valid", "valid@example.com"),
            role = Role.ADMIN,
        )
    repository.newEmployee(validEmployee)

    val malformedId = "malformed-" + UUID.randomUUID().toString()
    val malformedData =
        mapOf(
            "userId" to malformedId,
            "displayName" to "Malformed User",
            "email" to "malformed@example.com",
        )

    FirebaseEmulator.firestore
        .collection("employees")
        .document(malformedId)
        .set(malformedData)
        .await()

    val employees = repository.getEmployees()

    Assert.assertTrue(employees.any { it.user.id == validEmployee.user.id })
    Assert.assertFalse(employees.any { it.user.id == malformedId })
  }
}
