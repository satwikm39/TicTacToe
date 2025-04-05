package com.example.tictactoe.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tictactoe.data.GameResult
import com.example.tictactoe.data.TicTacToeDbHelper

@Composable
fun PastGamesActivity(context: Context) {
    val dbHelper = TicTacToeDbHelper(context)

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Past Games",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.inversePrimary
        )
        // Get all the games from the DB
        val games = dbHelper.getAllGameResults()
        HeaderRow()
        LazyColumn {
            items(games) { game ->
                GameItem(game)
            }
        }
    }
}

@Composable
fun HeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text("Date", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.inversePrimary)
        Text("Winner", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.inversePrimary)
        Text("Mode", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.inversePrimary)
    }
}

@Composable
fun GameItem(game: GameResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = game.date,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = game.winner,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = game.difficulty,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = getDifficultyColor(game.difficulty)
            )

        }
    }
}

@Composable
private fun getDifficultyColor(difficulty: String): Color {
    return when (difficulty) {
        "Easy" -> Color(0xFF4CAF50) // green
        "Medium" -> Color(0xFFC2B280) //khaki
        "Hard" -> MaterialTheme.colorScheme.error // red
        else -> MaterialTheme.colorScheme.onSurface
    }
}