package com.android.sample.model.eventRepositoryTest

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.authentication.FakeAuthRepository
import com.android.sample.model.authentication.User
import com.android.sample.model.authentication.UserRepository
import com.android.sample.model.authentication.UsersRepositoryFirebase
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

  private lateinit var repository: UserRepository

  @Before
  override fun setUp() {
    super.setUp()

    val authRepository = FakeAuthRepository(user = null)

    repository =
        UsersRepositoryFirebase(
            db = FirebaseEmulator.firestore,
            authRepository = authRepository,
        )
  }

  @Test
  fun newUser_andGetUsers_shouldWork() = runBlocking {
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

    repository.newUser(employee1)
    repository.newUser(employee2)

    val employees = repository.getUsers()

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

    repository.newUser(employee)

    var employees = repository.getUsers()
    Assert.assertTrue(employees.any { it.user.id == employee.user.id })

    repository.deleteUser(employee.user.id)

    employees = repository.getUsers()
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
      repository.newUser(invalidEmployee)
      Assert.fail("Expected IllegalArgumentException for blank userId")
    } catch (_: IllegalArgumentException) {}
  }

  @Test
  fun getUsers_shouldIgnoreMalformedDocuments() = runBlocking {
    val validEmployee =
        Employee(
            user = User("valid-user", "Valid", "valid@example.com"),
            role = Role.ADMIN,
        )
    repository.newUser(validEmployee)

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

    val employees = repository.getUsers()

    Assert.assertTrue(employees.any { it.user.id == validEmployee.user.id })
    Assert.assertFalse(employees.any { it.user.id == malformedId })
  }
}
