package com.example.emojiguess.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.emojiguess.model.GameStatus
import com.example.emojiguess.model.Player
import com.example.emojiguess.model.RoundInfo

@Composable
fun GameOverScreen(
    winner: Player?,
    currentPlayer: Player?,
    userEmoji: String?,
    status: GameStatus,
    roundHistory: List<RoundInfo>,
    players: List<Player>,
    onRestart: () -> Unit,
    isLandscape: Boolean
) {
    val isMyVictory = winner != null && winner.id == currentPlayer?.id
    val anySurvivors = players.any { it.isAlive }
    val isNoSurvivors = status == GameStatus.GAME_OVER && winner == null && !anySurvivors

    val themeColor: Color
    val title: String
    val message: String
    val backgroundGradient: List<Color>

    when {
        isMyVictory -> {
            themeColor = Color(0xFF39FF14)
            title = "You Won!"
            message = "You are the Emoji Master!"
            backgroundGradient = listOf(Color(0xFF0A290A), Color(0xFF000000))
        }
        isNoSurvivors -> {
            themeColor = Color(0xFFB00020)
            title = "No Survivors"
            message = "Everyone was eliminated!"
            backgroundGradient = listOf(Color(0xFF1A1A1A), Color(0xFF000000))
        }
        else -> {
            themeColor = Color.Red
            title = "Defeated"
            message = if (winner != null) "${winner.name} won the game!" else "Better luck next time!"
            backgroundGradient = listOf(Color(0xFF290A0A), Color(0xFF000000))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = backgroundGradient))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = Color.Black.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(24.dp)
                )
                .border(2.dp, themeColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = themeColor,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isMyVictory) "🏆" else if (isNoSurvivors) "💀" else "💔",
                fontSize = 60.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Text(
                text = message,
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            if (!isMyVictory && userEmoji != null) {
                 Text(
                    text = "Your Emoji was: $userEmoji",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            HorizontalDivider(color = themeColor.copy(alpha = 0.3f))
            
            Text(
                text = "Round History",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(roundHistory.sortedByDescending { it.roundNumber }) { round ->
                    RoundHistoryItem(round, themeColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = themeColor.copy(alpha = 0.8f)),
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp),
                 shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "MAIN MENU", 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun RoundHistoryItem(round: RoundInfo, themeColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2D2D3A), RoundedCornerShape(8.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Round ${round.roundNumber}",
            color = themeColor,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        round.playersSnapshot.values.forEach { player ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                     Text(
                        text = player.name,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    
                    if (player.lastGuessCorrect) {
                        Text(
                            text = "Guessed: ${player.lastGuessedEmoji} ✅",
                            color = Color.Green,
                            fontSize = 12.sp
                        )
                    } else if (player.hasGuessed) {
                        Text(
                            text = "Guessed: ${player.lastGuessedEmoji} ❌",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = "Time Out ⏱️",
                            color = Color(0xFFFF9800),
                            fontSize = 12.sp
                        )
                    }
                }
                
                Text(
                    text = "Was: ${player.assignedEmoji}",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
        }
    }
}
