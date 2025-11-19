package com.example.emojiguess.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.emojiguess.ui.vm.GameViewModel

@Composable
fun WaitingRoomScreen(gameId: String?, gameViewModel: GameViewModel = viewModel(), onGameStarted: () -> Unit) {
    if (gameId == null) {
        Text("Error: Game ID not found")
        return
    }

    LaunchedEffect(gameId) {
        gameViewModel.listenForGameChanges(gameId)
    }

    val game by gameViewModel.game.collectAsState()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(game?.gameState) {
        if (game?.gameState == "in-progress") {
            onGameStarted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Game Code: $gameId")
        Spacer(modifier = Modifier.height(16.dp))
        Text("Players:")
        LazyColumn {
            items(game?.players?.values?.toList() ?: emptyList()) { player ->
                Text(player.username)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        if (game?.hostId == userId) {
            Button(onClick = { gameViewModel.startGame(gameId) }) {
                Text("Start Game")
            }
        }
    }
}