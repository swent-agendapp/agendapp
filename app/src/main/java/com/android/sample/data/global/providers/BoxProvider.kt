package com.android.sample.data.global.providers

import android.content.Context
import com.android.sample.data.local.objects.EventEntity
import com.android.sample.data.local.objects.MyObjectBox
import io.objectbox.Box
import io.objectbox.BoxStore

/**
 * Singleton object to provide access to the ObjectBox BoxStore and Boxes.
 *
 * This object ensures that the BoxStore is initialized only once and provides methods to access
 * specific Boxes for entity types (e.g., EventEntity with eventBox()).
 */
object BoxProvider {

  // Late-initialized BoxStore instance
  private lateinit var boxStore: BoxStore

  // Initialize the BoxStore with the application context
  fun init(context: Context) {
    if (!::boxStore.isInitialized) {
      boxStore = MyObjectBox.builder().androidContext(context).build()
    }
  }

  // Initialize an in-memory BoxStore for testing purposes (not persisted to disk)
  fun initInMemory(context: Context) {
    if (!::boxStore.isInitialized) {
      boxStore =
          MyObjectBox.builder()
              .androidContext(context)
              // Unique name for each test run with timestamp of the run
              .inMemory("test-db-${System.currentTimeMillis()}")
              .build()
    }
  }

  // Provide access to the Box for EventEntity
  fun eventBox(): Box<EventEntity> {
    check(::boxStore.isInitialized) {
      "BoxStore not initialized! Call BoxProvider.init(context) first."
    }
    return boxStore.boxFor(EventEntity::class.java)
  }

  // Get the BoxStore instance
  fun getBoxStore(): BoxStore {
    check(::boxStore.isInitialized) {
      "BoxStore not initialized! Call BoxProvider.init(context) first."
    }
    return boxStore
  }

  // Close the BoxStore
  fun close() {
    if (::boxStore.isInitialized) {
      boxStore.close()
    }
  }
}
