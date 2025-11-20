package com.example.guessemogi.viewmodel.authviewmodel


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.guessemogi.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthException

class AuthViewModel(

) : ViewModel() {
    private val authRepository = AuthRepository()
    val email = mutableStateOf("")
    val password = mutableStateOf("")
    val loading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    fun onEmailChange(value: String) { email.value = value }
    fun onPasswordChange(value: String) { password.value = value }

    // Help function for translating common Firebase errors
    private fun translateAuthError(error: String?): String {
        if (error == null) return "Error desconocido. Por favor, revisa tu conexión."

        return when {

            error.contains("wrong-password", ignoreCase = true) -> "Contraseña incorrecta."
            error.contains("user-not-found", ignoreCase = true) -> "No existe un usuario con este correo electrónico."
            error.contains("invalid-email", ignoreCase = true) -> "El formato del correo es inválido."
            error.contains("email-already-in-use", ignoreCase = true) -> "El correo electrónico ya está registrado."
            error.contains("weak-password", ignoreCase = true) -> "La contraseña debe tener al menos 6 caracteres."
            error.contains("user-disabled", ignoreCase = true) -> "Esta cuenta ha sido deshabilitada."
            else -> "Error: $error. Por favor, inténtalo de nuevo."
        }
    }

    fun login(onSuccess: () -> Unit) {
        loading.value = true
        errorMessage.value = null
        authRepository.loginUser(email.value, password.value) { ok, error ->
            loading.value = false
            if (ok) onSuccess() else errorMessage.value = translateAuthError(error)
        }
    }

    fun register(onSuccess: () -> Unit) {
        loading.value = true
        errorMessage.value = null
        authRepository.registerUser(email.value, password.value) { ok, error ->
            loading.value = false
            if (ok) onSuccess() else errorMessage.value = translateAuthError(error)
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun getUid() : String? {
        return authRepository.getCurrentUserId()
    }
}