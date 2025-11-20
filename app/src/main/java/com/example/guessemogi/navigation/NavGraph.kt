package com.example.guessemogi.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.guessemogi.data.model.Constants
import com.example.guessemogi.ui.chat.ChatScreen
import com.example.guessemogi.ui.createRoom.CreateRoomScreen
import com.example.guessemogi.ui.game.GameScreen
import com.example.guessemogi.ui.home.HomeScreen
import com.example.guessemogi.ui.joinRoom.JoinRoomScreen
import com.example.guessemogi.ui.login.LoginScreen
import com.example.guessemogi.ui.register.RegisterScreen
import com.example.guessemogi.ui.result.ResultScreen
import com.example.guessemogi.ui.waitingRoom.WaitingRoomScreen
import com.example.guessemogi.viewmodel.authviewmodel.AuthViewModel
import com.example.guessemogi.viewmodel.game.GameViewModel
import com.example.guessemogi.viewmodel.home.HomeViewModel
import com.example.guessemogi.viewmodel.rooms.WaitingRoomViewModel
import com.example.guessemogi.viewmodel.chat.ChatViewModel
import com.example.guessemogi.viewmodel.rooms.CreateRoomViewModel
import com.example.guessemogi.viewmodel.rooms.JoinRoomViewModel

@Composable
fun NavGraph(navController: NavHostController) {

    val authViewModel: AuthViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Login.route
    ) {

        // LOGIN
        composable(NavRoutes.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onGoToRegister = { navController.navigate(NavRoutes.Register.route) },
                onLoginSuccess = {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // REGISTER
        composable(NavRoutes.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onGoToLogin = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Register.route) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // HOME
        composable(NavRoutes.Home.route) {
            // obtiene username/uid desde el AuthViewModel
            val username = authViewModel.getUid() ?: ""

            HomeScreen(
                vm = homeViewModel,
                onCreateRoom = { navController.navigate(NavRoutes.CreateRoom.route) },
                onJoinRoom = { code -> navController.navigate(NavRoutes.JoinRoom.route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // CREATE ROOM
        composable(NavRoutes.CreateRoom.route) {
            val createVm: CreateRoomViewModel = viewModel()
            val username = authViewModel.getUid() ?: ""

            CreateRoomScreen(
                vm = createVm,
                username = username,
                onRoomCreated = { roomId ->
                    navController.navigate(NavRoutes.Lobby.build(roomId))
                }
            )
        }

        // JOIN ROOM
        composable(NavRoutes.JoinRoom.route) {
            val joinVm: JoinRoomViewModel = viewModel()
            val username = authViewModel.getUid() ?: ""

            JoinRoomScreen(
                vm = joinVm,
                username = username,
                onJoined = { roomId ->
                    navController.navigate(NavRoutes.Lobby.build(roomId))
                }
            )
        }

        // LOBBY (WaitingRoomScreen)
        composable(NavRoutes.Lobby.route) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val waitingVm: WaitingRoomViewModel = viewModel()
            val username = authViewModel.getUid() ?: ""

            WaitingRoomScreen(
                vm = waitingVm,
                roomId = roomId,
                username = username,
                onStartGame = {
                    navController.navigate(NavRoutes.Game.build(roomId))
                }
            )
        }

        // GAME
        composable(NavRoutes.Game.route) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val gameVm: GameViewModel = viewModel()
            val createVm: CreateRoomViewModel = viewModel()   // 👈 AÑADIDO
            val username = authViewModel.getUid() ?: ""

            GameScreen(
                vm = gameVm,
                roomId = roomId,
                username = username,
                emojis = Constants.EMOJI_LIST.shuffled(),   // 👈 LISTA PASADA
                onExit = {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Home.route) { inclusive = true }
                    }
                },
                onGameEnd = { winnerName ->
                    // Navegar a la pantalla de resultados
                    navController.navigate("result/$winnerName")
                }
            )
        }

        // RESULT SCREEN
        composable(
            route = NavRoutes.Result.route,
            arguments = listOf(
                navArgument("winnerName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val winnerName = backStackEntry.arguments?.getString("winnerName") ?: "Desconocido"

            ResultScreen(
                winnerName = winnerName,
                onPlayAgain = {
                    // Volver a Home y limpiar la pila
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // CHAT
        composable(NavRoutes.Chat.route) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val chatVm: ChatViewModel = viewModel()
            val username = authViewModel.getUid() ?: ""


            ChatScreen(
                vm = chatVm,
                roomId = roomId,
                username = username
            )
        }
    }
}
