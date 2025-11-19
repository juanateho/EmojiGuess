package com.example.emojiguess

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.emojiguess.model.GameStatus
import com.example.emojiguess.ui.GameOverScreen
import com.example.emojiguess.ui.LobbyScreen
import com.example.emojiguess.ui.LoginScreen
import com.example.emojiguess.ui.MenuScreen
import com.example.emojiguess.ui.PlayingScreen
import com.example.emojiguess.ui.theme.EmojiGuessTheme
import com.example.emojiguess.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EmojiGuessTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EmojiGuessApp()
                }
            }
        }
    }
}

@Composable
fun EmojiGuessApp(
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E1E2C), Color(0xFF2A2A3E))
                )
            )
            .safeDrawingPadding()
    ) {
        when (uiState.status) {
            GameStatus.LOGIN -> LoginScreen(onLogin = { viewModel.login(it) }, isLandscape = isLandscape)
            GameStatus.MENU -> MenuScreen(
                onCreateGame = { viewModel.createGame() },
                onJoinGame = { viewModel.joinGame(it) },
                errorMessage = uiState.errorMessage,
                onClearError = { viewModel.clearError() },
                onBack = { viewModel.navigateToLogin() }, // Back to Login
                isLandscape = isLandscape
            )
            GameStatus.LOBBY -> LobbyScreen(
                players = uiState.players,
                onStartGame = { viewModel.startGame() },
                gameId = uiState.gameId,
                isLandscape = isLandscape,
                isHost = uiState.isHost,
                onBack = { viewModel.navigateToMenu() } // Back to Menu
            )
            GameStatus.PLAYING -> PlayingScreen(
                timeRemaining = uiState.timeRemaining,
                players = uiState.players,
                availableEmojis = uiState.availableEmojis,
                chatMessages = uiState.chatMessages,
                onGuess = { viewModel.submitGuess(it) },
                onSendMessage = { viewModel.sendChatMessage(it) },
                isLandscape = isLandscape,
                currentRound = uiState.currentRound // Added parameter
            )
            GameStatus.VICTORY, GameStatus.GAME_OVER -> {
                val currentPlayer = uiState.players.find { it.isCurrentUser }
                GameOverScreen(
                    winner = uiState.winner,
                    currentPlayer = currentPlayer,
                    userEmoji = uiState.userEmoji,
                    status = uiState.status,
                    roundHistory = uiState.roundHistory,
                    players = uiState.players,
                    onRestart = { viewModel.resetGame() },
                    isLandscape = isLandscape
                )
            }
        }
    }
}
