package com.turtlepaw.heart_connection

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import java.util.UUID

@RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
fun createGattCallback(
    onHeartRateChanged: (heartRate: Int) -> Unit
): BluetoothGattCallback {
    val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices()
            }
        }

        @SuppressLint("MissingPermission")
        @TargetApi(Build.VERSION_CODES.TIRAMISU)
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val heartRateService =
                    gatt.getService(UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb"))
                val heartRateCharacteristic =
                    heartRateService?.getCharacteristic(UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb"))
                if (heartRateCharacteristic != null) {
                    gatt.setCharacteristicNotification(heartRateCharacteristic, true)
                    val descriptor =
                        heartRateCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                    gatt.writeDescriptor(
                        descriptor,
                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    )
                } else {
                    Log.e("GattCallback", "Heart Rate characteristic not found")
                }
            } else {
                Log.e("GattCallback", "Service discovery failed")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            data: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, data)
            if (characteristic.uuid == UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")) {
                val heartRateValue = parseHeartRate(data)
                onHeartRateChanged(heartRateValue)
                Log.d("GattCallback", "Heart Rate: $heartRateValue")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            data: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, data, status)
            if (status == BluetoothGatt.GATT_SUCCESS && characteristic.uuid == UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")) {
                val heartRateValue = parseHeartRate(data)
                onHeartRateChanged(heartRateValue)
                Log.d("GattCallback", "Heart Rate: $heartRateValue")
            }
        }
    }
    return gattCallback
}