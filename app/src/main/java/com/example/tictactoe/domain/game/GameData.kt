package com.example.tictactoe.domain.game

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val board: List<List<String>>,
    val turn: String,
    val winner: String,
    val draw: Boolean,
    val connectionEstablished: Boolean,
    val reset: Boolean
)

@Serializable
data class PlayerChoice(
    val id: String,
    val name: String
)

@Serializable
data class MiniGame(
    val player1Choice: String,
    val player2Choice: String
)

@Serializable
data class Metadata(
    val choices: List<Choice>,
    val miniGame: MiniGame
)

@Serializable
data class GameData(
    val gameState: GameState,
    val metadata: Metadata
)

@Serializable
data class Choice(
    val id: String,
    val name: String? = null
)
