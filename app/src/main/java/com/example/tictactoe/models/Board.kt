package com.example.tictactoe.models

class Board {
    companion object {
        const val PLAYER_X = 'X'
        const val PLAYER_O = 'O'
        const val EMPTY = ' '

        const val BOARD_SIZE = 3
    }

    private val board: Array<CharArray> = Array(BOARD_SIZE) { CharArray(BOARD_SIZE) { EMPTY } }

    fun getBoardState(): Array<CharArray> {
        return board
    }

    fun makeMove(row: Int, col: Int, player: Char): Boolean {
        return if (isValidMove(row, col)) {
            board[row][col] = player
            true
        } else {
            false
        }
    }

    private fun isValidMove(row: Int, col: Int): Boolean {
        return board[row][col] == EMPTY
    }

    fun checkWin(boardState: Array<CharArray>): Char? {
        // Check rows and columns
        for (i in 0 until BOARD_SIZE) {
            if (boardState[i][0] == boardState[i][1] && boardState[i][1] == boardState[i][2] && boardState[i][0] != EMPTY) {
                return boardState[i][0]
            }
            if (boardState[0][i] == boardState[1][i] && boardState[1][i] == boardState[2][i] && boardState[0][i] != EMPTY) {
                return boardState[0][i]
            }
        }

        // Check diagonals
        if (boardState[0][0] == boardState[1][1] && boardState[1][1] == boardState[2][2] && boardState[0][0] != EMPTY) {
            return boardState[0][0]
        }
        if (boardState[0][2] == boardState[1][1] && boardState[1][1] == boardState[2][0] && boardState[0][2] != EMPTY) {
            return boardState[0][2]
        }

        // No winner yet
        return null
    }


    fun isDraw(): Boolean {
        for (i in 0 until BOARD_SIZE) {
            for (j in 0 until BOARD_SIZE) {
                if (board[i][j] == EMPTY) return false
            }
        }
        return true
    }

    fun resetBoard() {
        for (i in 0 until BOARD_SIZE) {
            for (j in 0 until BOARD_SIZE) {
                board[i][j] = EMPTY
            }
        }
    }
}
