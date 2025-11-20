package com.example.guessemogi.data.repository

import com.example.guessemogi.data.model.Constants
import com.example.guessemogi.data.model.Player
import com.google.firebase.database.*

class PlayerRepository {
    private val db = FirebaseDatabase.getInstance().reference
    private val roomsRef = db.child("rooms")


    fun joinRoom(roomId: String, player: Player, onComplete: (Boolean) -> Unit) {
        val roomRef = roomsRef.child(roomId)

        roomRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(data: MutableData): Transaction.Result {
                val roomMap = (data.value as? Map<*, *>)?.toMutableMap() as MutableMap<String, Any>


                if (roomMap["status"] != "waiting") {
                    return Transaction.abort()
                }

                val players = roomMap["players"] as? MutableMap<String, Any> ?: mutableMapOf()


                val emojis = Constants.EMOJI_LIST


                val aliveCount = players.values.count {
                    (it as? Map<*, *>)?.get("status") == "alive"
                }


                val assignedEmoji = emojis[aliveCount % emojis.size]


                val playerMap = mapOf(
                    "uid" to player.uid,
                    "name" to player.name,
                    "status" to "alive",
                    "emojiAssigned" to assignedEmoji
                )

                players[player.uid] = playerMap
                roomMap["players"] = players

                data.value = roomMap
                return Transaction.success(data)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                onComplete(error == null && committed)
            }
        })
    }


    fun listenPlayers(
        roomId: String,
        onPlayers: (Map<String, Player>) -> Unit
    ): ValueEventListener {

        val ref = roomsRef.child(roomId).child("players")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val players = snapshot.children
                    .mapNotNull { it.getValue(Player::class.java) }
                    .associateBy { it.uid }

                onPlayers(players)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addValueEventListener(listener)
        return listener
    }

    fun removePlayersListener(roomId: String, listener: ValueEventListener) {
        roomsRef.child(roomId)
            .child("players")
            .removeEventListener(listener)
    }


    fun submitGuess(
        roomId: String,
        guesserUid: String,
        targetEmoji: String,
        onResult: (Boolean) -> Unit
    ) {
        val playersRef = roomsRef.child(roomId).child("players")

        playersRef.runTransaction(object : Transaction.Handler {

            override fun doTransaction(mutableData: MutableData): Transaction.Result {

                val playersMap = mutableData.value as? Map<*, *> ?: return Transaction.success(mutableData)
                val newMap = playersMap.toMutableMap()


                val playerMap = playersMap[guesserUid] as? Map<*, *>
                val updatedPlayer = playerMap?.toMutableMap() ?: mutableMapOf()

                val assignedEmoji = playerMap?.get("emojiAssigned") as? String
                val correct = assignedEmoji == targetEmoji


                updatedPlayer["hasGuessed"] = true // 👈 AÑADIDO: Marcar como "jugado"

                if (!correct) {

                    updatedPlayer["status"] = "eliminated"
                }


                newMap[guesserUid] = updatedPlayer
                mutableData.value = newMap

                return Transaction.success(mutableData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {

                val players = currentData?.children
                    ?.mapNotNull { it.getValue(Player::class.java) }
                    ?: emptyList()

                val player = players.find { it.uid == guesserUid }


                onResult(player?.status == "alive")
            }
        })
    }
}
