package com.example.tictactoe.data.game

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.example.tictactoe.domain.game.GameData
import com.example.tictactoe.domain.game.TransferFailedException


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException

class BluetoothDataTransferService(
    private val socket: BluetoothSocket
) {
    fun listenForIncomingMessages(): Flow<GameData> {
        return flow {
            if (!socket.isConnected) {
                return@flow
            }
            val buffer = ByteArray(1024)
            while (true) {
                try {
                    val byteCount = socket.inputStream.read(buffer)
                    if (byteCount == -1) break
                    val messageJson = buffer.decodeToString(0, byteCount)
//                    Log.d("BluetoothReceive", "Received JSON: $messageJson")
                    val message = Json.decodeFromString(GameData.serializer(), messageJson)
                    emit(message)
                } catch (e: IOException) {
                    e.printStackTrace()
                    throw TransferFailedException()
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun sendMessage(message: GameData): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val messageJson = Json.encodeToString(GameData.serializer(), message)
//                Log.d("BluetoothSend", "Sending JSON: $messageJson")
                socket.outputStream.write(messageJson.toByteArray())
                socket.outputStream.flush()
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }

}