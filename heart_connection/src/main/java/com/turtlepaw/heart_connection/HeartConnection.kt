package com.turtlepaw.heart_connection

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.scan.BleScanRuleConfig
import java.util.UUID


enum class ScanningStatus {
    Idle,
    Scanning,
    Unsupported,
    Disabled
}

class HeartConnection(
    private var gattCallback: BluetoothGattCallback,
    private val context: Context,
    private val application: Application?
) {
    private val handler = Handler(Looper.getMainLooper())
    private var scanRunnable: Runnable? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private val _devices = MutableLiveData<List<BleDevice>>()
    val devices: LiveData<List<BleDevice>> = _devices
    private val _isScanning = MutableLiveData(false)
    val isScanning: LiveData<Boolean> = _isScanning

    init {
        if (application != null) {
            BleManager.getInstance().init(application)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startDiscovery(): ScanningStatus {
        val manager = BleManager.getInstance()
        val scanRuleConfig = BleScanRuleConfig.Builder()
            //.setServiceUuids(arrayOf(UUID.fromString("0x180D")))
            .setScanTimeOut(10000)
            .build()
        manager.initScanRule(scanRuleConfig)

        scanRunnable = Runnable {
            _isScanning.value = true
            // Start the BLE scan
            manager.scan(object : BleScanCallback() {
                override fun onScanStarted(success: Boolean) {
                    // Scanning started
                    _devices.postValue(mutableListOf())
                }

                @SuppressLint("MissingPermission")
                override fun onScanning(bleDevice: BleDevice) {
                    // Device detected
                    val currentList = _devices.value ?: mutableListOf()
                    if (!currentList.contains(bleDevice)) {
                        val updatedList = currentList.toMutableList()
                        updatedList.add(bleDevice)
                        _devices.postValue(updatedList)
                    }
                }

                override fun onScanFinished(scanResultList: List<BleDevice>) {
                    // Scanning finished
                }
            })

            // Schedule the next scan after 1 second
            handler.postDelayed(scanRunnable!!, 10000)
        }

        if (!isBluetoothAvailable(context)) {
            return ScanningStatus.Disabled
        }

        handler.post(scanRunnable!!)

        return ScanningStatus.Scanning
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopDiscovery() {
        if (_isScanning.value == true) BleManager.getInstance().cancelScan()
        _isScanning.value = false
        scanRunnable?.let { handler.removeCallbacks(it) }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connectToDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun getHeartRate() {
        val heartRateServiceUUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")
        val heartRateCharacteristicUUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")

        val heartRateService = bluetoothGatt?.getService(heartRateServiceUUID)
        val heartRateCharacteristic =
            heartRateService?.getCharacteristic(heartRateCharacteristicUUID)

        bluetoothGatt?.setCharacteristicNotification(heartRateCharacteristic, true)

        val descriptor =
            heartRateCharacteristic?.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
        descriptor?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bluetoothGatt?.writeDescriptor(
                    it,
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                )
            } else {
                it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                bluetoothGatt?.writeDescriptor(it)
            }
        }
    }

    fun setGattCallback(callback: BluetoothGattCallback) {
        this.gattCallback = callback
    }

    /**
     * https://stackoverflow.com/a/21010026/15751555
     * Check for Bluetooth.
     *
     * @return true if Bluetooth is available.
     */
    private fun isBluetoothAvailable(context: Context): Boolean {
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager.adapter

        return bluetoothAdapter != null && bluetoothAdapter.isEnabled && bluetoothAdapter.state == BluetoothAdapter.STATE_ON
    }
}

fun parseHeartRate(value: ByteArray): Int {
    // Parse heart rate value from the characteristic value
    val format = if ((value[0].toInt() and 0x01) != 0) {
        // Heart rate value format is UINT16
        (value[1].toInt() and 0xFF) or (value[2].toInt() shl 8 and 0xFF00)
    } else {
        // Heart rate value format is UINT8
        value[1].toInt() and 0xFF
    }
    return format
}