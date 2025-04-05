package com.example.tictactoe.data.game

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.tictactoe.domain.game.BluetoothController
import com.example.tictactoe.domain.game.BluetoothDeviceDomain
import com.example.tictactoe.domain.game.GameData
import com.example.tictactoe.domain.game.ConnectionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import com.example.tictactoe.domain.game.Choice
import com.example.tictactoe.domain.game.Metadata
import com.example.tictactoe.domain.game.MiniGame

@SuppressLint("MissingPermission")
class AndroidBluetoothController(
    private val context: Context
) : BluetoothController {

    private var onConnectionUpdated: ((Metadata) -> Unit)? = null

    override fun setOnConnectionUpdated(callback: (Metadata) -> Unit) {
        onConnectionUpdated = callback
    }

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }


//    override fun getBluetoothAdapterName(): String? {
//        return bluetoothAdapter?.name
//    }

    private var dataTransferService: BluetoothDataTransferService? = null

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

//    private var _localDeviceName = MutableStateFlow<String?>(null)
//    override val localDeviceName: StateFlow<String?>
//        get() = _localDeviceName.asStateFlow()

    override var localDeviceName: String? = null

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    private val _errors = MutableSharedFlow<String>()
    override val errors: SharedFlow<String>
        get() = _errors.asSharedFlow()

    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        _scannedDevices.update { devices ->
            val newDevice = device.toBluetoothDeviceDomain()
            if (newDevice in devices) devices else devices + newDevice
        }
    }

    private val bluetoothStateReceiver = BluetoothStateReceiver { isConnected, bluetoothDevice ->
        if (bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true) {
            _isConnected.update { isConnected }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                _errors.emit("Can't connect to a non-paired device.")
            }
        }
    }

    private var currentServerSocket: BluetoothServerSocket? = null
    private var currentClientSocket: BluetoothSocket? = null

    init {
        updatePairedDevices()
        localDeviceName = bluetoothAdapter?.name
//        Log.d("AndroidBluetoothController", "Local bluetoothdevice name: ${bluetoothAdapter?.name}")
//        Log.d("AndroidBluetoothController", "Local device name: $localDeviceName")
        context.registerReceiver(
            bluetoothStateReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )
    }

    override fun startDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )

        updatePairedDevices()

        bluetoothAdapter?.startDiscovery()
    }

    override fun stopDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            return
        }

        bluetoothAdapter?.cancelDiscovery()
    }

    override fun startBluetoothServer(): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                "chat_service",
                UUID.fromString(SERVICE_UUID)
            )

            var shouldLoop = true
            while (shouldLoop) {
                currentClientSocket = try {
                    currentServerSocket?.accept()
                } catch (e: IOException) {
                    shouldLoop = false
                    null
                }
                emit(ConnectionResult.ConnectionEstablished)
                currentClientSocket?.let {
                    currentServerSocket?.close()
                    val service = BluetoothDataTransferService(it)
                    dataTransferService = service

                    emitAll(
                        service
                            .listenForIncomingMessages()
                            .map {
                                ConnectionResult.TransferSucceeded(it)
                            }
                    )
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }

    fun requestDiscoverable(context: Context, duration: Int = 300) {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration) // duration in seconds
        }
        context.startActivity(discoverableIntent)
    }

    override fun connectToDevice(device: BluetoothDeviceDomain): Flow<ConnectionResult> {
        return flow {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            currentClientSocket = bluetoothAdapter
                ?.getRemoteDevice(device.address)
                ?.createRfcommSocketToServiceRecord(
                    UUID.fromString(SERVICE_UUID)
                )
            stopDiscovery()

            currentClientSocket?.let { socket ->
                try {
                    socket.connect()
                    emit(ConnectionResult.ConnectionEstablished)

                    BluetoothDataTransferService(socket).also {
                        dataTransferService = it
                        emitAll(
                            it.listenForIncomingMessages()
                                .map { ConnectionResult.TransferSucceeded(it) }
                        )
                    }
                } catch (e: IOException) {
                    socket.close()
                    currentClientSocket = null
                    emit(ConnectionResult.Error("Connection was interrupted"))
                }
            }
        }.onCompletion {
            closeConnection()
        }.flowOn(Dispatchers.IO)
    }


//    override suspend fun trySendMessage(message: String): BluetoothMessage? {
//        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
//            return null
//        }
//
//        if (dataTransferService == null) {
//            return null
//        }
//
//        val bluetoothMessage = BluetoothMessage(
//            message = message,
//            senderName = bluetoothAdapter?.name ?: "Unknown name",
//            isFromLocalUser = true
//        )
//
//        dataTransferService?.sendMessage(bluetoothMessage.toByteArray())
//
//        return bluetoothMessage
//    }

    override suspend fun trySendMessage(gameData: GameData, localDev: String): GameData? {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        val dataTransferService = this.dataTransferService ?: return null

        // Update metadata
        val updatedMetadata = when (gameData.metadata.choices.size) {
            0 -> {
                gameData.metadata.copy(
                    choices = listOf(
                        Choice(id = "player1", name = bluetoothAdapter?.name)
                    )
                )
            }
            1 -> {
                gameData.metadata.copy(
                    choices = listOf(
                        gameData.metadata.choices[0],
                        Choice(id = "player2", name = bluetoothAdapter?.name)
                    )
                )
            }
            else -> {
                gameData.metadata
            }
        }

        val updatedGameData = gameData.copy(metadata = updatedMetadata)
//        Log.d("BluetoothViewModel", "localDev: $localDev")
        val isSuccess = dataTransferService.sendMessage(updatedGameData)
        Log.d("BluetoothViewModel", "Sending message: $updatedGameData")
        Log.d("BluetoothViewModel", "Success message: $isSuccess")
        return if (isSuccess) updatedGameData else null
    }

    override fun closeConnection() {
        currentClientSocket?.close()
        currentServerSocket?.close()
        currentClientSocket = null
        currentServerSocket = null
    }

    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
        context.unregisterReceiver(bluetoothStateReceiver)
        closeConnection()
    }

    private fun updatePairedDevices() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            return
        }
        bluetoothAdapter
            ?.bondedDevices
            ?.map { it.toBluetoothDeviceDomain() }
            ?.also { devices ->
                _pairedDevices.update { devices }
            }
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val SERVICE_UUID = "27b7d1da-08c7-4505-a6d1-2459987e5e2d"
    }
}