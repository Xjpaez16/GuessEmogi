package com.example.guessemogi.viewmodel.game

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import com.example.guessemogi.data.model.ChatMessage // AÑADIDO
import com.example.guessemogi.data.model.Constants
import com.example.guessemogi.data.model.Player
import com.example.guessemogi.data.model.Room
import com.example.guessemogi.data.repository.ChatRepository // AÑADIDO
import com.example.guessemogi.data.repository.PlayerRepository
import com.example.guessemogi.data.repository.RoomRepository
import com.google.firebase.database.ValueEventListener
import kotlin.math.max

class GameViewModel : ViewModel() {

    private val roomRepo = RoomRepository()
    private val playerRepo = PlayerRepository()
    private val chatRepo = ChatRepository()

    val room = mutableStateOf<Room?>(null)
    val players = mutableStateOf<List<Player>>(emptyList())
    val remainingTime = mutableStateOf<Long>(0L)
    val messages = mutableStateOf<List<ChatMessage>>(emptyList())

    private var roomListener: ValueEventListener? = null
    private var playersListener: ValueEventListener? = null
    private var chatListener: ValueEventListener? = null
    private var currentRoomId: String = ""

    fun startListening(roomId: String) {
        Log.d("GameViewModel", "🎧 Iniciando listeners para sala: $roomId")

        // Listening to Room
        roomListener = roomRepo.listenRoom(roomId) { r ->
            Log.d("GameViewModel", "📦 Room actualizado: round=${r?.round}, turn=${r?.turn}")
            room.value = r
            r?.timerEnd?.let {
                remainingTime.value = max(0L, it - System.currentTimeMillis())
            }
        }

        // Listening to Players
        playersListener = playerRepo.listenPlayers(roomId) { map ->
            Log.d("GameViewModel", "👥 Players actualizados (${map.size}):")
            map.forEach { (uid, player) ->
                Log.d("GameViewModel", "  - $uid: emoji=${player.emojiAssigned}, status=${player.status}")
            }
            players.value = map.values.toList()
        }

        //Listener of Chat
        chatListener = chatRepo.listenChat(roomId) { msgs ->
            messages.value = msgs
        }
    }

    fun checkTimerEndAndAdvance(guesserUid: String) {
        val currentRoom = room.value ?: return

        // We only check if it is the current player's turn.
        if (currentRoom.turn == guesserUid) {
            val now = System.currentTimeMillis()

            // We check if the time has ended
            if (currentRoom.timerEnd != null && now >= currentRoom.timerEnd!!) {
                Log.w("GameViewModel", "🚨 Tiempo expirado para $guesserUid.")

                val me = players.value.find { it.uid == guesserUid }

                // CASE 1: The player is alive and has NOT guessed anything yet.
                // ACTION: We force them to fail (Lost Round).
                if (me?.status == "alive" && me.hasGuessed == false) {
                    Log.d("GameViewModel", "⏳ El jugador no respondió a tiempo. Enviando fallo forzado.")

                    // We deliberately send an incorrect response (“TIMEOUT”).
                    // This will cause the PlayerRepository to mark it as deleted.
                    submitGuess(currentRoom.id, guesserUid, "TIMEOUT_FAILURE") { _ ->
                        // Once the fault has been marked, we move forward with the turn.
                        advanceTurn(currentRoom.id, guesserUid)
                    }
                }
                // CASE 2: The player already guessed correctly or was already eliminated, but the turn did not change.
                // ACTION: We simply advance the turn so as not to stall the game.
                else {
                    advanceTurn(currentRoom.id, guesserUid)
                }
            }
        }
    }

    //Send Message
    fun sendMessage(roomId: String, sender: String, text: String) {
        if (text.isBlank()) return
        val msg = ChatMessage(fromUid = sender, text = text, ts = System.currentTimeMillis())
        chatRepo.sendMessage(roomId, msg)
    }

    fun submitGuess(roomId: String, username: String, guessed: String, onResult: (Boolean) -> Unit) {
        Log.d("GameViewModel", "🎯 Adivinanza: $username -> $guessed")
        playerRepo.submitGuess(roomId, username, guessed) { alive ->
            Log.d("GameViewModel", "✅ Resultado: alive=$alive")
            onResult(alive)
        }
    }

    fun advanceTurn(roomId: String, uid: String) {
        Log.d("GameViewModel", "Avanzando turno desde: $uid")
        roomRepo.advanceTurn(roomId, uid)
    }

    fun nextRound(roomId: String) {
        Log.d("GameViewModel", "Llamando nextRound() para sala: $roomId")

        val emojis = Constants.EMOJI_LIST.shuffled()

        roomRepo.advanceRound(roomId, emojis) { ok ->
            Log.d("GameViewModel", "advanceRound completado: success=$ok")
            if (!ok) {
                Log.e("GameViewModel", "ERROR: No se pudo avanzar la ronda")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("GameViewModel", "Limpiando listeners")
        val roomId = room.value?.id
        if (roomId != null) {
            roomListener?.let { roomRepo.removeRoomListener(roomId, it) }
            playersListener?.let { playerRepo.removePlayersListener(roomId, it) }
            chatListener?.let { chatRepo.stopListening(roomId, it) }
        }
    }
}