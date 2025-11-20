package com.example.guessemogi.ui.chat



import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.guessemogi.viewmodel.chat.ChatViewModel

@Composable
fun ChatScreen(
    vm: ChatViewModel,
    roomId: String,
    username: String)
{
    LaunchedEffect(Unit) { vm.listen(roomId) }
    DisposableEffect(Unit) { onDispose { vm.stop(roomId) } }

    val msgs by remember { vm.messages }
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(msgs) { m ->
                Column(modifier = Modifier.padding(6.dp)) {
                    Text("${m.fromUid}: ${m.text}")
                    Text("${m.ts}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.weight(1f), label = { Text("Mensaje") })
            Spacer(Modifier.width(8.dp))
            Button(onClick = { if (text.isNotBlank()) { vm.send(roomId, username, text); text = "" } }) { Text("Enviar") }
        }
    }
}
