package com.example.hangon.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data object Success : AuthResult()
    data class Failure(val message: String) : AuthResult()
}

interface AuthRepository {
    val isLoggedIn: Boolean
    suspend fun register(email: String, password: String, displayName: String): AuthResult
    suspend fun login(email: String, password: String): AuthResult
    suspend fun getIdToken(): String?
    fun logout()
}

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override val isLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null

    override suspend fun register(email: String, password: String, displayName: String): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            firebaseUser
                ?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(displayName).build())
                ?.await()
            // Force a token refresh: the ID token minted at sign-up is already cached and
            // won't carry the "name" claim from the profile update above. Without this, the
            // first backend call would use the stale token, and since the backend only sets
            // display_name when it first creates the User row, it would stay NULL forever.
            firebaseUser?.getIdToken(true)?.await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "Registrasi gagal, coba lagi.")
        }
    }

    override suspend fun login(email: String, password: String): AuthResult {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Failure(e.message ?: "Login gagal, periksa email dan password Anda.")
        }
    }

    override suspend fun getIdToken(): String? {
        return firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
    }

    override fun logout() {
        firebaseAuth.signOut()
    }
}
