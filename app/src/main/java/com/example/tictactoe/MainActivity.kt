package com.example.tictactoe

import android.content.Context
import android.os.Bundle
import com.example.tictactoe.ui.MultiplayerUI
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.example.tictactoe.ui.DeviceScreen
import com.example.tictactoe.ui.GameUI
import com.example.tictactoe.ui.HomeUI
import com.example.tictactoe.ui.MultiplayerGameUI
import com.example.tictactoe.ui.PastGamesActivity
import com.example.tictactoe.ui.SettingsPage
import com.example.tictactoe.viewmodel.BluetoothUiState
import com.example.tictactoe.viewmodel.BluetoothViewModel
import com.example.tictactoe.viewmodel.GameViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val bluetoothManager by lazy {
        applicationContext.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {}

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            val canEnableBluetooth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                perms[Manifest.permission.BLUETOOTH_CONNECT] == true
            } else true

            if (canEnableBluetooth && !isBluetoothEnabled) {
                enableBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                )
            )
        }

        setContent {
            // Obtain the ViewModel
            val viewModel = ViewModelProvider(this)[GameViewModel::class.java]
            val viewModelBluetooth = hiltViewModel<BluetoothViewModel>()
            val state by viewModelBluetooth.state.collectAsState()

            LaunchedEffect(key1 = state.errorMessage) {
                state.errorMessage?.let { message ->
                    Toast.makeText(
                        applicationContext,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            LaunchedEffect(key1 = state.isConnected) {
                if (state.isConnected) {
                    Toast.makeText(
                        applicationContext,
                        "You're connected!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            MainApp(viewModel, viewModelBluetooth, state, this)
        }


//        override fun onCreate(savedInstanceState: Bundle?) {
//            super.onCreate(savedInstanceState)
//            // Obtain the ViewModel
//            val viewModel = ViewModelProvider(this)[GameViewModel::class.java]
//            setContent {
//                MainApp(viewModel)
//            }
//        }
    }
}

@Composable
fun MainApp(
    gameViewModel: GameViewModel,
    viewModelBluetooth: BluetoothViewModel,
    state: BluetoothUiState,
    context: Context
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "homeUI") {
        composable("homeUI") {
            HomeUI(
                navController = navController,
                viewModel = gameViewModel
            )
        }
        composable("gameUI") {
            GameUI(gameViewModel, navController, context)
        }
        composable("multiplayerUI") {
            MultiplayerGameUI(viewModelBluetooth, navController = navController, context)
        }
        composable("bluetoothUI") {
            when {
                state.isConnecting -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Text(text = "Connecting...")
                    }
                }

                state.isConnected -> {
                    MultiplayerUI(
                        viewModelBluetooth,
                        navController = navController
//                        onDisconnect = viewModelBluetooth::disconnectFromDevice,
//                        onSendMessage = viewModelBluetooth::sendMessage
                    )
                }

                else -> {
                    DeviceScreen(
                        state = state,
                        onStartScan = viewModelBluetooth::startScan,
                        onStopScan = viewModelBluetooth::stopScan,
                        onDeviceClick = viewModelBluetooth::connectToDevice,
                        onStartServer = viewModelBluetooth::waitForIncomingConnections
                    )
                }
            }
        }

        composable("pastGames") {
            PastGamesActivity(context)
        }
        composable(
            "settings/{returnDestination}",
            arguments = listOf(navArgument("returnDestination") { type = NavType.StringType })
        ) { backStackEntry ->
            val returnDestination = backStackEntry.arguments?.getString("returnDestination") ?: "homeUI"

            SettingsPage(
                viewModel = gameViewModel,
                navController = navController,
                returnDestination = returnDestination,
            )
        }

    }
}


