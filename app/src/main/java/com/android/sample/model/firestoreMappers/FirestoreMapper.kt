package com.android.sample.model.firestoreMappers

import com.google.firebase.firestore.DocumentSnapshot

/**
 * Generic interface for mapping between Firestore documents and model objects.
 *
 * @param T The model type (e.g., Event, Organization, etc.)
 */
interface FirestoreMapper<T> {

  /**
   * Converts a Firestore document to a model object.
   *
   * @param document Firestore document snapshot.
   * @return The model object, or null if conversion fails.
   */
  fun fromDocument(document: DocumentSnapshot): T?

  /**
   * Converts a raw Map (as returned by Firestore for nested objects) into a model object.
   *
   * This is useful when Firestore returns nested data structures as `Map<String, Any>` rather than
   * full `DocumentSnapshot` objects.
   *
   * For example, when reading a document that contains a list of sub-objects:
   * ```kotlin
   * val admins = document["admins"] as? List<Map<String, Any?>> ?: emptyList()
   * val users = admins.mapNotNull { UserMapper.fromMap(it) }
   * ```
   *
   * @param data A key-value map representing the Firestore object.
   * @return The model object, or null if conversion fails.
   */
  fun fromMap(data: Map<String, Any?>): T?

  /**
   * Converts a model object to a Map suitable for Firestore storage.
   *
   * @param model The model object.
   * @return Map representation of the object.
   */
  fun toMap(model: T): Map<String, Any?>

  /**
   * Helper function: convert either a DocumentSnapshot or a Map<String, Any?> to T. Useful when
   * dealing with mixed sources (Firestore documents or offline Maps / test data).
   *
   * @param input Either a DocumentSnapshot or a Map<String, Any?>.
   * @return The model object, or null if conversion fails or input type is unsupported.
   */
  fun fromAny(input: Any?): T? {
    return when (input) {
      is DocumentSnapshot -> fromDocument(document = input)
      is Map<*, *> -> @Suppress("UNCHECKED_CAST") fromMap(data = input as Map<String, Any?>)
      else -> null
    }
  }
}
