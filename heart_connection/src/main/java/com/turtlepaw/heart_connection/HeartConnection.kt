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
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleScanCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.scan.BleScanRuleConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume


enum class ScanningStatus {
    Idle,
    Scanning,
    Unsupported,
    Disabled
}

enum class DeviceScanResult {
    Compatible,
    Unknown
}

typealias Device = Pair<DeviceScanResult, BleDevice>

class HeartConnection(
    private var gattCallback: BluetoothGattCallback,
    private val context: Context,
    private val application: Application?
) : CoroutineScope {
    private val handler = Handler(Looper.getMainLooper())
    private var scanRunnable: Runnable? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private val _devices = MutableLiveData<List<Device>>()
    val devices: LiveData<List<Device>> = _devices
    private val _isScanning = MutableLiveData(false)
    val isScanning: LiveData<Boolean> = _isScanning

    init {
        if (application != null) {
            BleManager.getInstance().init(application)
        }
    }

    // Coroutine scope for background tasks
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startDiscovery(): ScanningStatus {
        val manager = BleManager.getInstance()
        val scanRuleConfig = BleScanRuleConfig.Builder()
            .setScanTimeOut(100000)
            .build()
        manager.initScanRule(scanRuleConfig)

        if (!isBluetoothAvailable(context)) {
            return ScanningStatus.Disabled
        }

        _isScanning.value = true

        manager.scan(object : BleScanCallback() {
            override fun onScanStarted(success: Boolean) {
                _devices.postValue(mutableListOf())
            }

            @SuppressLint("MissingPermission")
            override fun onScanning(bleDevice: BleDevice) {
                updateDeviceList(DeviceScanResult.Unknown, bleDevice)

                launch {
                    val result = checkBluetoothCompatibilitySuspend(bleDevice)
                    Log.d(TAG, "onScanning: ${bleDevice.name} - ${bleDevice.mac} - $result")
                    updateDeviceList(result, bleDevice)
                }
            }

            override fun onScanFinished(scanResultList: List<BleDevice>) {
                _isScanning.postValue(false)
            }
        })

        return ScanningStatus.Scanning
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun checkBluetoothCompatibilitySuspend(device: BleDevice): DeviceScanResult {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                device.device.connectGatt(context, false, object : BluetoothGattCallback() {
                    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                        Log.d(
                            TAG,
                            "onServicesDiscovered: $status - ${gatt.services.map { it.uuid }}"
                        )
                        val isCompatible =
                            status == BluetoothGatt.GATT_SUCCESS && gatt.services.any { service ->
                                service.uuid == UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
                            }
                        gatt.close()
                        continuation.resume(if (isCompatible) DeviceScanResult.Compatible else DeviceScanResult.Unknown)
                    }

                    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    override fun onConnectionStateChange(
                        gatt: BluetoothGatt,
                        status: Int,
                        newState: Int
                    ) {
                        if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            gatt.close()
                            continuation.resume(DeviceScanResult.Unknown)
                        }
                    }
                })
            }
        }
    }

    private fun updateDeviceList(result: DeviceScanResult, device: BleDevice) {
        val currentList = _devices.value ?: listOf()
        val newDevice = result to device

        // Ensure no duplicates
        if (currentList.none { it.second.device.address == device.device.address }) {
            _devices.postValue(currentList + newDevice)
        } else {
            _devices.postValue(currentList.map { if (it.second.device.address == device.device.address) newDevice else it })
        }
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
                @Suppress("DEPRECATION") // only used for sdk versions lower than tiramisu
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

    companion object {
        private const val TAG = "HeartConnection"
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