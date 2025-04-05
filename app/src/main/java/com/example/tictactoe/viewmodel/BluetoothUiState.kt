package com.example.tictactoe.viewmodel

import com.example.tictactoe.domain.game.BluetoothDevice
import com.example.tictactoe.domain.game.GameData
import com.example.tictactoe.domain.game.GameState
import com.example.tictactoe.domain.game.Metadata
import com.example.tictactoe.domain.game.MiniGame
import kotlinx.serialization.Serializable

@Serializable
data class BluetoothUiState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val errorMessage: String? = null,
    val messages: List<GameData> = emptyList(),
    val localDev: String = "",
    val gameState: GameState = GameState(
        board = listOf(listOf(" ", " ", " "), listOf(" ", " ", " "), listOf(" ", " ", " ")),
        turn = "",
        winner = "",
        draw = false,
        connectionEstablished = false,
        reset = false
    ),
    val metadata: Metadata = Metadata(
        choices = listOf(),
        miniGame = MiniGame(player1Choice = "", player2Choice = "")
    )
)