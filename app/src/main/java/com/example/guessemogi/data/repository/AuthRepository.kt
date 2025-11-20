package com.example.guessemogi.data.repository


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import android.util.Log // Agregado para logging

class AuthRepository(

) {
    private val auth =  FirebaseAuth.getInstance()


    private fun getUsernameFromEmail(email: String): String {
        return email.substringBefore('@') //get username from email

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


                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()


                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Log.d("AuthRepository", "Nombre de usuario ($username) establecido exitosamente.")
                                onResult(true, null)
                            } else {
                                Log.e("AuthRepository", "Error al establecer display name: ${updateTask.exception?.message}")

                                onResult(true, null)
                            }
                        }
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }


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


    fun getCurrentUserId(): String? {
        return auth.currentUser?.displayName ?: auth.currentUser?.uid
    }

    fun logout() {
        auth.signOut()
    }
}