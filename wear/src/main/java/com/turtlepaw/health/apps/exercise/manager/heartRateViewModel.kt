package com.turtlepaw.health.apps.exercise.manager

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.clj.fastble.data.BleDevice
import com.turtlepaw.heart_connection.HeartConnection
import com.turtlepaw.heart_connection.createGattCallback

open class HeartRateModel(application: Application) : AndroidViewModel(application) {
    val _heartRate = MutableLiveData<Int>(null)
    val heartRate: LiveData<Int> get() = _heartRate
    val isConnected = false

    fun connectHeartRateMonitor(context: ComponentActivity, bleDevice: BleDevice) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val connection = HeartConnection(
                createGattCallback {
                    _heartRate.postValue(it)
                },
                context,
                context.application
            )

            val device = bleDevice.device

            connection.connectToDevice(device)
        }
    }
}
