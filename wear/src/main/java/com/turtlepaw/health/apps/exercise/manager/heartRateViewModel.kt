package com.turtlepaw.health.apps.exercise.manager

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.clj.fastble.data.BleDevice
import com.turtlepaw.heart_connection.HeartConnection
import com.turtlepaw.heart_connection.createGattCallback
import com.turtlepaw.shared.Settings
import com.turtlepaw.shared.SettingsBasics

open class HeartRateModel(application: Application) : AndroidViewModel(application) {
    val _heartRate = MutableLiveData<Int>(null)
    val heartRate: LiveData<Int> get() = _heartRate
    val isConnected = false
    private var heartConnection: HeartConnection? = null

    fun connectHeartRateMonitor(context: ComponentActivity, device: BleDevice) {
        connectHeartRateMonitor(context, device.device)
    }

    fun connectHeartRateMonitor(context: ComponentActivity, device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            heartConnection = HeartConnection(
                createGattCallback {
                    _heartRate.postValue(it)
                },
                context,
                context.application
            )

            heartConnection!!.connectToDevice(device)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @SuppressLint("NullSafeMutableLiveData")
    fun onRequestDisconnect() {
        _heartRate.postValue(null)
        heartConnection?.disconnect()
        heartConnection = null
    }

    fun attemptConnectSaved(context: ComponentActivity) {
        var bluetoothAdapter = context.getSystemService(BluetoothManager::class.java)?.adapter
        val sharedPreferences = context.getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )

        val macId = sharedPreferences.getString(
            Settings.DEFAULT_DEVICE.getKey(),
            Settings.DEFAULT_DEVICE.getDefaultOrNull()
        ) ?: return
        if (macId == "null") return

        val device = bluetoothAdapter?.getRemoteDevice(macId)
            ?: return

        connectHeartRateMonitor(context, device)
    }
}
