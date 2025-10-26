package com.android.sample.model.authentification

import com.github.se.bootcamp.model.authentication.AuthRepository


// --- Fake repository for testing ---

class FakeAuthRepository(private val user: User? = null) : AuthRepository {
    override fun getCurrentUser(): User? = user

    override suspend fun signInWithGoogle(
        credential: androidx.credentials.Credential
    ): Result<User> {
        return user?.let { Result.success(it) } ?: Result.failure(Exception("No user"))
    }

    override fun signOut(): Result<Unit> = Result.success(Unit)
}