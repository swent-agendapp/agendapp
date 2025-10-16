package com.android.sample.utils

import android.util.Log
import com.android.sample.model.calendar.EVENTS_COLLECTION_PATH
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryFirebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlin.text.get
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

/**
 * Base test class for tests that require Firebase emulators. Handles setup, teardown, and
 * connection to local emulators for Firestore and Auth.
 */
open class FirebaseEmulatedTest {

  fun createInitializedRepository(): EventRepository {
    return EventRepositoryFirebase(db = FirebaseEmulator.firestore)
  }

  suspend fun getEventsCount(): Int {
    return FirebaseEmulator.firestore.collection(EVENTS_COLLECTION_PATH).get().await().size()
  }

  private suspend fun clearTestCollection() {
    val user = FirebaseEmulator.auth.currentUser ?: return
    val events = FirebaseEmulator.firestore.collection(EVENTS_COLLECTION_PATH).get().await()

    val batch = FirebaseEmulator.firestore.batch()
    events.documents.forEach { batch.delete(it.reference) }
    batch.commit().await()

    assert(getEventsCount() == 0) {
      "Test collection is not empty after clearing, count: ${getEventsCount()}"
    }
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
    FirebaseEmulator.clearFirestoreEmulator()
    if (FirebaseEmulator.isRunning) {
      FirebaseEmulator.auth.signOut()
      FirebaseEmulator.clearAuthEmulator()
    }
  }
}
