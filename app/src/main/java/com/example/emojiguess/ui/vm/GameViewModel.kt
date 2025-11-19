package com.example.emojiguess.ui.vm

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

    fun listenForGameChanges(gameId: String) {
        database.child(gameId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _game.value = snapshot.getValue(Game::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun createGame(onGameCreated: (String) -> Unit) {
        val gameId = generateGameId()
        val hostId = auth.currentUser?.uid ?: return
        val game = Game(hostId = hostId, players = mapOf(hostId to Player(hostId, "Host")))

        database.child(gameId).setValue(game).addOnSuccessListener {
            onGameCreated(gameId)
        }
    }

    private fun generateGameId(): String {
        return (10000..99999).random().toString()
    }
}
