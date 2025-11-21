package com.android.sample.utils

import com.android.sample.model.calendar.EventRepository
import com.android.sample.model.calendar.EventRepositoryFirebase
import com.android.sample.model.calendar.EventRepositoryProvider
import com.android.sample.model.constants.FirestoreConstants
import com.android.sample.model.map.MapRepository
import com.android.sample.model.map.MapRepositoryFirebase
import com.android.sample.model.map.MapRepositoryProvider
import com.android.sample.model.organization.OrganizationRepository
import com.android.sample.model.organization.OrganizationRepositoryFirebase
import com.android.sample.model.organization.OrganizationRepositoryProvider
import com.android.sample.model.replacement.ReplacementRepositoryFirebase
import com.android.sample.model.replacement.ReplacementRepositoryProvider
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

/**
 * Base test class for tests that require Firebase emulators. Handles setup, teardown, and
 * connection to local emulators for Firestore and Auth.
 */
open class FirebaseEmulatedTest {

  // List of ALL Firestore collections used in the app (dynamic cleanup)
  private val allCollections = FirestoreConstants.ALL_COLLECTIONS

  // --- Creation methods for initialized repositories using the emulated Firestore ---

  fun createInitializedEventRepository(): EventRepository {
    return EventRepositoryFirebase(db = FirebaseEmulator.firestore)
  }

  fun createInitializedMapRepository(): MapRepository {
    return MapRepositoryFirebase(db = FirebaseEmulator.firestore)
  }

  fun createInitializedOrganizationRepository(): OrganizationRepository {
    return OrganizationRepositoryFirebase(db = FirebaseEmulator.firestore)
  }

  fun createInitializedReplacementRepository(): ReplacementRepositoryFirebase {
    return ReplacementRepositoryFirebase(db = FirebaseEmulator.firestore)
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

  private suspend fun countDocs(path: String): Int {
    return FirebaseEmulator.firestore.collection(path).get().await().size()
  }

  private suspend fun clearAllCollections() {
    allCollections.forEach { clearCollection(it) }
  }

  private suspend fun assertAllCollectionsEmpty() {
    val nonEmpty = allCollections.filter { countDocs(it) > 0 }

    assert(nonEmpty.isEmpty()) { "Some Firestore collections were not empty: $nonEmpty" }
  }

  /**
   * Sets up the test environment before each test.
   * - Clears all Firestore collections to ensure a clean state
   * - Asserts that all collections are empty
   */
  @Before
  open fun setUp() {
    MapRepositoryProvider.repository = createInitializedMapRepository()
    OrganizationRepositoryProvider.repository = createInitializedOrganizationRepository()
    EventRepositoryProvider.repository = createInitializedEventRepository()
    ReplacementRepositoryProvider.repository = createInitializedReplacementRepository()
    runTest {
      clearAllCollections()
      assertAllCollectionsEmpty()
    }
  }

  /**
   * Cleans up the test environment after each test.
   * - Clears all collections
   * - Resets emulator auth + Firestore state
   */
  @After
  open fun tearDown() {
    runTest { clearAllCollections() }

    FirebaseEmulator.clearFirestoreEmulator()

    if (FirebaseEmulator.isRunning) {
      FirebaseEmulator.auth.signOut()
      FirebaseEmulator.clearAuthEmulator()
    }
  }
}
