package com.example.emojiguess.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.example.emojiguess.ui.vm.GameViewModel

@Composable
fun GameScreen(gameId: String?, gameViewModel: GameViewModel = viewModel()) {
    if (gameId == null) {
        // Handle error: gameId is null
        Text("Error: Game not found")
        return
    }

    LaunchedEffect(gameId) {
        gameViewModel.listenForGameChanges(gameId)
    }

    val game by gameViewModel.game.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (game == null) {
            Text("Loading game...")
        } else {
            Text("Game ID: $gameId")
            Text("00:30")
            game?.players?.forEach { (_, player) ->
                Text("${player.username} - ${player.emoji}")
            }
            // TODO: Display chat messages
            Button(onClick = { /* TODO: Handle emoji selection */ }) {
                Text("Select Emoji")
            }
        }
    }
}
