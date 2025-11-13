package com.android.sample.model.authRepositoryTest

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.utils.FirebaseEmulatedTest
import com.github.se.bootcamp.model.authentication.AuthRepository
import com.github.se.bootcamp.model.authentication.AuthRepositoryFirebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthRepositoryFirebaseTest : FirebaseEmulatedTest() {

  private lateinit var repository: AuthRepository

  @Before
  override fun setUp() {
    super.setUp()
    repository = AuthRepositoryFirebase()
  }

  @Test
  fun getUserById_returnsUser_whenUserExists() = runBlocking {
    // Create a user document
    Firebase.firestore
        .collection("users")
        .document("doc-alice")
        .set(
            mapOf(
                "id" to "alice-123",
                "displayName" to "Alice",
                "email" to "alice@example.com",
                "phoneNumber" to "+41 79 000 00 00"))
        .await()

    val user = repository.getUserById("alice-123")

    Assert.assertNotNull("Expected an existing user to be returned", user)
    Assert.assertEquals("alice-123", user!!.id)
    Assert.assertEquals("Alice", user.displayName)
    Assert.assertEquals("alice@example.com", user.email)
    Assert.assertEquals("+41 79 000 00 00", user.phoneNumber)
  }

  @Test
  fun getUserById_returnsNull_whenUserDoesNotExist() = runBlocking {
    // Create a user with different id (different from what we will look for)
    Firebase.firestore
        .collection("users")
        .document("doc-bob")
        .set(mapOf("id" to "bob-456", "displayName" to "Bob"))
        .await()

    // Look for a non-existing userId
    val user = repository.getUserById("missing-999")

    Assert.assertNull("Unknown id should return null", user)
  }

  @Test
  fun getUserById_worksWithManyUsers() = runBlocking {
    // Insert multiple users
    val users = Firebase.firestore.collection("users")
    users
        .document("doc-charlie")
        .set(mapOf("id" to "charlie-001", "displayName" to "Charlie"))
        .await()
    users.document("doc-diana").set(mapOf("id" to "diana-002", "displayName" to "Diana")).await()

    // get the second user inserted
    val user = repository.getUserById("diana-002")

    Assert.assertNotNull(user)
    Assert.assertEquals("Diana", user!!.displayName)
    Assert.assertEquals("diana-002", user.id)
  }

  @Test
  fun getUserById_handlesMissingOptionalFields() = runBlocking {
    // Omit optional fields (displayName, email and phoneNumber)
    Firebase.firestore
        .collection("users")
        .document("doc-erin")
        .set(
            mapOf(
                "id" to "erin-777",
            ))
        .await()

    val user = repository.getUserById("erin-777")

    // The user should exist but with optionals fields being null
    Assert.assertNotNull(user)
    Assert.assertNull(user!!.displayName)
    Assert.assertNull(user.email)
    Assert.assertNull(user.phoneNumber)
  }
}
