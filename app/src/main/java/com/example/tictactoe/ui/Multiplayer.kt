package com.example.tictactoe.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tictactoe.viewmodel.BluetoothViewModel

@Composable
fun MultiplayerUI(
    viewModel: BluetoothViewModel,
    navController: NavController,
) {
    val state = viewModel.state.collectAsState().value
    var isOpponentSelected by remember { mutableStateOf(false) }

    if (viewModel.shouldNavigateToGame()) {
        navController.navigate("multiplayerUI")
        Log.d("MultiplayerUI", "Navigating to multiplayerUI")
    }

    Scaffold(
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Who goes first?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Button for "Me"
                Button(
                    onClick = {
                        viewModel.setPlayerChoice("Me")
                        isOpponentSelected = false // Reset the opponent selected state
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                ) {
                    Text("Me", color = MaterialTheme.colorScheme.onPrimary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Button for "Opponent"
                Button(
                    onClick = {
                        viewModel.setPlayerChoice("Opponent")
                        isOpponentSelected = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                ) {
                    Text("Opponent", color = MaterialTheme.colorScheme.onPrimary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isOpponentSelected) {
                    Text(
                        text = "Waiting for other player to select...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Disconnect Button
                Button(
                    onClick = {
                        viewModel.disconnectFromDevice()
                        navController.popBackStack()
                    },
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
                ) {
                    Text("Disconnect", color = MaterialTheme.colorScheme.onError)
                }
            }
        }
    )
}


fun BluetoothViewModel.shouldNavigateToGame(): Boolean {
    val game = state.value.metadata.miniGame
    if (game.player1Choice.isNotEmpty() && game.player2Choice.isNotEmpty() && !shouldNav) {
        shouldNav = true
        return true
    }
    return false
}

