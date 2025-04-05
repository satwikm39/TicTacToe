package com.example.tictactoe.data.game


import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.example.tictactoe.domain.game.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain(): BluetoothDeviceDomain {
    return BluetoothDeviceDomain(
        name = name,
        address = address
    )
}