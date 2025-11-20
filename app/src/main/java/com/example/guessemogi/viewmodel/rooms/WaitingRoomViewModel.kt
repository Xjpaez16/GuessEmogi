package com.example.guessemogi.viewmodel.rooms

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import com.example.guessemogi.data.model.Player
import com.example.guessemogi.data.repository.RoomRepository
import com.example.guessemogi.data.repository.PlayerRepository
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class WaitingRoomViewModel(

) : ViewModel() {
    private val roomRepo= RoomRepository()
    private val playerRepo=PlayerRepository()
    val players = mutableStateOf<List<Player>>(emptyList())
    val roomState = mutableStateOf<com.example.guessemogi.data.model.Room?>(null)

    private var playersListener: ValueEventListener? = null
    private var roomListener: ValueEventListener? = null

    fun listen(roomId: String) {

        // LISTENER DE JUGADORES
        playersListener = playerRepo.listenPlayers(roomId) { map ->
            players.value = map.values.toList()
        }

        // LISTENER DE ROOM
        roomListener = roomRepo.listenRoom(roomId) { room ->
            roomState.value = room
        }
    }

    fun stopListening(roomId: String) {
        playersListener?.let {
            playerRepo.removePlayersListener(roomId, it)
        }
        roomListener?.let {
            roomRepo.removeRoomListener(roomId, it)
        }
    }

    fun startGame(roomId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("rooms/$roomId")

        val updates = mapOf(
            "status" to "playing",
            "hasStarted" to true,
            "round" to 1,
            "timerEnd" to (System.currentTimeMillis() + 90000)
        )

        ref.updateChildren(updates)
    }

}
