package com.example.hangon.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data object Success : AuthResult()
    data class Failure(val message: String) : AuthResult()
}

interface AuthRepository {
    val isLoggedIn: Boolean
    val email: String?
    suspend fun register(email: String, password: String, displayName: String): AuthResult
    suspend fun login(email: String, password: String): AuthResult
    suspend fun getIdToken(): String?
    suspend fun updateDisplayName(displayName: String): AuthResult
    fun logout()
}

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override val isLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null

    override val email: String?
        get() = firebaseAuth.currentUser?.email

    override suspend fun register(email: String, password: String, displayName: String): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            firebaseUser
                ?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(displayName).build())
                ?.await()
            firebaseUser?.getIdToken(true)?.await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Failure(mapAuthError(e, "Registration failed, please try again."))
        }
    }

    override suspend fun login(email: String, password: String): AuthResult {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Failure(mapAuthError(e, "Login failed, please check your email and password."))
        }
    }

    override suspend fun getIdToken(): String? {
        return firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
    }

    override suspend fun updateDisplayName(displayName: String): AuthResult {
        return try {
            firebaseAuth.currentUser
                ?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(displayName).build())
                ?.await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Failure(mapAuthError(e, "Failed to update name, please try again."))
        }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    private fun mapAuthError(e: Exception, fallback: String): String = when {
        e is FirebaseNetworkException ->
            "Unable to connect. Please check your internet connection."
        e is FirebaseAuthException -> when (e.errorCode) {
            "ERROR_INVALID_EMAIL" -> "That email address looks invalid."
            "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL", "INVALID_LOGIN_CREDENTIALS" ->
                "Incorrect email or password."
            "ERROR_USER_NOT_FOUND" -> "No account found with that email."
            "ERROR_USER_DISABLED" -> "This account has been disabled."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "That email is already registered."
            "ERROR_WEAK_PASSWORD" -> "Password is too weak. Please choose a stronger one."
            "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later."
            else -> e.message ?: fallback
        }
        else -> e.message ?: fallback
    }
}
