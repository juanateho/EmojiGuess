package com.example.emojiguess.view

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DeepViolet = Color(0xFF1A0029)
private val BrightViolet = Color(0xFF6200EE)
private val NeonGreen = Color(0xFF39FF14)
private val SoftPurple = Color(0xFFBB86FC)

@Composable
fun LoginScreen(
    onLogin: (String) -> Unit, 
    isLandscape: Boolean
) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context as? ComponentActivity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
        }
    }

    var username by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepViolet, Color(0xFF2D0046))
                )
            )
    ) {
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
                    text = "EMOJI\nGUESS",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    lineHeight = 48.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Enter the arena",
                    fontSize = 16.sp,
                    color = NeonGreen,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { if (it.length <= 12) username = it },
                    label = { Text("Nickname") },
                    placeholder = { Text("Ex: EmojiKing") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = SoftPurple,
                        focusedLabelColor = NeonGreen,
                        unfocusedLabelColor = SoftPurple.copy(alpha = 0.7f),
                        cursorColor = NeonGreen,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { if (username.isNotBlank()) onLogin(username) }
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onLogin(username) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = username.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = DeepViolet,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Text(
                        text = "NEXT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GeometricBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = (-80).dp, y = (-80).dp)
                .alpha(0.2f)
                .background(BrightViolet, CircleShape)
                .blur(40.dp)
        )

        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterEnd)
                .offset(x = 60.dp, y = (-50).dp)
                .alpha(0.15f)
                .background(SoftPurple, CircleShape) 
                .blur(50.dp)
        )

        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 100.dp)
                .alpha(0.1f)
                .background(DeepViolet, CircleShape)
                .blur(60.dp)
        )
        
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 100.dp, x = 100.dp)
                .size(20.dp)
                .alpha(0.4f)
                .background(BrightViolet, CircleShape)
        )
        
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-150).dp, x = (-80).dp)
                .size(15.dp)
                .alpha(0.3f)
                .background(Color.White, CircleShape)
        )
    }
}
