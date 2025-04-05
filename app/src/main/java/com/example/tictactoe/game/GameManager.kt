package com.example.tictactoe

import com.example.tictactoe.models.Board

class GameManager {
    private val board: Board = Board()

    fun getBoardState(): Array<CharArray> {
        return board.getBoardState()
    }

    fun makeMove(row: Int, col: Int, player: Char): Boolean {
        return board.makeMove(row, col, player)
    }

    fun checkWin(): Char? {
        val boardState = board.getBoardState()
        return board.checkWin(boardState)
    }

    fun isDraw(): Boolean {
        return board.isDraw()
    }

    fun resetBoard() {
        board.resetBoard()
    }
}
