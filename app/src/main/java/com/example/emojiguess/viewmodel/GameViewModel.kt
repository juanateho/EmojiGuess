package com.example.emojiguess.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emojiguess.model.ChatMessage
import com.example.emojiguess.model.Emojis
import com.example.emojiguess.model.GameData
import com.example.emojiguess.model.GameLogic
import com.example.emojiguess.model.GameRepository
import com.example.emojiguess.model.GameStatus
import com.example.emojiguess.model.GameUiState
import com.example.emojiguess.model.Player
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    private val repository = GameRepository()
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var currentGameId: String? = null
    private var currentUserId: String? = null

    init {
        initializeAuth()
    }

    private fun initializeAuth() {
        viewModelScope.launch {
            val existingUid = repository.getCurrentUserUid()
            if (existingUid != null) {
                currentUserId = existingUid
                Log.d("GameViewModel", "Already signed in with UID: $existingUid")
            } else {
                val uid = repository.signInAnonymously()
                if (uid != null) {
                    currentUserId = uid
                    Log.d("GameViewModel", "Signed in anonymously with UID: $uid")
                } else {
                    Log.e("GameViewModel", "Failed to sign in anonymously during init")
                }
            }
        }
    }

    fun login(username: String) {
        if (username.isBlank()) return
        
        viewModelScope.launch {
            if (currentUserId == null) {
                 currentUserId = repository.getCurrentUserUid()
            }
            if (currentUserId == null) {
                currentUserId = repository.signInAnonymously()
            }
            
            _uiState.update { 
                it.copy(
                    localPlayerName = username,
                    status = GameStatus.MENU
                ) 
            }
        }
    }

    fun createGame() {
        val username = _uiState.value.localPlayerName
        if (username.isBlank()) {
             _uiState.update { it.copy(errorMessage = "Error: Username missing. Restart app.") }
             return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }

            if (currentUserId == null) {
                 currentUserId = repository.getCurrentUserUid() ?: repository.signInAnonymously()
            }

            if (currentUserId == null) {
                 _uiState.update { it.copy(errorMessage = "Auth failed. Check internet connection.") }
                 delay(1000)
                 currentUserId = repository.signInAnonymously()
                 if (currentUserId == null) return@launch
            }

            Log.d("GameViewModel", "Creating game for user: $username")
            
            val gameId = try {
                repository.createGame(username)
            } catch (e: Exception) {
                Log.e("GameViewModel", "Exception creating game", e)
                ""
            }
            
            if (gameId.isNotBlank()) {
                 Log.d("GameViewModel", "Game created with ID: $gameId")
                 currentGameId = gameId
                 _uiState.update { it.copy(status = GameStatus.LOBBY) }
                 observeGame(gameId)
            } else {
                Log.e("GameViewModel", "Failed to create game")
                _uiState.update { it.copy(errorMessage = "Failed to create game. Try again.") }
            }
        }
    }

    fun joinGame(inputGameId: String) {
        val username = _uiState.value.localPlayerName
        if (username.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Error: Username missing.") }
            return
        }
        if (inputGameId.isBlank()) return
        
        val gameId = inputGameId.uppercase().trim()
        
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }

            if (currentUserId == null) {
                 currentUserId = repository.getCurrentUserUid() ?: repository.signInAnonymously()
            }
            
            if (currentUserId == null) {
                 _uiState.update { it.copy(errorMessage = "Auth failed. Check internet connection.") }
                 return@launch
            }

            if (repository.checkGameExists(gameId)) {
                val success = repository.joinGame(gameId, username)
                if (success) {
                    currentGameId = gameId
                    _uiState.update { it.copy(status = GameStatus.LOBBY) }
                    observeGame(gameId)
                } else {
                    _uiState.update { it.copy(errorMessage = "Lobby is full (Max 4 players)") }
                }
            } else {
                 _uiState.update { it.copy(errorMessage = "Game not found with ID: $gameId") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun observeGame(gameId: String) {
        Log.d("GameViewModel", "Observing game: $gameId")
        viewModelScope.launch {
            repository.observeGame(gameId).collectLatest { gameData ->
                if (gameData == null) {
                    Log.d("GameViewModel", "Game data is null for gameId: $gameId")
                    return@collectLatest
                }
                
                val playersList = gameData.players.values.toList().map { 
                    it.copy(isCurrentUser = it.id == currentUserId)
                }
                
                val currentUserPlayer = playersList.find { it.isCurrentUser }
                val userEmoji = currentUserPlayer?.assignedEmoji
                
                val isHost = if (gameData.hostId != null) {
                    gameData.hostId == currentUserId
                } else {
                    false 
                }

                val statusEnum = try {
                    GameStatus.valueOf(gameData.status)
                } catch (e: Exception) {
                    GameStatus.LOBBY
                }

                if (isHost && statusEnum == GameStatus.PLAYING) {
                    val survivors = playersList.filter { it.isAlive }
                    val allGuessed = survivors.isNotEmpty() && survivors.all { it.hasGuessed }
                    
                    if (allGuessed) {
                         timerJob?.cancel()
                         repository.resolveRoundAndStartNext(gameId, playersList, gameData.currentRound, gameData.roundHistory)
                    }
                }

                if (isHost && statusEnum == GameStatus.PLAYING && timerJob?.isActive != true) {
                   startRound()
                }
                if (statusEnum != GameStatus.PLAYING) {
                    timerJob?.cancel()
                }

                val finalStatus = if (statusEnum == GameStatus.PLAYING && currentUserPlayer?.isAlive == false) {
                    GameStatus.GAME_OVER
                } else {
                    statusEnum
                }

                _uiState.update { 
                    it.copy(
                        status = finalStatus,
                        players = playersList,
                        timeRemaining = gameData.timeRemaining,
                        chatMessages = gameData.chat?.values?.toList() ?: emptyList(),
                        userEmoji = userEmoji,
                        gameId = gameId,
                        winner = if (gameData.winnerId != null) gameData.players[gameData.winnerId] else null,
                        isHost = isHost,
                        currentRound = gameData.currentRound,
                        roundHistory = gameData.roundHistory
                    )
                }
            }
        }
    }

    fun startGame() {
        val gameId = currentGameId ?: return
        val currentState = _uiState.value
        if (!currentState.isHost) return 

        repository.startGameLogic(gameId, currentState.players)
        startRound()
    }

    private fun startRound() {
        val isHost = _uiState.value.isHost
        if (!isHost) return

        resetTimer()
    }

    private fun resetTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var time = 60L 
            val gameId = currentGameId ?: return@launch
            
            while (time > 0) {
                repository.updateTimer(gameId, time)
                delay(1000)
                time--
            }
            
            repository.updateTimer(gameId, 0)
            
            val currentState = _uiState.value
            repository.resolveRoundAndStartNext(gameId, currentState.players, currentState.currentRound, currentState.roundHistory)
            
            delay(2000)
        }
    }

    fun submitGuess(emoji: String) {
        val currentState = _uiState.value
        val currentUser = currentState.players.find { it.isCurrentUser } ?: return
        val userAssignedEmoji = currentState.userEmoji ?: return
        val gameId = currentGameId ?: return

        if (!currentUser.isAlive || currentUser.hasGuessed) return

        val isCorrect = GameLogic.isGuessCorrect(userAssignedEmoji, emoji)
        
        repository.submitGuess(gameId, isCorrect)
    }
    
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val gameId = currentGameId ?: return
        val currentUser = _uiState.value.players.find { it.isCurrentUser } ?: return
        
        repository.sendChatMessage(gameId, text, currentUser.name)
    }

    fun resetGame() {
        timerJob?.cancel()
        _uiState.update { 
            GameUiState(
                status = GameStatus.MENU,
                localPlayerName = it.localPlayerName
            ) 
        }
        currentGameId = null
    }

    fun navigateToLogin() {
        _uiState.update { it.copy(status = GameStatus.LOGIN) }
    }

    fun navigateToMenu() {
        _uiState.update { it.copy(status = GameStatus.MENU, errorMessage = null) }
        currentGameId = null
    }
}
