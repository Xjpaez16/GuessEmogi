package com.example.guessemogi.ui.joinRoom

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
import com.example.guessemogi.viewmodel.rooms.JoinRoomViewModel

@Composable
fun JoinRoomScreen(
    vm: JoinRoomViewModel,
    username: String,
    onJoined: (String) -> Unit
) {
    val code by remember { vm.roomCode }
    val joining by remember { vm.joining }
    val error by remember { vm.error }

    BackgroundScreen(backgroundResId = R.drawable.bob) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Unirse a Sala", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = code,
                        onValueChange = vm::onRoomCodeChange,
                        label = { Text("Código de sala") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (code.isNotBlank()) vm.join(code, username) { ok ->
                                if (ok) onJoined(code)
                            }
                        }
                    ) {
                        Text(if (joining) "Uniendo..." else "Unirse")
                    }

                    error?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
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
