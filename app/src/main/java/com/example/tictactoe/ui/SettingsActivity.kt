package com.example.tictactoe.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tictactoe.ai.DifficultyLevel
import com.example.tictactoe.viewmodel.GameViewModel

@Composable
fun SettingsPage(
    viewModel: GameViewModel,
    navController: NavController,
    returnDestination: String,
) {
    val selectedDifficulty by viewModel.difficulty.collectAsState()
    val selectedGameMode by viewModel.gameMode.collectAsState()
    val difficulties = DifficultyLevel.values()
    val gameModes = GameMode.values()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(32.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Select Difficulty", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            difficulties.forEach { difficulty ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    RadioButton(
                        selected = selectedDifficulty == difficulty,
                        onClick = {
                            viewModel.setDifficulty(difficulty)
                            navController.navigate(returnDestination)
                        }
                    )
                    Text(
                        text = difficulty.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Select Game Mode", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            gameModes.forEach { mode ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    RadioButton(
                        selected = selectedGameMode == mode,
                        onClick = {
                            viewModel.setGameMode(mode)
                            navController.navigate(returnDestination)
                        }
                    )
                    Text(
                        text = mode.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun GameModeButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Text(text = text, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

enum class GameMode {
    VS_AI,
    VS_HUMAN,
    MULTIPLAYER,
}
