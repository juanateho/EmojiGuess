package com.example.emojiguess.model

// Firebase requires a no-argument constructor for data classes.
// We add default values to all fields to satisfy this.
data class Player(
    val id: String = "",
    val name: String = "",
    var assignedEmoji: String = "",
    var isAlive: Boolean = true,
    var hasGuessed: Boolean = false,
    var lastGuessCorrect: Boolean = false,
    var lastGuessedEmoji: String = "", // Campo faltante para guardar la elección del usuario
    // isCurrentUser is NOT stored in Firebase, it's calculated locally based on Auth ID
    var isCurrentUser: Boolean = false 
)

enum class GameStatus {
    LOGIN, MENU, LOBBY, PLAYING, GAME_OVER, VICTORY
}

data class RoundInfo(
    val roundNumber: Int = 0,
    val playersSnapshot: Map<String, Player> = emptyMap() // Guardamos el estado de los jugadores al final de la ronda
)

// UI State used by ViewModel (not stored directly in Firebase as a whole object)
data class GameUiState(
    val status: GameStatus = GameStatus.LOGIN,
    val players: List<Player> = emptyList(),
    val currentRound: Int = 1,
    val timeRemaining: Long = 30, 
    val winner: Player? = null,
    val userEmoji: String? = null,
    val availableEmojis: List<String> = Emojis.gameEmojis,
    val chatMessages: List<ChatMessage> = emptyList(),
    val gameId: String? = null, // Track which game we are in
    val localPlayerName: String = "", // Store name temporarily before joining/creating
    val errorMessage: String? = null, // Error message for UI (e.g., "Game not found")
    val isHost: Boolean = false, // Only the host can start the game
    val roundHistory: List<RoundInfo> = emptyList() // Historial de rondas para Game Over
)

object Emojis {
    val gameEmojis = listOf(
        "😀", "😂", "😎", "😍", "🤔", "🤐", "😴", "🤢", "🤠", "🥳",
        "👻", "👽", "🤖", "👾", "🎃", "🐶", "🐱", "🐭", "🐹", "🐰"
    )

    // Asigna un emoji aleatorio único a cada jugador vivo
    fun assignEmojisToPlayers(players: List<Player>): Map<String, Player> {
        val available = gameEmojis.shuffled()
        val updatedMap = mutableMapOf<String, Player>()
        
        var emojiIndex = 0
        players.forEach { player ->
            if (player.isAlive) {
                // Asignar nuevo emoji
                val newEmoji = if (emojiIndex < available.size) available[emojiIndex] else "😀"
                updatedMap[player.id] = player.copy(
                    assignedEmoji = newEmoji,
                    hasGuessed = false, // Resetear estado de adivinanza
                    lastGuessCorrect = false, // Resetear resultado anterior
                    lastGuessedEmoji = "" // Resetear elección
                )
                emojiIndex++
            } else {
                // Mantener estado de muerto
                updatedMap[player.id] = player
            }
        }
        return updatedMap
    }
}

object GameLogic {
    fun determineWinner(players: List<Player>): Player? {
        val survivors = players.filter { it.isAlive }
        return if (survivors.size == 1) survivors.first() else null
    }

    fun isGuessCorrect(assignedEmoji: String, guessedEmoji: String): Boolean {
        return assignedEmoji == guessedEmoji
    }
}
