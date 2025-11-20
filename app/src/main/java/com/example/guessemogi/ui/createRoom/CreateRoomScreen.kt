package com.example.guessemogi.ui.createRoom

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import com.example.guessemogi.viewmodel.rooms.CreateRoomViewModel

@Composable
fun CreateRoomScreen(
    vm: CreateRoomViewModel,
    username: String,
    onRoomCreated: (String) -> Unit
) {
    val code by remember { vm.generatedRoomId }

    LaunchedEffect(Unit) { if (code.isEmpty()) vm.generateRoomId() }

    BackgroundScreen(backgroundResId = R.drawable.minion) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter // alineamos arriba
        ) {
            // Card amarilla sobre la parte superior de la imagen (minion)
            Card(
                modifier = Modifier
                    .padding(top = 60.dp)
                    .width(280.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEB3B).copy(alpha = 0.7f) // amarillo con 70% de opacidad
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Crear Sala",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Código de sala:", color = Color.Black)
                    Spacer(Modifier.height(4.dp))
                    Text(code, style = MaterialTheme.typography.headlineSmall, color = Color.Black)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            vm.createRoomWithHost(username)
                            onRoomCreated(code)
                        }
                    ) {
                        Text("Crear y entrar a sala")
                    }
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
