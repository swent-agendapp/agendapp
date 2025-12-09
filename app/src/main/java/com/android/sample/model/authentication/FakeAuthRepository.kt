package com.android.sample.model.authentication

// --- Fake repository for testing ---

class FakeAuthRepository(private val user: User? = null) : AuthRepository {

  var currUser: User? = user

  fun setCurrentUser(user: User) {
    currUser = user
  }

  fun clearCurrentUser() {
    currUser = null
  }

  override fun getCurrentUser(): User? = currUser

  override suspend fun signInWithGoogle(credential: androidx.credentials.Credential): Result<User> {
    return currUser?.let { Result.success(it) } ?: Result.failure(Exception("No user"))
  }

  override fun signOut(): Result<Unit> = Result.success(Unit)

  override suspend fun getUserById(userId: String): User? {
    return if (currUser?.id == userId) user else null
  }
}
