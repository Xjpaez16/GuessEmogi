package com.example.guessemogi.viewmodel.rooms


import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import com.example.guessemogi.data.model.Player
import com.example.guessemogi.data.repository.PlayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JoinRoomViewModel(

) : ViewModel() {
    private val playerRepo= PlayerRepository()
    val roomCode = mutableStateOf("")
    val joining = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    fun onRoomCodeChange(v: String) { roomCode.value = v }

    fun join(roomId: String, username: String, onJoined: (Boolean) -> Unit) {
        joining.value = true
        error.value = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                playerRepo.joinRoom(
                    roomId,
                    Player(uid = username, name = username)
                ) { success ->
                    CoroutineScope(Dispatchers.Main).launch {
                        joining.value = false
                        if (success) {
                            onJoined(true)
                        } else {
                            error.value = "No se pudo unir"
                            onJoined(false)
                        }
                    }
                }

            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    joining.value = false
                    error.value = e.message
                    onJoined(false)
                }
            }
        }
    }

}
