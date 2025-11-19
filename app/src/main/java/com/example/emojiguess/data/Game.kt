package com.example.emojiguess.data

import kotlinx.serialization.Serializable

@Serializable
data class Game(
    val hostId: String = "",
    val players: Map<String, Player> = emptyMap(),
    val gameState: String = "lobby", // lobby, in-progress, finished
    val currentRound: Int = 0,
    val messages: List<ChatMessage> = emptyList()
)