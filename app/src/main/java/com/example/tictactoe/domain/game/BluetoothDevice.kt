package com.example.tictactoe.domain.game

import kotlinx.serialization.Serializable

typealias BluetoothDeviceDomain = BluetoothDevice

@Serializable
data class BluetoothDevice(
    val name: String?,
    val address: String
)
