package com.example.tictactoe.data.game

import com.example.tictactoe.domain.game.GameData
import kotlinx.serialization.json.Json


// Convert BluetoothMessage to JSON string
fun GameData.toJsonString(): String {
    return Json.encodeToString(GameData.serializer(), this)
}

// Convert JSON string to BluetoothMessage
fun String.toBluetoothMessage(): GameData {
    return Json.decodeFromString(GameData.serializer(), this)
}

// Convert BluetoothMessage to ByteArray for transmission
fun GameData.toByteArray(): ByteArray {
    return this.toJsonString().encodeToByteArray()
}

// Convert ByteArray back to BluetoothMessage after receiving
fun ByteArray.toBluetoothMessage(): GameData {
    return this.decodeToString().toBluetoothMessage()
}