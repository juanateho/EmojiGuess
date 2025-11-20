package com.example.emojiguess.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class GameRepository {

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val gamesRef = database.getReference("games")

    fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }

    suspend fun signInAnonymously(): String? {
        return try {
            val result = auth.signInAnonymously().await()
            result.user?.uid
        } catch (e: Exception) {
            null
        }
    }

    suspend fun checkGameExists(gameId: String): Boolean {
        return try {
            val snapshot = gamesRef.child(gameId).get().await()
            snapshot.exists()
        } catch (e: Exception) {
            false
        }
    }

    fun createGame(username: String): String {
        // Generar ID de 6 caracteres alfabéticos
        val gameId = generateGameId()
        val userId = auth.currentUser?.uid ?: return ""
        
        val player = Player(id = userId, name = username, isCurrentUser = true)
        // Guardamos el estado como String para evitar problemas de deserialización de Enums
        val gameData = GameData(
            status = GameStatus.LOBBY.name,
            players = mapOf(userId to player),
            hostId = userId // Asignar el creador como host
        )
        
        gamesRef.child(gameId).setValue(gameData)
        return gameId
    }

    private fun generateGameId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return (1..6)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }

    suspend fun joinGame(gameId: String, username: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val playersRef = gamesRef.child(gameId).child("players")

        return try {
            val snapshot = playersRef.get().await()
            if (snapshot.childrenCount >= 4) {
                return false // Sala llena
            }
            
            val player = Player(id = userId, name = username, isCurrentUser = true)
            playersRef.child(userId).setValue(player).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun observeGame(gameId: String): Flow<GameData?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val gameData = snapshot.getValue(GameData::class.java)
                    trySend(gameData)
                } catch (e: Exception) {
                    Log.e("GameRepository", "Error parsing game data", e)
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        val ref = gamesRef.child(gameId)
        ref.addValueEventListener(listener)
        
        awaitClose { ref.removeEventListener(listener) }
    }
    
    fun updateGameStatus(gameId: String, status: GameStatus) {
        gamesRef.child(gameId).child("status").setValue(status.name)
    }
    
    fun updateTimer(gameId: String, time: Long) {
        gamesRef.child(gameId).child("timeRemaining").setValue(time)
    }

    fun submitGuess(gameId: String, isCorrect: Boolean) {
         val userId = auth.currentUser?.uid ?: return
         val updates = mapOf<String, Any>(
             "hasGuessed" to true,
             "lastGuessCorrect" to isCorrect
         )
         gamesRef.child(gameId).child("players").child(userId).updateChildren(updates)
    }

    fun sendChatMessage(gameId: String, message: String, senderName: String) {
        val chatRef = gamesRef.child(gameId).child("chat")
        val msgId = chatRef.push().key ?: return
        val chatMessage = ChatMessage(sender = senderName, text = message, timestamp = System.currentTimeMillis())
        chatRef.child(msgId).setValue(chatMessage)
    }

    fun startGameLogic(gameId: String, currentPlayers: List<Player>) {
        val updatedPlayers = Emojis.assignEmojisToPlayers(currentPlayers)
        val updates = mapOf(
            "status" to GameStatus.PLAYING.name,
            "players" to updatedPlayers,
            "currentRound" to 1,
            "timeRemaining" to 60,
            "roundHistory" to emptyList<RoundInfo>()
        )
        gamesRef.child(gameId).updateChildren(updates)
    }

    fun resolveRoundAndStartNext(gameId: String, currentPlayers: List<Player>, currentRound: Int, currentHistory: List<RoundInfo>) {
        val roundInfo = RoundInfo(
            roundNumber = currentRound,
            playersSnapshot = currentPlayers.associateBy { it.id }
        )
        val newHistory = currentHistory + roundInfo

        val playersAfterRound = currentPlayers.map { player ->
            if (player.isAlive) {
                if (player.hasGuessed) {

                    if (player.lastGuessCorrect) {
                        player // Sobrevive
                    } else {
                        player.copy(isAlive = false)
                    }
                } else {

                    player.copy(isAlive = false)
                }
            } else {
                player
            }
        }

        val survivors = playersAfterRound.filter { it.isAlive }
        if (survivors.size <= 1) {

            val winnerId = survivors.firstOrNull()?.id
            val updates = mapOf<String, Any>(
                "status" to if (winnerId != null) GameStatus.VICTORY.name else GameStatus.GAME_OVER.name,
                "players" to playersAfterRound.associateBy { it.id },
                "winnerId" to (winnerId ?: ""),
                "roundHistory" to newHistory
            )
            gamesRef.child(gameId).updateChildren(updates)
        } else {

            val playersNextRoundMap = Emojis.assignEmojisToPlayers(playersAfterRound)
            
            val updates = mapOf(
                "players" to playersNextRoundMap,
                "currentRound" to currentRound + 1,
                "timeRemaining" to 60,
                "roundHistory" to newHistory
            )
            gamesRef.child(gameId).updateChildren(updates)
        }
    }
}

data class GameData(
    val status: String = "LOGIN", 
    val players: Map<String, Player> = emptyMap(),
    val currentRound: Int = 1,
    val timeRemaining: Long = 60, 
    val chat: Map<String, ChatMessage>? = null,
    val winnerId: String? = null,
    val hostId: String? = null,
    val roundHistory: List<RoundInfo> = emptyList()
)
