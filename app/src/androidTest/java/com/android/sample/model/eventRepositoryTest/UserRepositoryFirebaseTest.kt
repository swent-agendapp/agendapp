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

@RunWith(AndroidJUnit4::class)
class UserRepositoryFirebaseTest : FirebaseEmulatedTest() {

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
    val user1 = User("user-1", "Alice", "alice@example.com")
    val user2 = User("user-2", "Bob", "bob@example.com")

    repository.newUser(user1)
    repository.newUser(user2)

    val users = repository.getUsers()

    Assert.assertEquals(2, users.size)
    Assert.assertTrue(users.any { it.id == user1.id && it.email == user1.email })
    Assert.assertTrue(users.any { it.id == user2.id && it.email == user2.email })
  }

  @Test
  fun deleteUser_shouldRemoveUser() = runBlocking {
    val user = User("to-delete", "Charlie", "charlie@example.com")

    repository.newUser(user)

    var users = repository.getUsers()
    Assert.assertTrue(users.any { it.id == user.id })

    repository.deleteUser(user.id)

    users = repository.getUsers()
    Assert.assertFalse(users.any { it.id == user.id })
  }

  @Test
  fun newUser_withBlankUserId_shouldThrow() = runBlocking {
    val invalidUser = User("", "NoId", "noid@example.com")

    try {
      repository.newUser(invalidUser)
      Assert.fail("Expected IllegalArgumentException for blank userId")
    } catch (_: IllegalArgumentException) {}
  }

  @Test
  fun getUsers_shouldIgnoreMalformedDocuments() = runBlocking {
    val validUser = User("valid-user", "Valid", "valid@example.com")
    repository.newUser(validUser)

    val malformedId = "malformed-" + UUID.randomUUID().toString()

    val malformedData =
        mapOf(
            "userId" to malformedId, // missing displayName, missing email → invalid
        )

    FirebaseEmulator.firestore.collection("users").document(malformedId).set(malformedData).await()

    val users = repository.getUsers()

    Assert.assertTrue(users.any { it.id == validUser.id })
    Assert.assertFalse(users.any { it.id == malformedId })
  }
}
