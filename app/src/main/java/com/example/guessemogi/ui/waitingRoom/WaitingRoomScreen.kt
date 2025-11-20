package com.example.guessemogi.ui.waitingRoom

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.guessemogi.R
import com.example.guessemogi.ui.register.BackgroundScreen
import com.example.guessemogi.viewmodel.rooms.WaitingRoomViewModel

@Composable
fun WaitingRoomScreen(
    vm: WaitingRoomViewModel,
    roomId: String,
    username: String,
    onStartGame: () -> Unit
) {
    LaunchedEffect(Unit) { vm.listen(roomId) }

    val players by remember { vm.players }
    val room by remember { vm.roomState }

    LaunchedEffect(room?.hasStarted) {
        if (room?.hasStarted == true) {
            onStartGame()
        }
    }

    BackgroundScreen(backgroundResId = R.drawable.left) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                // Información de sala y host (más compacta y contrastante)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFC107).copy(alpha = 0.85f) // amarillo semi-transparente
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Sala: $roomId",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF0D47A1) // azul oscuro para contraste
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Host: ${room?.hostId ?: "-"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF0D47A1)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Lista de jugadores
                Text(
                    "Jugadores:",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFFFFC107)
                )
                Spacer(Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1976D2).copy(alpha = 0.3f) // azul claro transparente
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(min = 80.dp, max = 250.dp)
                            .padding(8.dp)
                    ) {
                        items(players) { p ->
                            Text(
                                "- ${p.name} ${if (p.status != "alive") "(eliminado)" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Botón o mensaje de espera
                // Botón de iniciar partida centrado
                if (room?.hostId == username) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            modifier = Modifier
                                .width(250.dp)
                                .height(45.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFC107)
                            ),
                            onClick = { vm.startGame(roomId); onStartGame() }
                        ) {
                            Text(
                                "Iniciar partida",
                                color = Color(0xFF0D47A1)
                            )
                        }
                    }
                } else {
                    Text(
                        "Esperando al host para iniciar...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFC107)
                    )
                }

            }
        }
    }
}


@Composable
fun BackgroundScreen(backgroundResId: Int, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgroundResId),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        content()
    }
}

