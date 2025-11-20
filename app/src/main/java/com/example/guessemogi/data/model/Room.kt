package com.example.guessemogi.data.model

data class Room(
    val id: String = "",
    val hostId: String = "",
    val status: String = "waiting",
    val round: Long = 0L,
    val turn: String = "",
    val timerEnd: Long = 0L,
    val players: Map<String, Player> = emptyMap(),
    val hasStarted: Boolean = false
)