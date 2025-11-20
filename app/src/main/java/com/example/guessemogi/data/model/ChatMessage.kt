package com.example.guessemogi.data.model

data class ChatMessage(
    val id: String = "",
    val fromUid: String = "",
    val text: String = "",
    val ts: Long = 0L
)