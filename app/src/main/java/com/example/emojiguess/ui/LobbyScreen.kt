package com.example.emojiguess.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emojiguess.model.Player

@Composable
fun LobbyScreen(
    players: List<Player>, 
    onStartGame: () -> Unit,
    gameId: String?,
    isLandscape: Boolean,
    isHost: Boolean // New parameter
) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = if (!isLandscape) 40.dp else 0.dp), // Space for camera
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Lobby",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .background(Color(0xFF6C36E2), RoundedCornerShape(16.dp))
                .padding(horizontal = 32.dp, vertical = 8.dp)
        )

        // Display Game ID
        if (gameId != null) {
            Row(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2D2D3A))
                    .clickable {
                        clipboardManager.setText(AnnotatedString(gameId))
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: $gameId",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Copy ID",
                    tint = Color(0xFFBB86FC),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (isLandscape) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    PlayerList(players)
                }
                Column(modifier = Modifier.weight(1f).padding(start = 16.dp), verticalArrangement = Arrangement.Center) {
                    // Only show button if user is host
                    if (isHost) {
                        Button(
                            onClick = onStartGame,
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C36E2))
                        ) {
                            Text("Start Game")
                        }
                    } else {
                        Text(
                            text = "Waiting for host to start...",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            PlayerList(players, modifier = Modifier.weight(1f))
            
            // Only show button if user is host
            if (isHost) {
                Button(
                    onClick = onStartGame,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C36E2))
                ) {
                    Text("Start Game")
                }
            } else {
                 Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Waiting for host to start...",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerList(players: List<Player>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(players) { player ->
            val showEmoji = !player.isCurrentUser
            PlayerCard(player, showEmoji = showEmoji)
        }
    }
}

@Composable
fun PlayerCard(player: Player, showEmoji: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D3A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (player.isAlive) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Transparent, CircleShape)
                        .border(2.dp, Color(0xFF4A90E2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                     Text(
                        text = if (showEmoji) player.assignedEmoji else "❓",
                        fontSize = 24.sp
                    )
                }
            } else {
                 Text(text = "💀", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = player.name + if (player.isCurrentUser) " (You)" else "",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Cambiado de ✅ a 🔒 para indicar que ya jugó pero no si acertó
            if(player.hasGuessed && player.isAlive) {
                Text("🔒", fontSize = 16.sp)
            }
        }
    }
}
