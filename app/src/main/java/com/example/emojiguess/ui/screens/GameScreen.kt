package com.example.emojiguess.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.emojiguess.ui.vm.GameViewModel

@Composable
fun GameScreen(gameId: String?, gameViewModel: GameViewModel = viewModel()) {
    if (gameId == null) {
        Text("Error: Game not found")
        return
    }

    LaunchedEffect(gameId) {
        gameViewModel.listenForGameChanges(gameId)
    }

    val game by gameViewModel.game.collectAsState()
    val timer by gameViewModel.timer.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var selectedEmoji by remember { mutableStateOf("") }
    val context = LocalContext.current

    val player = game?.players?.get(userId)

    if (game?.gameState == "finished") {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val winner = game?.players?.values?.firstOrNull { !it.isEliminated }
            if (winner != null) {
                if (winner.uid == userId) {
                    Text("You Won!", fontSize = 48.sp, color = Color.White)
                } else {
                    Text("Game Over!", fontSize = 48.sp, color = Color.White)
                    Text("${winner.username} won!", fontSize = 24.sp, color = Color.White)
                }
            } else {
                Text("Game Over!", fontSize = 48.sp, color = Color.White)
            }
        }
        return
    }
    
    if (player?.isEliminated == true) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("You have been eliminated!", fontSize = 24.sp, color = Color.White)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Game", fontSize = 32.sp, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Text(String.format("00:%02d", timer), fontSize = 48.sp, color = Color.White)
        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(game?.players?.values?.toList() ?: emptyList()) { p ->
                if (p.uid != userId && !p.isEliminated) { // Don't show current player's emoji or eliminated players
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(p.emoji, fontSize = 24.sp)
                        }
                        Text(p.username, modifier = Modifier.padding(start = 16.dp), color = Color.White)
                    }
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 48.dp),
            modifier = Modifier.height(200.dp) // Adjust height as needed
        ) {
            items(gameViewModel.getEmojiList()) { emoji ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(if (selectedEmoji == emoji) Color.White else Color.Gray)
                        .clickable { selectedEmoji = emoji },
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 24.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { 
            if(selectedEmoji.isNotEmpty()) {
                gameViewModel.selectEmoji(gameId, selectedEmoji)
            } else {
                Toast.makeText(context, "Please select an emoji", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Select Emoji")
        }
    }
}