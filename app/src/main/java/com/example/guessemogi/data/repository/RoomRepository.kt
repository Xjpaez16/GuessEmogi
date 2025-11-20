package com.example.guessemogi.data.repository

import android.util.Log
import com.example.guessemogi.data.model.Constants
import com.example.guessemogi.data.model.Player
import com.example.guessemogi.data.model.Room
import com.google.firebase.database.*

class RoomRepository {

    private val db = FirebaseDatabase.getInstance().reference
    private val roomsRef = db.child("rooms")

    // ============================================================
    // CREAR SALA (MODIFICADO - Req 5)
    // ============================================================
    fun createRoom(roomId: String, host: Player, onComplete: (Boolean) -> Unit) {

        // AÑADIDO: Asignar el primer emoji al host
        val emojis =  Constants.EMOJI_LIST.shuffled()
        val hostEmoji = emojis.first()

        val hostMap = mapOf(
            "uid" to host.uid,
            "name" to host.name,
            "status" to "alive",
            "emojiAssigned" to hostEmoji // MODIFICADO (ya no es null)
        )

        val roomData = mapOf(
            "id" to roomId,
            "hostId" to host.uid,
            "status" to "waiting",
            "round" to 0,
            "turn" to host.uid,
            "hasStarted" to false,
            "timerEnd" to 0L, // MODIFICADO: No iniciar timer hasta que empiece
            "players" to mapOf(host.uid to hostMap)
        )

        // ... (el resto de la función sigue igual)
        roomsRef.child(roomId)
            .setValue(roomData)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
            .addOnFailureListener {
                Log.e("RoomRepository", "createRoom failed", it)
                onComplete(false)
            }
    }

    // ============================================================
    // LISTENER (Sin cambios)
    // ============================================================
    fun listenRoom(roomId: String, onUpdate: (Room?) -> Unit): ValueEventListener {
        // ... (sin cambios)
        val ref = roomsRef.child(roomId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    onUpdate(snapshot.getValue(Room::class.java))
                } catch (e: Exception) {
                    Log.e("RoomRepository", "Error parsing Room", e)
                    onUpdate(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RoomRepository", "listenRoom cancelled: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        return listener
    }

    fun removeRoomListener(roomId: String, listener: ValueEventListener) {
        // ... (sin cambios)
        roomsRef.child(roomId).removeEventListener(listener)
    }

    // ============================================================
    // ASIGNAR EMOJIS A LOS VIVOS (ELIMINADO)
    // ============================================================
    // ELIMINADO: Ya no necesitamos esta función.
    // createRoom asigna el primer emoji.
    // joinRoom (en PlayerRepository) asigna los siguientes.
    // advanceRound tiene su propia lógica de re-asignación.
    /*
    fun assignEmojis(roomId: String, emojis: List<String>, onComplete: (Boolean) -> Unit) {
        ...
    }
    */

    // ============================================================
    // AVANZAR TURNO (Sin cambios)
    // ============================================================
    fun advanceTurn(roomId: String, currentUid: String) {
        val ref = roomsRef.child(roomId)

        ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(data: MutableData): Transaction.Result {
                val roomMap = data.value as? Map<*, *> ?: return Transaction.abort()

                val players = roomMap["players"] as? Map<String, Any> ?: return Transaction.abort()

                // 1. Obtener jugadores vivos ordenados (importante para la secuencia)
                val alivePlayers = players.values
                    .filter { (it as Map<*, *>)["status"] == "alive" }
                    .map { (it as Map<*, *>)["uid"].toString() }
                    .sorted()

                // Si queda 1 o ninguno, no avanzamos turno (la lógica de fin de juego lo manejará)
                if (alivePlayers.isEmpty() || alivePlayers.size == 1) return Transaction.success(data)

                // 2. Calcular el siguiente jugador
                val index = alivePlayers.indexOf(currentUid)
                val nextIndex = (index + 1) % alivePlayers.size
                val nextUid = alivePlayers[nextIndex]

                val updated = roomMap.toMutableMap()

                // 3. Cambiar el turno
                updated["turn"] = nextUid

                // 🔥 4. NUEVO: Resetear el temporizador a 60 segundos para el nuevo turno
                updated["timerEnd"] = System.currentTimeMillis() + 90000

                data.value = updated
                return Transaction.success(data)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (error != null) Log.e("RoomRepository", "Error en advanceTurn: ${error.message}")
            }
        })
    }


    // ============================================================
    // AVANZAR RONDA (MODIFICADO - Req 2)
    // ============================================================
    fun advanceRound(roomId: String, emojis: List<String>, onComplete: (Boolean) -> Unit) {
        roomsRef.child(roomId).runTransaction(object : Transaction.Handler {

            override fun doTransaction(data: MutableData): Transaction.Result {

                val roomMap = data.value as? MutableMap<*, *> ?: return Transaction.success(data)
                val players = roomMap["players"] as? Map<*, *> ?: return Transaction.success(data)

                // ... (Obtener jugadores vivos - sin cambios)
                val aliveList = players.filter { (_, v) ->
                    (v as Map<*, *>)["status"] == "alive"
                }.keys.toList()

                if (aliveList.isEmpty()) return Transaction.success(data)

                // ... (Mezclar emojis - sin cambios)
                val shuffledEmojis = emojis.shuffled()

                // Crear mapa mutable de jugadores
                val mutablePlayers = players.mapValues { (uid, p) ->
                    val pm = (p as Map<*, *>).toMutableMap()

                    if (uid in aliveList) {
                        // Reasignar emoji
                        val newEmoji = shuffledEmojis[aliveList.indexOf(uid) % shuffledEmojis.size]
                        pm["emojiAssigned"] = newEmoji

                        // 👈 AÑADIDO: Resetear el estado de "ha jugado"
                        pm["hasGuessed"] = false

                    } else {
                        // Asegurarse de que los jugadores no vivos también lo tengan en false
                        pm["hasGuessed"] = false
                    }
                    pm
                }.toMutableMap()

                // ... (Subir ronda - sin cambios)
                val currentRound = when (val r = roomMap["round"]) {
                    is Long -> r
                    is Int -> r.toLong()
                    else -> 0L
                }
                val newRound = currentRound + 1

                // Actualizar sala
                val newMap = roomMap.toMutableMap()
                newMap["round"] = newRound
                newMap["players"] = mutablePlayers
                newMap["timerEnd"] = System.currentTimeMillis() + 90000
                newMap["turn"] = aliveList.first() // Resetear turno
                newMap["hasStarted"] = true
                newMap["status"] = "playing"

                data.value = newMap
                return Transaction.success(data)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                Log.d("RoomRepository", "advanceRound complete: success=${error == null && committed}")
                onComplete(error == null && committed)
            }
        })
    }
}