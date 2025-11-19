package com.example.emojiguess.data

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val uid: String = "",
    val username: String = "",
    val emoji: String = "",
    val isEliminated: Boolean = false
)