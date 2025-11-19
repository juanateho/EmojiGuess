package com.example.emojiguess.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colores consistentes con LoginScreen
private val DeepViolet = Color(0xFF1A0029)
private val NeonGreen = Color(0xFF39FF14)
private val SoftPurple = Color(0xFFBB86FC)

@Composable
fun MenuScreen(
    onCreateGame: () -> Unit,
    onJoinGame: (String) -> Unit,
    errorMessage: String?,
    onClearError: () -> Unit,
    isLandscape: Boolean
) {
    var gameIdToJoin by remember { mutableStateOf("") }

    // Logic to determine if the text field should be highlighted as error
    // Only highlight if the error specifically relates to the Game ID (not found)
    val isIdError = errorMessage != null && errorMessage.contains("Game not found", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepViolet, Color(0xFF2D0046))
                )
            )
    ) {
        // Fondo Geométrico (Reutilizado o similar al Login)
        GeometricBackground()

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val width = maxWidth * 0.85f

            Column(
                modifier = Modifier
                    .width(width)
                    .background(
                        color = Color.Black.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "MAIN MENU",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Choose an option",
                    fontSize = 16.sp,
                    color = NeonGreen,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Opción 1: Crear Partida
                Button(
                    onClick = onCreateGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = DeepViolet
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Text(
                        text = "CREATE NEW GAME",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.weight(1f))
                    Text(
                        text = "OR",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // Opción 2: Unirse a Partida
                Text(
                    text = "Join Existing Game",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = gameIdToJoin,
                    onValueChange = { 
                        gameIdToJoin = it 
                        if (errorMessage != null) onClearError()
                    },
                    placeholder = { Text("Enter Game ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = isIdError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = SoftPurple,
                        focusedLabelColor = NeonGreen,
                        unfocusedLabelColor = SoftPurple.copy(alpha = 0.7f),
                        cursorColor = NeonGreen,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        errorBorderColor = Color.Red,
                        errorLabelColor = Color.Red
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = { if (gameIdToJoin.isNotBlank()) onJoinGame(gameIdToJoin) }
                    )
                )
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onJoinGame(gameIdToJoin) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = gameIdToJoin.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftPurple,
                        contentColor = DeepViolet,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = "JOIN GAME",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
