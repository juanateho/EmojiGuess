package com.example.emojiguess.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emojiguess.model.ChatMessage
import com.example.emojiguess.model.GameStatus
import com.example.emojiguess.model.Player

@Composable
fun PlayingScreen(
    timeRemaining: Long,
    players: List<Player>,
    availableEmojis: List<String>,
    chatMessages: List<ChatMessage>,
    onGuess: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    isLandscape: Boolean,
    currentRound: Int
) {
    var showEmojiSelection by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = if (!isLandscape) 40.dp else 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
             Text(
                text = "Round: $currentRound",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Box(
                modifier = Modifier
                    .background(
                        if (timeRemaining <= 10) Color.Red.copy(alpha = 0.8f) else Color(0xFF6C36E2),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                val minutes = timeRemaining / 60
                val seconds = timeRemaining % 60
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLandscape) {
            Row(modifier = Modifier.weight(1f)) {
                PlayerList(players, modifier = Modifier.weight(1f))
                
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    ChatArea(
                        messages = chatMessages,
                        onSendMessage = onSendMessage,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GameControls(
                        onSelectEmoji = { showEmojiSelection = true },
                        canGuess = players.find { it.isCurrentUser }?.isAlive == true &&
                                   players.find { it.isCurrentUser }?.hasGuessed == false
                    )
                }
            }
        } else {
            PlayerList(players, modifier = Modifier.weight(0.55f))

            Spacer(modifier = Modifier.height(8.dp))

            ChatArea(
                messages = chatMessages,
                onSendMessage = onSendMessage,
                modifier = Modifier.weight(0.45f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            GameControls(
                onSelectEmoji = { showEmojiSelection = true },
                canGuess = players.find { it.isCurrentUser }?.isAlive == true &&
                           players.find { it.isCurrentUser }?.hasGuessed == false
            )
        }
    }

    if (showEmojiSelection) {
        EmojiSelectionDialog(
            emojis = availableEmojis,
            onEmojiSelected = {
                onGuess(it)
                showEmojiSelection = false
            },
            onDismiss = { showEmojiSelection = false }
        )
    }
}

@Composable
fun ChatArea(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMessage by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF2D2D3A).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = false
        ) {
            items(messages) { msg ->
                Text(
                    text = "${msg.sender}: ${msg.text}", 
                    color = Color.White, 
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = currentMessage,
                onValueChange = { currentMessage = it },
                placeholder = { Text("Chat...", color = Color.Gray) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color(0xFF6C36E2),
                    unfocusedBorderColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Button(
                onClick = { 
                    if (currentMessage.isNotBlank()) {
                        onSendMessage(currentMessage)
                        currentMessage = ""
                    }
                },
                modifier = Modifier.height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C36E2)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
fun GameControls(onSelectEmoji: () -> Unit, canGuess: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp)
    ) {
        Button(
            onClick = onSelectEmoji,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6C36E2),
                disabledContainerColor = Color(0xFF6C36E2).copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = canGuess,
             elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 2.dp
            )
        ) {
            Text(
                "🎯 Select Your Emoji", 
                fontSize = 20.sp, 
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmojiSelectionDialog(
    emojis: List<String>,
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .clickable(enabled = false) {},
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D3A))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Pick your emoji!", color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 50.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(emojis) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color(0xFF3D3D4D), RoundedCornerShape(8.dp))
                                .clickable { onEmojiSelected(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 28.sp)
                        }
                    }
                }
            }
        }
    }
}
