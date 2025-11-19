package com.example.emojiguess.model

data class Player(
    val id: String = "",
    val name: String = "",
    var assignedEmoji: String = "",
    var isAlive: Boolean = true,
    var hasGuessed: Boolean = false,
    var lastGuessCorrect: Boolean = false,
    var lastGuessedEmoji: String = "",
    var isCurrentUser: Boolean = false 
)

enum class GameStatus {
    LOGIN, MENU, LOBBY, PLAYING, GAME_OVER, VICTORY
}

data class RoundInfo(
    val roundNumber: Int = 0,
    val playersSnapshot: Map<String, Player> = emptyMap()
)

data class GameUiState(
    val status: GameStatus = GameStatus.LOGIN,
    val players: List<Player> = emptyList(),
    val currentRound: Int = 1,
    val timeRemaining: Long = 30, 
    val winner: Player? = null,
    val userEmoji: String? = null,
    val availableEmojis: List<String> = Emojis.gameEmojis,
    val chatMessages: List<ChatMessage> = emptyList(),
    val gameId: String? = null,
    val localPlayerName: String = "",
    val errorMessage: String? = null,
    val isHost: Boolean = false,
    val roundHistory: List<RoundInfo> = emptyList()
)

object Emojis {
    val gameEmojis = listOf(
        "😀", "😂", "😎", "😍", "🤔", "🤐", "😴", "🤢", "🤠", "🥳",
        "👻", "👽", "🤖", "👾", "🎃", "🐶", "🐱", "🐭", "🐹", "🐰"
    )

    fun assignEmojisToPlayers(players: List<Player>): Map<String, Player> {
        val available = gameEmojis.shuffled()
        val updatedMap = mutableMapOf<String, Player>()
        
        var emojiIndex = 0
        players.forEach { player ->
            if (player.isAlive) {

                val newEmoji = if (emojiIndex < available.size) available[emojiIndex] else "😀"
                updatedMap[player.id] = player.copy(
                    assignedEmoji = newEmoji,
                    hasGuessed = false,
                    lastGuessCorrect = false,
                    lastGuessedEmoji = ""
                )
                emojiIndex++
            } else {
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
