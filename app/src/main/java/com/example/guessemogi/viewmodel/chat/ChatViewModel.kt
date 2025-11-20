package com.example.guessemogi.viewmodel.chat


import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import com.example.guessemogi.data.model.ChatMessage
import com.example.guessemogi.data.repository.ChatRepository
import com.google.firebase.database.ValueEventListener

class ChatViewModel(

) : ViewModel() {
    private val chatRepo =  ChatRepository()
    val messages = mutableStateOf<List<ChatMessage>>(emptyList())
    private var chatListener: ValueEventListener? = null

    fun listen(roomId: String) {
        chatListener = chatRepo.listenChat(roomId) { msgs -> messages.value = msgs }
    }

    fun stop(roomId: String) {
        chatRepo.stopListening(roomId, chatListener)
        chatListener = null
    }

    fun send(roomId: String, sender: String, text: String) {
        val msg = ChatMessage(id = "", fromUid = sender, text = text, ts = System.currentTimeMillis())
        chatRepo.sendMessage(roomId, msg)
    }
}
