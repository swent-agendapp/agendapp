package com.android.sample.utils

import android.util.Log
import com.android.sample.model.calendar.EVENTS_COLLECTION_PATH
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryFirebase
import com.android.sample.model.calendar.EventRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.junit.After
import org.junit.Before

/**
 * Base test class for tests that require Firebase emulators. Handles setup, teardown, and
 * connection to local emulators for Firestore and Auth.
 */
open class FirebaseEmulatedTest {

  protected val emulatedFirestore = Firebase.firestore
  protected val emulatedAuth = Firebase.auth

  fun createInitializedRepository(): EventRepository {
    return EventRepositoryFirebase(db = emulatedFirestore)
  }

  suspend fun getEventsCount(): Int {
    return emulatedFirestore.collection(EVENTS_COLLECTION_PATH).get().await().size()
  }

  private suspend fun clearTestCollection() {
    val events = emulatedFirestore.collection(EVENTS_COLLECTION_PATH).get().await()
    events?.forEach { it.reference.delete() }
  }

  /**
   * Sets up the test environment before each test.
   * - Checks if emulators are running
   * - Connects Firebase to the emulators
   * - Sets the repository to use the emulated Firestore
   * - Ensures the test collection is empty
   */
  @Before
  open fun setUp() {
    /** Verify that the emulators are running */
    checkIfEmulatorsAreRunning()

    /** Connect Firebase to the emulators */
    useEmulators()

    EventRepositoryProvider.repository = createInitializedRepository()

    runTest {
      val eventsCount = getEventsCount()
      if (eventsCount > 0) {
        Log.w(
            "FirebaseEmulatedTest",
            "Warning: Test collection is not empty at the beginning of the test, count: $eventsCount",
        )
        clearTestCollection()
      }
      assert(value = getEventsCount() == 0) {
        "Test collection is not empty at the beginning of the test, clearing failed."
      }
    }
  }

  /**
   * Cleans up the test environment after each test.
   * - Clears the test collection
   * - Signs out any authenticated user
   * - Clears emulated Firestore and Auth data
   */
  @After
  open fun tearDown() {
    runTest { clearTestCollection() }
    emulatedAuth.signOut()
    Firestore.clear()
    Auth.clear()
  }

  /**
   * Checks if the Firebase emulators are running by sending a request to the emulator endpoint.
   * Throws an IllegalStateException if not running.
   */
  private fun checkIfEmulatorsAreRunning() {
    val client = OkHttpClient()
    val request = Request.Builder().url(EMULATORS).build()

    try {
      val response = client.newCall(request).execute()
      if (!response.isSuccessful) {
        throw IllegalStateException("Firebase Emulators are not running.")
      }
    } catch (e: IOException) {
      throw IllegalStateException("Firebase Emulators are not running. (${e.message})")
    }
  }

  /**
   * Configures Firestore and Auth to use the local emulator instances. Throws an
   * IllegalStateException if unable to connect.
   */
  private fun useEmulators() {
    try {
      emulatedFirestore.useEmulator(HOST, Firestore.PORT)
      emulatedAuth.useEmulator(HOST, Auth.PORT)
    } catch (e: IllegalStateException) {
      Log.i("FirebaseEmulatedTest", "Firebase Emulators are already in use.", e)
    } finally {
      val currentHost = Firebase.firestore.firestoreSettings.host
      if (!currentHost.contains(HOST)) {
        throw IllegalStateException("Failed to connect to Firebase Emulators.")
      }
    }
  }

  companion object {
    const val HOST = "10.0.2.2"
    const val EMULATORS = "http://10.0.2.2:4400/emulators"
    const val FIRESTORE_PORT = 8080
    const val AUTH_PORT = 9099
  }

  /** Helper object for Firestore emulator operations. */
  object Firestore {
    const val PORT = 8080

    /** Clears all documents from the emulated Firestore database. */
    fun clear() {
      val projectId = FirebaseApp.getInstance().options.projectId
      val endpoint =
          "http://${HOST}:$PORT/emulator/v1/projects/$projectId/databases/(default)/documents"

      val client = OkHttpClient()
      val request = Request.Builder().url(endpoint).delete().build()
      val response = client.newCall(request).execute()
      Log.e("Firestore", "Cleared")
      if (!response.isSuccessful) {
        throw IOException("Failed to clear Firestore.")
      }
    }
  }

  /** Helper object for Auth emulator operations. */
  object Auth {
    const val PORT = 9099

    /** Clears all user accounts from the emulated Auth database. */
    fun clear() {
      val projectId = FirebaseApp.getInstance().options.projectId
      val endpoint = "http://${HOST}:$PORT/emulator/v1/projects/$projectId/accounts"

      val client = OkHttpClient()
      val request = Request.Builder().url(url = endpoint).delete().build()
      val response = client.newCall(request).execute()

      if (!response.isSuccessful) {
        throw IOException("Failed to clear Auth.")
      }
    }
  }
}
