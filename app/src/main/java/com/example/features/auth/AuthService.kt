package com.example.features.auth

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false
)

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: AuthUser) : AuthState()
    data class Unauthenticated(val message: String? = null) : AuthState()
    data class Error(val errorMessage: String) : AuthState()
    data class PasswordResetSent(val email: String) : AuthState()
}

/**
 * Clean Architecture AuthService supporting Firebase Authentication simulation and real API contract.
 * Allows users to register with email/password, sign in, reset password, and switch between states.
 */
class AuthService {
    private val _authState = MutableStateFlow<AuthState>(
        AuthState.Authenticated(
            AuthUser(
                uid = "student_usr_101",
                email = "student@edu.qcm.app",
                displayName = "طالب جامعي متميز"
            )
        )
    )
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    suspend fun signIn(email: String, pass: String): Result<AuthUser> {
        _authState.value = AuthState.Loading
        delay(600) // Realistic network delay
        if (email.isBlank() || !email.contains("@")) {
            val err = "يرجى إدخال بريد إلكتروني صالح"
            _authState.value = AuthState.Error(err)
            return Result.failure(IllegalArgumentException(err))
        }
        if (pass.length < 6) {
            val err = "كلمة المرور يجب أن لا تقل عن 6 أحرف"
            _authState.value = AuthState.Error(err)
            return Result.failure(IllegalArgumentException(err))
        }

        val name = email.substringBefore("@").replace(".", " ").capitalizeWords()
        val user = AuthUser(
            uid = "usr_${Math.abs(email.hashCode())}",
            email = email,
            displayName = name.ifBlank { "طالب QCM" }
        )
        _authState.value = AuthState.Authenticated(user)
        return Result.success(user)
    }

    suspend fun signUp(name: String, email: String, pass: String): Result<AuthUser> {
        _authState.value = AuthState.Loading
        delay(700)
        if (email.isBlank() || !email.contains("@")) {
            val err = "يرجى إدخال بريد إلكتروني صحيح"
            _authState.value = AuthState.Error(err)
            return Result.failure(IllegalArgumentException(err))
        }
        if (pass.length < 6) {
            val err = "كلمة المرور يجب أن لا تقل عن 6 أحرف"
            _authState.value = AuthState.Error(err)
            return Result.failure(IllegalArgumentException(err))
        }

        val user = AuthUser(
            uid = "usr_${Math.abs(email.hashCode())}",
            email = email,
            displayName = name.ifBlank { "مستخدم جديد" }
        )
        _authState.value = AuthState.Authenticated(user)
        return Result.success(user)
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        _authState.value = AuthState.Loading
        delay(500)
        if (email.isBlank() || !email.contains("@")) {
            val err = "يرجى كتابة البريد الإلكتروني لإرسال رابط الاستعادة"
            _authState.value = AuthState.Error(err)
            return Result.failure(IllegalArgumentException(err))
        }
        _authState.value = AuthState.PasswordResetSent(email)
        return Result.success(Unit)
    }

    fun signOut() {
        _authState.value = AuthState.Unauthenticated()
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated()
        }
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}
