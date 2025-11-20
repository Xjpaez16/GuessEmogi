package com.example.guessemogi.navigation

sealed class NavRoutes(val route : String) {
    object Login : NavRoutes("login")
    object Register : NavRoutes("register")
    object Home : NavRoutes("home")
    object CreateRoom : NavRoutes("create_room")
    object JoinRoom : NavRoutes("join_room")
    object Lobby : NavRoutes("lobby/{roomId}") {
        fun build(roomId: String) = "lobby/$roomId"
    }
    object Game : NavRoutes("game/{roomId}") {
        fun build(roomId: String) = "game/$roomId"
    }
    object Chat : NavRoutes("chat/{roomId}") {
        fun build(roomId: String) = "chat/$roomId"
    }

    object Result : NavRoutes("result/{winnerName}") {
        fun build(winnerName: String) = "result/$winnerName"
    }
}
