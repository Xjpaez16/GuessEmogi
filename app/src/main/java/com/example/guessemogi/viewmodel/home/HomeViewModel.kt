package com.example.guessemogi.viewmodel.home



import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import com.example.guessemogi.data.repository.AuthRepository

class HomeViewModel(

) : ViewModel() {
    private val authRepo= AuthRepository()
    val roomCode = mutableStateOf("")

    fun onRoomCodeChange(value: String) { roomCode.value = value }

    fun getCurrentUserId(): String? = authRepo.getCurrentUserId()
}
