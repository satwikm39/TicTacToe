package com.example.tictactoe.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tictactoe.viewmodel.GameViewModel

@Composable
fun HomeUI(navController: NavController, viewModel: GameViewModel) {
    val difficulty by viewModel.difficulty.collectAsState() // difficulty state
    val gameMode by viewModel.gameMode.collectAsState() // gameMode state

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Button to start game based on gameMode
            Button(
                onClick = {
                    viewModel.resetGame() // reset gameBoard and currentPlayer
                    when (gameMode) {
                        GameMode.MULTIPLAYER -> navController.navigate("bluetoothUI")
                        GameMode.VS_HUMAN -> navController.navigate("gameUI")
                        GameMode.VS_AI -> navController.navigate("gameUI")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(60.dp)
            ) {
                Text("Start Game")
            }

            // Display game mode and difficulty label
            val gameModeLabel = when (gameMode) {
                GameMode.VS_AI -> "Vs AI : $difficulty"
                GameMode.VS_HUMAN -> "Vs Human"
                GameMode.MULTIPLAYER -> "Multiplayer"
            }

            Text(
                text = gameModeLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White // Ensure it's visible against the black background
            )

            // Settings button
            Button(
                onClick = {
                    navController.navigate("settings/homeUI")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(60.dp)
            ) {
                Text("Settings")
            }

            // PastGames button
            Button(
                onClick = {
                    navController.navigate("pastGames")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(60.dp)
            ) {
                Text("Past Games")
            }
        }
    }
}

