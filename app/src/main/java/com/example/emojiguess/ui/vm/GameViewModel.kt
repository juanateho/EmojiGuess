package com.example.emojiguess.ui.vm

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import com.example.emojiguess.data.Game
import com.example.emojiguess.data.Player
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class GameViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().getReference("games")
    private val auth = FirebaseAuth.getInstance()

    private val _game = MutableStateFlow<Game?>(null)
    val game: StateFlow<Game?> = _game

    private val _timer = MutableStateFlow(30)
    val timer: StateFlow<Int> = _timer

    private var countDownTimer: CountDownTimer? = null

    private val emojiList = listOf("😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇", "😉", "😍", "🥰", "😘", "😋", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩", "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣", "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠", "😡", "🤬", "🤯", "😳", "🥵", "🥶", "😱", "😨", "😰", "😥", "😓", "🤗", "🤔", "🤭", "🤫", "🤥", "😶", "😐", "😑", "😬", "🙄", "😯", "😦", "😧", "😮", "😲", "🥱", "😴", "🤤", "😪", "😵", "🤐", "🥴", "🤢", "🤮", "🤧", "😷", "🤒", "🤕", "🤑", "🤠", "😈", "👿", "👹", "👺", "🤡", "💩", "👻", "💀", "☠️", "👽", "👾", "🤖", "🎃", "😺", "😸", "😹", "😻", "😼", "😽", "🙀", "😿", "😾")

    fun getEmojiList(): List<String> = emojiList

    fun listenForGameChanges(gameId: String) {
        database.child(gameId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentGame = snapshot.getValue(Game::class.java)
                if (_game.value?.gameState != "in-progress" && currentGame?.gameState == "in-progress") {
                    startTimer(gameId)
                }
                _game.value = currentGame
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun createGame(onGameCreated: (String) -> Unit) {
        val gameId = generateGameId()
        val hostId = auth.currentUser?.uid ?: return
        val game = Game(hostId = hostId, players = mapOf(hostId to Player(uid = hostId, username = "Player 1")))

        database.child(gameId).setValue(game).addOnSuccessListener {
            onGameCreated(gameId)
        }
    }

    fun joinGame(gameId: String, onGameJoined: () -> Unit, onFailure: (String) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        database.child(gameId).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val game = dataSnapshot.getValue(Game::class.java)
                if (game != null) {
                    val player = Player(userId, "Player ${game.players.size + 1}")
                    val updatedPlayers = game.players.toMutableMap()
                    updatedPlayers[userId] = player
                    database.child(gameId).child("players").setValue(updatedPlayers)
                        .addOnSuccessListener { onGameJoined() }
                        .addOnFailureListener { onFailure("Failed to join game") }
                } else {
                    onFailure("Game not found")
                }
            } else {
                onFailure("Game not found")
            }
        }.addOnFailureListener {
            onFailure("Failed to join game")
        }
    }

    fun startGame(gameId: String) {
        database.child(gameId).get().addOnSuccessListener { dataSnapshot ->
            val game = dataSnapshot.getValue(Game::class.java)
            if (game != null) {
                startNewRound(gameId, game)
            }
        }
    }

    fun selectEmoji(gameId: String, selectedEmoji: String) {
        val userId = auth.currentUser?.uid ?: return
        database.child(gameId).get().addOnSuccessListener { dataSnapshot ->
            val game = dataSnapshot.getValue(Game::class.java)
            if (game != null) {
                val player = game.players[userId]
                if (player != null && player.emoji != selectedEmoji) {
                    database.child(gameId).child("players").child(userId).child("isEliminated").setValue(true)
                }
                // Mark player as having made a choice, e.g., by setting emoji to "guessed"
                database.child(gameId).child("players").child(userId).child("emoji").setValue("") // Clear emoji to show guess is made
            }
        }
    }


    private fun startTimer(gameId: String) {
        countDownTimer?.cancel()
        _timer.value = 30
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timer.value = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                _timer.value = 0
                endRound(gameId)
            }
        }.start()
    }

    private fun endRound(gameId: String) {
        database.child(gameId).get().addOnSuccessListener { dataSnapshot ->
            val game = dataSnapshot.getValue(Game::class.java)
            if (game != null) {
                val updatedPlayers = game.players.mapValues { (_, player) ->
                    if (player.emoji.isNotEmpty() && !player.isEliminated) {
                        player.copy(isEliminated = true) // Eliminate players who didn't guess
                    } else {
                        player
                    }
                }
                database.child(gameId).child("players").setValue(updatedPlayers).addOnSuccessListener {
                    startNewRound(gameId, game.copy(players = updatedPlayers))
                }
            }
        }
    }

    private fun startNewRound(gameId: String, game: Game) {
        val remainingPlayers = game.players.filter { !it.value.isEliminated }
        if (remainingPlayers.size <= 1) {
            database.child(gameId).child("gameState").setValue("finished")
            return
        }

        val shuffledEmojis = emojiList.shuffled()
        val playersWithNewEmojis = remainingPlayers.entries.mapIndexed { index, entry ->
            entry.key to entry.value.copy(emoji = shuffledEmojis[index % emojiList.size])
        }.toMap()

        val updatedPlayers = game.players.mapValues { (id, player) ->
            playersWithNewEmojis[id] ?: player.copy(isEliminated = true)
        }

        val updatedGame = game.copy(
            gameState = "in-progress",
            players = updatedPlayers,
            currentRound = game.currentRound + 1
        )
        database.child(gameId).setValue(updatedGame)
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }

    private fun generateGameId(): String {
        return (10000..99999).random().toString()
    }
}
