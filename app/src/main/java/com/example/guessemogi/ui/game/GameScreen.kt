package com.example.guessemogi.ui.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.guessemogi.data.model.ChatMessage
import com.example.guessemogi.data.model.Player
import com.example.guessemogi.viewmodel.game.GameViewModel
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import android.util.Log
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.sp
import com.example.guessemogi.R // Necesario para acceder a R.drawable



@Composable
fun BackgroundScreen(backgroundResId: Int, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgroundResId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        content()
    }
}


@Composable
fun GameScreen(
    vm: GameViewModel,
    roomId: String,
    username: String,
    emojis: List<String>,
    onExit: () -> Unit,
    onGameEnd: (winnerName: String) -> Unit
) {
    LaunchedEffect(Unit) { vm.startListening(roomId) }

    val room by vm.room
    val players by vm.players
    val messages by vm.messages

    var chatText by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>("¡Empieza el juego!") }

    var remainingTimeText by remember { mutableStateOf("00:00") }

    val isMyTurn = room?.turn == username
    val currentRound = room?.round ?: 0
    val me = players.find { it.uid == username }
    val iAmAlive = me?.status == "alive"
    val iHaveGuessed = me?.hasGuessed == true

    // Reset 'info' and 'selected' when the round changes
    LaunchedEffect(currentRound) {
        if (currentRound > 0) {
            info = "🔄 ¡Nueva ronda $currentRound! ¡A adivinar!"
            selected = null
        }
    }

    // Countdown and TIMEOUT
    LaunchedEffect(room?.timerEnd, room?.turn) {
        val timerEnd = room?.timerEnd ?: return@LaunchedEffect


        while (System.currentTimeMillis() < timerEnd) {
            val remainingMillis = timerEnd - System.currentTimeMillis()
            remainingTimeText = formatTime(remainingMillis)
            delay(1000L)
        }


        remainingTimeText = "00:00"


        if (room?.turn == username && iAmAlive && !iHaveGuessed) {
            info = "¡Tiempo agotado! No seleccionaste nada (Eliminado)."

            vm.checkTimerEndAndAdvance(username)
        }
    }


    // End of the round and winner
    LaunchedEffect(players, currentRound) {
        if (currentRound == 0L) return@LaunchedEffect

        val alivePlayers = players.filter { it.status == "alive" }

        if (alivePlayers.size == 1 && players.size > 1) {
            val winner = alivePlayers.first()
            info = "🏆 ${winner.name} ganó el juego!"
            delay(2000)
            onGameEnd(winner.name)
            return@LaunchedEffect
        }

        val allAliveHaveGuessed = alivePlayers.isNotEmpty() && alivePlayers.all { it.hasGuessed }

        if (allAliveHaveGuessed) {
            info = "Todos jugaron - Nueva ronda en 2 segundos..."
            delay(2000)
            vm.nextRound(roomId)
        }
    }


    BackgroundScreen(backgroundResId = R.drawable.polar) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            GameHeader(
                roomId = roomId,
                round = currentRound,
                remainingTimeText = remainingTimeText,
                onExit = onExit
            )

            Spacer(Modifier.height(20.dp))


            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(players.sortedBy { it.name }) { player ->
                    PlayerCard(player = player, isCurrentPlayer = player.uid == username)
                }
            }

            Spacer(Modifier.height(16.dp))


            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        text = info ?: "",
                        style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )


                    if (iAmAlive && isMyTurn && !iHaveGuessed) {
                        GuessingSection(
                            emojis = emojis,
                            selected = selected,
                            onSelected = { selected = it },
                            onSubmit = {
                                selected?.let { s ->
                                    vm.submitGuess(roomId, username, s) { alive ->
                                        info = if (alive) "✅ ¡Correcto! Sigues en el juego."
                                        else "❌ ¡Fallaste! Has sido eliminado."
                                        vm.advanceTurn(roomId, username)
                                    }
                                }
                            }
                        )
                    } else if (iAmAlive && iHaveGuessed) {
                        Text("✅ Ya jugaste esta ronda. Esperando a los demás...", style = MaterialTheme.typography.bodyLarge)
                    } else if (!iAmAlive) {
                        Text("❌ Has sido eliminado. Solo puedes chatear.", style = MaterialTheme.typography.bodyLarge)
                    } else {
                        Text("⏳ Esperando tu turno...", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))


            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                ChatSection(
                    messages = messages,
                    players = players,
                    chatText = chatText,
                    onChatTextChange = { chatText = it },
                    onSend = {
                        vm.sendMessage(roomId, username, chatText)
                        chatText = ""
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}


@Composable
fun GameHeader(roomId: String, round: Long, remainingTimeText: String, onExit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Game",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )

        Spacer(Modifier.height(16.dp))


        Card(
            modifier = Modifier.fillMaxWidth(0.7f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Text(
                text = remainingTimeText,
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(Modifier.height(16.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Ronda: $round",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Button(
                onClick = onExit,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
            ) {
                Text("Salir")
            }
        }
    }
}


@Composable
fun PlayerCard(player: Player, isCurrentPlayer: Boolean) {

    val backgroundColor = if (isCurrentPlayer)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    else
        Color.White.copy(alpha = 0.8f)

    val contentColor = if (isCurrentPlayer)
        MaterialTheme.colorScheme.onPrimary
    else
        Color.Black

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {


                val emojiShown = if (isCurrentPlayer) "" else (player.emojiAssigned ?: "❓")
                Text(
                    text = emojiShown,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(end = 12.dp)
                )


                Column {

                    Text(
                        text = if (isCurrentPlayer) "${player.name} (Tú)" else player.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )


                    val statusText = if (player.status == "alive") "Vivo" else "Eliminado 💀"
                    val statusColor = if (player.status == "alive")
                        Color(0xFF2E7D32) // Verde oscuro
                    else
                        Color.Red

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCurrentPlayer) contentColor.copy(alpha = 0.8f) else statusColor
                    )
                }
            }


            if (player.hasGuessed == true && player.status == "alive") {
                Text(
                    text = "✅ Listo",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if(isCurrentPlayer) contentColor else Color(0xFF2E7D32)
                )
            }
        }
    }
}



@Composable
fun GuessingSection(
    emojis: List<String>,
    selected: String?,
    onSelected: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text(
            "Tu turno. Selecciona tu adivinanza:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        EmojiSelector(emojis, selected, onSelected)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onSubmit,
            enabled = selected != null,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
            contentPadding = PaddingValues(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Seleccionar Emoji", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
fun EmojiSelector(
    emojis: List<String>,
    selected: String?,
    onSelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(emojis) { emoji ->
            Card(
                onClick = { onSelected(emoji) },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected == emoji) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(if (selected == emoji) 8.dp else 2.dp),
                modifier = Modifier.size(55.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        emoji,
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp)
                    )
                }
            }
        }
    }
}



@Composable
fun ChatSection(
    messages: List<ChatMessage>,
    players: List<Player>,
    chatText: String,
    onChatTextChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(12.dp)) {
        Text("Chat", style = MaterialTheme.typography.titleMedium, color = Color.Black)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Black.copy(alpha = 0.5f))


        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 8.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { msg ->
                val sender = players.find { it.uid == msg.fromUid }
                val senderName = sender?.name ?: msg.fromUid
                val isMe = sender?.uid == msg.fromUid


                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = if (isMe) 12.dp else 2.dp,
                            bottomEnd = if (isMe) 2.dp else 12.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe) MaterialTheme.colorScheme.tertiary else Color.Gray.copy(alpha = 0.4f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.widthIn(max = 250.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                text = senderName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(text = msg.text, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }


        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = chatText,
                onValueChange = onChatTextChange,
                modifier = Modifier.weight(1f),
                label = { Text("Escribe un mensaje...", color = Color.Black.copy(alpha = 0.7f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = Color.Black.copy(alpha = 0.5f),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = onSend, enabled = chatText.isNotBlank()) { Text("Enviar") }
        }
    }
}



private fun formatTime(millis: Long): String {
    if (millis <= 0) return "00:00"
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    return String.format("%02d:%02d", minutes, seconds)
}