package com.android.sample.utils

import android.util.Log
import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryFirebase
import com.android.sample.model.constants.FirestoreConstants.EVENTS_COLLECTION_PATH
import com.android.sample.model.constants.FirestoreConstants.ORGANIZATIONS_COLLECTION_PATH
import com.android.sample.model.organization.OrganizationRepository
import com.android.sample.model.organization.OrganizationRepositoryFirebase
import com.android.sample.model.replacement.ReplacementRepositoryFirebase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

/**
 * Base test class for tests that require Firebase emulators. Handles setup, teardown, and
 * connection to local emulators for Firestore and Auth.
 */
open class FirebaseEmulatedTest {

  fun createInitializedEventRepository(): EventRepository {
    return EventRepositoryFirebase(db = FirebaseEmulator.firestore)
  }

  fun createInitializedOrganizationRepository(): OrganizationRepository {
    return OrganizationRepositoryFirebase(db = FirebaseEmulator.firestore)
  }

  fun createInitializedReplacementRepository(): ReplacementRepositoryFirebase {
    return ReplacementRepositoryFirebase(db = FirebaseEmulator.firestore)
  }

  suspend fun getEventsCount(): Int {
    return FirebaseEmulator.firestore.collection(EVENTS_COLLECTION_PATH).get().await().size()
  }

  suspend fun getOrganizationsCount(): Int {
    return FirebaseEmulator.firestore.collection(ORGANIZATIONS_COLLECTION_PATH).get().await().size()
  }

  // --- Generic collection utilities ---
  private suspend fun clearCollection(path: String) {
    val collection = FirebaseEmulator.firestore.collection(path).get().await()
    if (collection.isEmpty) return

    val batch = FirebaseEmulator.firestore.batch()
    collection.documents.forEach { batch.delete(it.reference) }
    batch.commit().await()

    val remaining = FirebaseEmulator.firestore.collection(path).get().await().size()
    assert(remaining == 0) { "Collection $path not empty after clearing, remaining: $remaining" }
  }

  // --- Specific collection helpers ---
  suspend fun clearEventsCollection() = clearCollection(EVENTS_COLLECTION_PATH)

  suspend fun clearOrganizationsCollection() = clearCollection(ORGANIZATIONS_COLLECTION_PATH)

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
      val organizationsCount = getOrganizationsCount()
      if (eventsCount > 0 || organizationsCount > 0) {
        Log.w(
            "FirebaseEmulatedTest",
            "Warning: Test collection is not empty at the beginning of the test, count: $eventsCount events + $organizationsCount organizations.",
        )
        clearEventsCollection()
        clearOrganizationsCollection()
      }
      assert(value = getEventsCount() == 0 && getOrganizationsCount() == 0) {
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
    runTest {
      clearEventsCollection()
      clearOrganizationsCollection()
    }
    FirebaseEmulator.clearFirestoreEmulator()
    if (FirebaseEmulator.isRunning) {
      FirebaseEmulator.auth.signOut()
      FirebaseEmulator.clearAuthEmulator()
    }
  }
}
