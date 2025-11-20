package com.example.guessemogi.data.repository

import com.example.guessemogi.data.model.ChatMessage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatRepository(

) {
    private val db = FirebaseDatabase.getInstance().reference
    private val roomsRef = db.child("rooms")

    fun sendMessage(roomId: String, msg: ChatMessage) {
        val msgRef = roomsRef.child(roomId).child("chat").push()
        msgRef.setValue(msg.copy(id = msgRef.key ?: ""))
    }

    fun listenChat(roomId: String, onMessages: (List<ChatMessage>) -> Unit): ValueEventListener {
        val chatRef = roomsRef.child(roomId).child("chat")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val msgs = snapshot.children.mapNotNull { it.getValue(ChatMessage::class.java) }
                onMessages(msgs)
            }
            override fun onCancelled(error: DatabaseError) {}
        }

        chatRef.addValueEventListener(listener)
        return listener
    }
    fun stopListening(roomId: String, listener: ValueEventListener?) {

        listener?.let {

            db.child(roomId).child("chat").removeEventListener(it)
        }
    }
}