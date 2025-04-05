package com.example.tictactoe.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tictactoe.ai.AiPlayer
import com.example.tictactoe.ai.DifficultyLevel
import com.example.tictactoe.ui.GameMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel : ViewModel() {
    private val _difficulty = MutableStateFlow(DifficultyLevel.EASY) // Set default difficulty
    val difficulty = _difficulty.asStateFlow()

    private val _gameMode = MutableStateFlow(GameMode.VS_HUMAN) // Default game mode
    val gameMode = _gameMode.asStateFlow()

    private val aiPlayer = AiPlayer()
    private val _currentPlayer = MutableStateFlow("X")
    val currentPlayer = _currentPlayer.asStateFlow()

    private val _gameOver = MutableStateFlow<Pair<Boolean, Char>?>(null)
    val gameOver = _gameOver.asStateFlow()


    // Initialize _gameBoard as an Array of CharArray
    private val _gameBoard = MutableStateFlow(
        arrayOf(
            charArrayOf(' ', ' ', ' '),
            charArrayOf(' ', ' ', ' '),
            charArrayOf(' ', ' ', ' ')
        )
    )
    val gameBoard = _gameBoard.asStateFlow()

    fun setDifficulty(newDifficulty: DifficultyLevel) {
        _difficulty.value = newDifficulty
    }

    fun setGameMode(newGameMode: GameMode) {
        _gameMode.value = newGameMode
    }

    fun resetGame() {
        _gameBoard.value = arrayOf(
            charArrayOf(' ', ' ', ' '),
            charArrayOf(' ', ' ', ' '),
            charArrayOf(' ', ' ', ' ')
        )
        _currentPlayer.value = "X"
        _gameOver.value = null
    }

    fun makeMove(row: Int, col: Int) {
        if (_gameBoard.value[row][col] == ' ' && _gameOver.value == null) {
            val updatedBoard = _gameBoard.value.map { it.copyOf() }.toTypedArray()
            updatedBoard[row][col] = _currentPlayer.value[0]
            _gameBoard.value = updatedBoard

            val (isGameOver, winner) = checkWin()
            if (isGameOver) {
                _gameOver.value = Pair(true, winner ?: 'D')
            } else {
                togglePlayer()
            }
        }
    }

    private fun togglePlayer() {
        _currentPlayer.value = if (_currentPlayer.value == "X") "O" else "X"
        if (_currentPlayer.value == "O" && _gameMode.value == GameMode.VS_AI) {
            performAiMove()
        }
    }

    private fun performAiMove() {
        val aiMove = aiPlayer.getMove(_gameBoard.value, _difficulty.value)
        if (aiMove != null) {
            makeMove(aiMove.first, aiMove.second)
        }
    }

    private fun checkWin(): Pair<Boolean, Char?> {
        val board = _gameBoard.value

        for (i in 0 until 3) {
            if (board[i][0] != ' ' && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return Pair(true, board[i][0])
            }
            if (board[0][i] != ' ' && board[0][i] == board[1][i] && board[1][i] == board[2][i]) {
                return Pair(true, board[0][i])
            }
        }

        if (board[0][0] != ' ' && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return Pair(true, board[0][0])
        }
        if (board[0][2] != ' ' && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return Pair(true, board[0][2])
        }

        val isDraw = board.all { row -> row.none { cell -> cell == ' ' } }
        if (isDraw) {
            return Pair(true, null)
        }

        return Pair(false, null)
    }
}

