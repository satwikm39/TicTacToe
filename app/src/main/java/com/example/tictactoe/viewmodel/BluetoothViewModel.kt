package com.example.tictactoe.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tictactoe.domain.game.BluetoothController
import com.example.tictactoe.domain.game.BluetoothDeviceDomain
import com.example.tictactoe.domain.game.ConnectionResult
import com.example.tictactoe.domain.game.GameData
import com.example.tictactoe.domain.game.GameState
import com.example.tictactoe.domain.game.MiniGame
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothController
) : ViewModel() {
    private var localDev = bluetoothController.localDeviceName ?: ""
    var shouldNav = false
    var isDbUpdate = true
    private var isFirst = false
    val _state = MutableStateFlow(BluetoothUiState())
    val state = combine(
        bluetoothController.scannedDevices, bluetoothController.pairedDevices, _state
    ) { scannedDevices, pairedDevices, currentState ->
        currentState.copy(
            scannedDevices = scannedDevices,
            pairedDevices = pairedDevices,
            messages = if (currentState.isConnected) currentState.messages else emptyList(),
            gameState = currentState.gameState,
            metadata = currentState.metadata
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)


    private var deviceConnectionJob: Job? = null

    init {
        bluetoothController.setOnConnectionUpdated { newMetadata ->
            _state.update { currentState ->
                currentState.copy(metadata = newMetadata)
            }
        }

        bluetoothController.isConnected.onEach { isConnected ->
            _state.update { it.copy(isConnected = isConnected) }
        }.launchIn(viewModelScope)

        bluetoothController.errors.onEach { error ->
            _state.update {
                it.copy(errorMessage = error)
            }
        }.launchIn(viewModelScope)
    }

    fun connectToDevice(device: BluetoothDeviceDomain) {
        isDbUpdate = true
        _state.update { it.copy(isConnecting = true) }
        deviceConnectionJob = bluetoothController.connectToDevice(device).listen()
    }

    fun disconnectFromDevice() {
        isDbUpdate = true
        deviceConnectionJob?.cancel()
        bluetoothController.closeConnection()
//        _state.update {
//            it.copy(
//                isConnecting = false, isConnected = false
//            )
//        }
        shouldNav = false
        _state.update { currentState ->
            currentState.copy(
                isConnecting = false, isConnected = false, gameState = GameState(
                    board = listOf(
                        listOf(" ", " ", " "), listOf(" ", " ", " "), listOf(" ", " ", " ")
                    ),
                    turn = "",
                    winner = "",
                    draw = false,
                    connectionEstablished = false,
                    reset = false
                ), metadata = currentState.metadata.copy(
                    miniGame = MiniGame(player1Choice = "", player2Choice = "")
                )
            )
        }
    }

    fun resetBoard() {
        isDbUpdate = true
        viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(
                    gameState = currentState.gameState.copy(
                        board = listOf(
                            listOf(" ", " ", " "), listOf(" ", " ", " "), listOf(" ", " ", " ")
                        ), reset = true, winner = "", draw = false
                    )
                )
            }

            val updatedState = _state.value
            sendMessage(GameData(updatedState.gameState, updatedState.metadata))
        }
    }

    fun waitForIncomingConnections() {
        _state.update { it.copy(isConnecting = true) }
        deviceConnectionJob = bluetoothController.startBluetoothServer().listen()
    }

    fun sendMessage(gameData: GameData, localDev: String = "") {
        viewModelScope.launch {
            val bluetoothMessage = bluetoothController.trySendMessage(gameData, localDev)
            if (bluetoothMessage != null) {
                _state.update { currentState ->
                    currentState.copy(
                        gameState = gameData.gameState,
                        metadata = gameData.metadata,
                    )
                }
            }
        }
//        Log.d("BluetoothViewModel", "Sending message: $gameData")
    }


    fun startScan() {
        bluetoothController.startDiscovery()
    }

    fun stopScan() {
        bluetoothController.stopDiscovery()
    }

    private fun Flow<ConnectionResult>.listen(): Job {
        return onEach { result ->
            when (result) {
                ConnectionResult.ConnectionEstablished -> {
                    _state.update {
                        it.copy(
                            isConnected = true, isConnecting = false, errorMessage = null
                        )
                    }
                }

                is ConnectionResult.TransferSucceeded -> {
                    val receivedGameData = result.gameData
                    _state.update {
                        it.copy(
                            gameState = receivedGameData.gameState,
                            metadata = receivedGameData.metadata
                        )
                    }
                }

                is ConnectionResult.Error -> {
                    _state.update {
                        it.copy(
                            isConnected = false, isConnecting = false, errorMessage = result.message
                        )
                    }
                }
            }
        }.catch { throwable ->
            bluetoothController.closeConnection()
            _state.update {
                it.copy(
                    isConnected = false,
                    isConnecting = false,
                )
            }
        }.launchIn(viewModelScope)
    }


    override fun onCleared() {
        resetBoard()
        super.onCleared()
        bluetoothController.release()
    }

    fun setPlayerChoice(choice: String) {
        viewModelScope.launch {
            // Get the current miniGame state and gameState
            val currentMetadata = _state.value.metadata
            val miniGame = currentMetadata.miniGame
            val currentGameState = _state.value.gameState

            // Update the miniGame with the logic you provided
//            Log.d("BluetoothViewModel", "localDev: $localDev")
            val updatedMiniGame = when {
                choice == "Me" -> {
                    isFirst = true
                    miniGame.copy(
                        player1Choice = localDev, player2Choice = localDev
                    )
                }

                else -> {
                    miniGame
                }
            }

            _state.update { currentState ->
                currentState.copy(
                    metadata = currentMetadata.copy(
                        miniGame = updatedMiniGame
                    ), gameState = currentGameState.copy(
                        turn = "0"
                    )
                )
            }

            // Send updated state as GameData
            val updatedState = _state.value
            sendMessage(GameData(updatedState.gameState, updatedState.metadata))
        }
    }


    fun makeMove(row: Int, col: Int) {
        viewModelScope.launch {
            val gameState = _state.value.gameState
            if (gameState.board[row][col] == " " && gameState.winner.isEmpty() && isMoveAllowed(
                    gameState.turn.toInt()
                )
            ) {
                val currentPlayer = if (gameState.turn.toInt() % 2 == 0) "X" else "O"
                val newBoard = gameState.board.mapIndexed { r, list ->
                    if (r == row) list.mapIndexed { c, value -> if (c == col) currentPlayer else value }
                    else list
                }

                _state.update { currentState ->
                    currentState.copy(
                        gameState = currentState.gameState.copy(
                            board = newBoard,
                            turn = (currentState.gameState.turn.toInt() + 1).toString()  // Increment the turn
                        )
                    )
                }

                postMoveUpdate(newBoard)
            } else {
                Log.d(
                    "BluetoothViewModel", "Invalid move: $row, $col : ${gameState.turn} : $isFirst"
                )
            }
        }
    }

    private fun postMoveUpdate(board: List<List<String>>) {
        val winner = checkWinner(board)

        val updatedWinner = when {
            winner == "X" && isFirst -> {
                localDev
            }

            winner == "X" && !isFirst -> {
                _state.value.metadata.choices.find { it.id == "player2" }?.name
                    ?: "Player 2" // Player 2 wins if X wins and isFirst is false
            }

            winner == "O" && !isFirst -> {
                localDev
            }

            winner == "O" && isFirst -> {
                _state.value.metadata.choices.find { it.id == "player1" }?.name ?: "Player 1"
            }

            else -> {
                ""
            }
        }

        // If there's a winner
        if (updatedWinner.isNotEmpty()) {
            _state.update { currentState ->
                currentState.copy(gameState = currentState.gameState.copy(winner = updatedWinner))
            }
        }
        // Check for draw
        else if (isDraw(board)) {
            isDbUpdate = true
            _state.update { currentState ->
                currentState.copy(
                    gameState = currentState.gameState.copy(
                        draw = true, winner = "Draw"
                    )
                )
            }
        }
        isDbUpdate = false
        // Send updated game state over Bluetooth
        sendMessage(GameData(_state.value.gameState, _state.value.metadata))
        isDbUpdate = true
    }


    private fun checkWinner(board: List<List<String>>): String {
        // Check rows for winner
        for (i in 0 until 3) {
            if (board[i][0] != " " && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                isDbUpdate = true
                return board[i][0]
            }
        }

        // Check columns for winner
        for (i in 0 until 3) {
            if (board[0][i] != " " && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                isDbUpdate = true
                return board[0][i]
            }
        }

        // Check diagonals for winner
        if (board[0][0] != " " && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            isDbUpdate = true
            return board[0][0]
        }
        if (board[0][2] != " " && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            isDbUpdate = true
            return board[0][2]
        }

        // Check for draw
        val isDraw = board.all { row -> row.all { cell -> cell != " " } }
        if (isDraw) {
            isDbUpdate = true
            return "Draw"
        }

        return "" // No winner yet
    }

    private fun isDraw(board: List<List<String>>): Boolean {
        return board.all { row -> row.all { cell -> cell != " " } }
    }

    private fun togglePlayer() {
        _state.update { currentState ->
            val newTurn = if (currentState.gameState.turn == "X") "O" else "X"
            currentState.copy(gameState = currentState.gameState.copy(turn = newTurn))
        }
    }

//    fun updateChoicesAfterConnection() {
//        val localAddress = _localDev
//
//        _state.update { currentState ->
//            currentState.copy(
//                metadata = currentState.metadata.copy(
//                    choices = listOf(
//                        Choice(id = "player1", name = localAddress),
//                        Choice(id = "player2", name = remoteAddress)
//                    )
//                )
//            )
//        }
//    }

    // check if move is allowed to handle unwarranted clicks
    private fun isMoveAllowed(currentTurn: Int): Boolean {
        return if (isFirst) {
            currentTurn % 2 == 0
        } else {
            currentTurn % 2 != 0
        }
    }

}