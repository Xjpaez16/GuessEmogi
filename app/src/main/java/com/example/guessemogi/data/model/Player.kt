package com.example.guessemogi.data.model

data class Player(
    val uid: String = "",
    val name: String = "",
    val status: String = "alive",
    val emojiAssigned: String? = null,
    val hasGuessed: Boolean = false
)
