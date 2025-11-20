package com.example.guessemogi.data.repository


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import android.util.Log // Agregado para logging

class AuthRepository(

) {
    private val auth =  FirebaseAuth.getInstance()

    // Función auxiliar para obtener el nombre de usuario (parte del email antes del @)
    private fun getUsernameFromEmail(email: String): String {
        return email.substringBefore('@')
    }

    fun registerUser(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val username = getUsernameFromEmail(email)

                    // 1. Crear la solicitud de cambio de perfil
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()

                    // 2. Actualizar el perfil del usuario
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Log.d("AuthRepository", "Nombre de usuario ($username) establecido exitosamente.")
                                onResult(true, null)
                            } else {
                                Log.e("AuthRepository", "Error al establecer display name: ${updateTask.exception?.message}")
                                // Aunque falle la actualización del nombre, el usuario se creó.
                                onResult(true, null)
                            }
                        }
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    // Nota: Para el login, el display name ya debería existir si el registro fue exitoso.
    fun loginUser(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    // Modificamos getCurrentUserId para priorizar el displayName si está disponible
    fun getCurrentUserId(): String? {
        return auth.currentUser?.displayName ?: auth.currentUser?.uid
    }

    fun logout() {
        auth.signOut()
    }
}