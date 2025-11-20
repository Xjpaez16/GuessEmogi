package com.example.guessemogi.viewmodel.rooms


import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import com.example.guessemogi.data.model.Player
import com.example.guessemogi.data.repository.RoomRepository
import kotlin.random.Random

class CreateRoomViewModel() : ViewModel() {
    private val roomRepo = RoomRepository()
    val generatedRoomId = mutableStateOf("")



    fun generateRoomId() {
        // ... (sin cambios)
        val code = (1..6).map { "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random() }.joinToString("")
        generatedRoomId.value = code
    }

    // MODIFICADO (Req 5): Simplificado
    fun createRoomWithHost(username: String) {
        val id = generatedRoomId.value.ifBlank {
            generateRoomId()
            generatedRoomId.value
        }

        val hostPlayer = Player(uid = username, name = username)

        // Llamamos solo a createRoom. Ya no se necesita assignEmojis.
        roomRepo.createRoom(id, hostPlayer) { success ->
            if (success) {
                println("Sala creada y emoji de host asignado correctamente.")
            } else {
                println("Error: No se pudo crear la sala.")
            }
        }
    }
}