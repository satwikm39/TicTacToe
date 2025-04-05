package com.example.tictactoe.ai

import com.example.tictactoe.models.Board

class AiPlayer {
    companion object {
        const val AI_MARKER = Board.PLAYER_O
        const val PLAYER_MARKER = Board.PLAYER_X
    }

    fun getMove(boardState: Array<CharArray>, difficulty: DifficultyLevel): Pair<Int, Int>? {
        return when (difficulty) {
            DifficultyLevel.EASY -> getRandomMove(boardState)
            DifficultyLevel.MEDIUM -> {
                if (Math.random() > 0.5) {
                    getRandomMove(boardState)
                } else {
                    getOptimalMove(boardState)
                }
            }
            DifficultyLevel.HARD -> {
                getOptimalMove(boardState)
            }
        }
    }

    private fun getRandomMove(boardState: Array<CharArray>): Pair<Int, Int>? {
        val emptySpaces = mutableListOf<Pair<Int, Int>>()
        for (i in boardState.indices) {
            for (j in boardState[i].indices) {
                if (boardState[i][j] == Board.EMPTY) {
                    emptySpaces.add(Pair(i, j))
                }
            }
        }
        return if (emptySpaces.isNotEmpty()) {
            emptySpaces.random()
        } else {
            null
        }
    }

    private fun getOptimalMove(boardState: Array<CharArray>): Pair<Int, Int>? {
        val winningMove = findWinningMove(boardState, AI_MARKER)
        if (winningMove != null) {
            return winningMove
        }

        val blockingMove = findWinningMove(boardState, PLAYER_MARKER)
        if (blockingMove != null) {
            return blockingMove
        }

        val bestMove = minimax(boardState, AI_MARKER, 0, Int.MIN_VALUE, Int.MAX_VALUE)
        return if (bestMove.second != Pair(-1, -1)) bestMove.second else null
    }

    private fun findWinningMove(boardState: Array<CharArray>, marker: Char): Pair<Int, Int>? {
        val emptySpaces = getLegalMoves(boardState)
        for (move in emptySpaces) {
            boardState[move.first][move.second] = marker
            if (checkWinner(boardState) == marker) {
                boardState[move.first][move.second] = Board.EMPTY
                return move
            }
            boardState[move.first][move.second] = Board.EMPTY
        }
        return null
    }

    private fun minimax(boardState: Array<CharArray>, marker: Char, depth: Int, alpha: Int, beta: Int): Pair<Int, Pair<Int, Int>> {
        val boardStateValue = evaluateBoard(boardState)
        if (boardStateValue != 0 || isBoardFull(boardState)) {
            return Pair(boardStateValue, Pair(-1, -1))
        }

        var bestMove = Pair(-1, -1)
        var bestScore = if (marker == AI_MARKER) Int.MIN_VALUE else Int.MAX_VALUE
        val emptySpaces = getLegalMoves(boardState)
        var newAlpha = alpha
        var newBeta = beta

        for (move in emptySpaces) {
            boardState[move.first][move.second] = marker
            val score = minimax(boardState, if (marker == AI_MARKER) PLAYER_MARKER else AI_MARKER, depth + 1, newAlpha, newBeta).first

            // Undo move
            boardState[move.first][move.second] = Board.EMPTY

            if (marker == AI_MARKER) {
                if (score > bestScore) {
                    bestScore = score
                    bestMove = move
                }
                newAlpha = maxOf(newAlpha, bestScore)
            } else {
                if (score < bestScore) {
                    bestScore = score
                    bestMove = move
                }
                newBeta = minOf(newBeta, bestScore)
            }

            // Alpha-beta pruning
            if (newBeta <= newAlpha) {
                break
            }
        }

        return Pair(bestScore, bestMove)
    }

    private fun evaluateBoard(boardState: Array<CharArray>): Int {
        val winner = checkWinner(boardState)
        return when {
            winner == AI_MARKER -> 10 // AI wins
            winner == PLAYER_MARKER -> -10 // Player wins
            else -> 0 // Draw or ongoing game
        }
    }

    private fun getLegalMoves(boardState: Array<CharArray>): List<Pair<Int, Int>> {
        val moves = mutableListOf<Pair<Int, Int>>()
        for (i in boardState.indices) {
            for (j in boardState[i].indices) {
                if (boardState[i][j] == Board.EMPTY) {
                    moves.add(Pair(i, j))
                }
            }
        }
        return moves
    }

    private fun isBoardFull(boardState: Array<CharArray>): Boolean {
        for (row in boardState) {
            for (cell in row) {
                if (cell == Board.EMPTY) {
                    return false
                }
            }
        }
        return true
    }

    private fun checkWinner(boardState: Array<CharArray>): Char? {
        return Board().checkWin(boardState)
    }
}