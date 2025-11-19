package com.example.emojiguess.model

data class ChatMessage(
    val sender: String = "",
    val text: String = "",
    val timestamp: Long = 0
)
