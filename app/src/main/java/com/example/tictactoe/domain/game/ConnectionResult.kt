package com.example.tictactoe.domain.game

sealed interface ConnectionResult {
    object ConnectionEstablished: ConnectionResult
    data class TransferSucceeded(val gameData: GameData): ConnectionResult
    data class Error(val message: String): ConnectionResult
}