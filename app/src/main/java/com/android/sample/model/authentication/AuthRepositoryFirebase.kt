package com.github.se.bootcamp.model.authentication

import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import com.android.sample.model.authentication.User
import com.android.sample.model.constants.FirestoreConstants.COLLECTION_USERS
import com.android.sample.model.firestoreMappers.UserMapper
import com.android.sample.model.versioning.withUpdatedVersion
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase as KtxFirebase
import kotlinx.coroutines.tasks.await

/**
 * Firebase-based implementation of [AuthRepository].
 *
 * Handles user authentication and data retrieval using Firebase services. This includes:
 * - Signing in with Google through the Credential Manager API
 * - Authenticating with Firebase using Google credentials
 * - Fetching user information from Firestore
 * - Handling sign-out operations
 *
 * @param auth The [FirebaseAuth] instance managing authentication state.
 * @param helper A [GoogleSignInHelper] used to extract and convert Google ID token credentials.
 * @param firestore The [FirebaseFirestore] instance used to retrieve user documents.
 */
class AuthRepositoryFirebase(
    private val auth: FirebaseAuth = Firebase.auth,
    private val helper: GoogleSignInHelper = DefaultGoogleSignInHelper(),
    private val firestore: FirebaseFirestore = KtxFirebase.firestore
) : AuthRepository {

  fun getGoogleSignInOption(serverClientId: String) =
      GetSignInWithGoogleOption.Builder(serverClientId = serverClientId).build()

  /** Maps a [FirebaseUser] to our domain [User] model. */
  private fun FirebaseUser.toDomainUser() =
      User(id = uid, displayName = displayName, email = email).withUpdatedVersion()

  override suspend fun signInWithGoogle(credential: Credential): Result<User> {
    return try {
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val idToken = helper.extractIdTokenCredential(credential.data).idToken
        val firebaseCred = helper.toFirebaseCredential(idToken)

        // Sign in with Firebase
        val firebaseUser =
            auth.signInWithCredential(firebaseCred).await().user
                ?: return Result.failure(
                    IllegalStateException("Login failed : Could not retrieve user information"))

        return Result.success(firebaseUser.toDomainUser())
      } else {
        return Result.failure(
            IllegalStateException("Login failed: Credential is not of type Google ID"))
      }
    } catch (e: Exception) {
      Result.failure(
          IllegalStateException("Login failed: ${e.localizedMessage ?: "Unexpected error."}"))
    }
  }

  override fun signOut(): Result<Unit> {
    return try {
      // Firebase sign out
      auth.signOut()

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(
          IllegalStateException("Logout failed: ${e.localizedMessage ?: "Unexpected error."}"))
    }
  }

  override fun getCurrentUser(): User? {
    return auth.currentUser?.toDomainUser()
  }

  /**
   * Fetches a user by ID from the repository using a filtered query.
   *
   * @param userId The ID of the user to retrieve.
   * @return The [User] matching the given ID, or null if not found.
   */
  override suspend fun getUserById(userId: String): User? {
    return try {
      // Filter with the id field
      val snapshot =
          firestore.collection(COLLECTION_USERS).whereEqualTo("id", userId).limit(1).get().await()

      val doc = snapshot.documents.firstOrNull() ?: return null
      UserMapper.fromMap(doc.data ?: emptyMap()) ?: UserMapper.fromDocument(doc)
    } catch (_: Exception) {
      null
    }
  }
}
