package com.example.tictactoe.domain.game

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val isConnected: StateFlow<Boolean>
    val scannedDevices: StateFlow<List<BluetoothDevice>>
    val pairedDevices: StateFlow<List<BluetoothDevice>>
    val errors: SharedFlow<String>
    val localDeviceName: String?

    fun startDiscovery()
    fun stopDiscovery()

    fun startBluetoothServer(): Flow<ConnectionResult>
    fun connectToDevice(device: BluetoothDevice): Flow<ConnectionResult>

    suspend fun trySendMessage(gameData: GameData, localDev: String): GameData?
    fun closeConnection()
    fun release()

    fun setOnConnectionUpdated(callback: (Metadata) -> Unit)

//    fun getBluetoothAdapterName(): String?
}