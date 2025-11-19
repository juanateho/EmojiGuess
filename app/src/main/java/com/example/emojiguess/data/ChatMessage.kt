package com.example.emojiguess.data

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val senderId: String = "",
    val message: String = "",
    val timestamp: Long = 0
)