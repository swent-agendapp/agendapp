package com.android.sample.model.constants

/**
 * Constants for Firestore collection paths used in the app.
 *
 * If you add a new collection path here, remember to update ALL_COLLECTIONS.
 */
object FirestoreConstants {
  const val EVENTS_COLLECTION_PATH = "events"
  const val CATEGORIES_COLLECTION_PATH = "categories"
  const val ORGANIZATIONS_COLLECTION_PATH = "organizations"
  const val COLLECTION_USERS = "users"
  const val COLLECTION_ADMINS = "admins"
  const val REPLACEMENTS_COLLECTION_PATH = "replacements"
  const val INVITATIONS_COLLECTION_PATH = "invitations"
  const val MAP_COLLECTION_PATH = "areas"

  val ALL_COLLECTIONS =
      listOf(
          EVENTS_COLLECTION_PATH,
          CATEGORIES_COLLECTION_PATH,
          ORGANIZATIONS_COLLECTION_PATH,
          COLLECTION_USERS,
          REPLACEMENTS_COLLECTION_PATH,
          EMPLOYEES_COLLECTION_PATH,
          INVITATIONS_COLLECTION_PATH,
          MAP_COLLECTION_PATH,
          COLLECTION_ADMINS,
      )
}
