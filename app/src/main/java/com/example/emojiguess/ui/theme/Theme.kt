package com.example.emojiguess.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    background = DarkBackground,
    surface = TextFieldBackground,
    onPrimary = TextColor,
    onBackground = TextColor,
    onSurface = TextColor
)

@Composable
fun EmojiGuessTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}