package com.turtlepaw.health.apps.exercise.presentation.pages

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.RadioButton
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import com.clj.fastble.data.BleDevice
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.R
import com.turtlepaw.heart_connection.DeviceScanResult
import com.turtlepaw.heart_connection.HeartConnection
import com.turtlepaw.heart_connection.ScanningStatus
import com.turtlepaw.heartconnect.presentation.theme.ExerciseTheme
import com.turtlepaw.shared.components.Page

data class SelectionItem(
    val id: String,
    val name: String,
    val icon: Int
)

@RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun BluetoothSearch(
    lifecycleOwner: LifecycleOwner,
    heartConnection: HeartConnection,
    selected: String? = null,
    context: Context,
    onSelectionConfirm: (it: BleDevice) -> Unit
) {
    ExerciseTheme {
        val devices = heartConnection.devices.observeAsState()
        val isScanning = heartConnection.isScanning.observeAsState()
        val state = remember { mutableStateOf(ScanningStatus.Idle) }
        DisposableEffect(
            key1 = lifecycleOwner,
            effect = {
                state.value = heartConnection.startDiscovery()
                onDispose {
                    heartConnection.stopDiscovery()
                    state.value = ScanningStatus.Idle
                }
            }
        )

        Page {
            item {}
            if (
                listOf(
                    ScanningStatus.Disabled,
                    ScanningStatus.Unsupported
                ).contains(state.value)
            ) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colors.error,
                            modifier = Modifier
                                .size(36.dp)
                                .padding(bottom = 10.dp)
                        )
                        Text(
                            text = when (state.value) {
                                ScanningStatus.Unsupported -> "Bluetooth isn't supported on your device"
                                ScanningStatus.Disabled -> "Bluetooth isn't enabled on your device"
                                else -> "Unknown scanning state"
                            },
                            textAlign = TextAlign.Center
                        )
                        if (state.value == ScanningStatus.Disabled) {
                            CompactChip(onClick = {
                                val intentOpenBluetoothSettings = Intent()
                                intentOpenBluetoothSettings.setAction(Settings.ACTION_BLUETOOTH_SETTINGS)
                                context.startActivity(intentOpenBluetoothSettings)
                            }, label = {
                                Text(text = "Settings")
                            }, colors = ChipDefaults.secondaryChipColors())
                        }
                    }
                }
            } else {
                val bluetoothDevices = devices.value?.filter { it.second.name != null }
                bluetoothDevices?.let {
                    items(bluetoothDevices.size) {
                        val item = bluetoothDevices.elementAt(it)
                        val isSelected = item.second.mac == selected

                        ToggleChip(
                            checked = isSelected,
                            onCheckedChange = {
                                onSelectionConfirm(item.second)
                            },
                            label = {
                                Text(text = item.second.name)
                            },
                            toggleControl = {
                                RadioButton(selected = isSelected)
                            },
                            appIcon = {
                                Icon(
                                    painter = when (item.first) {
                                        DeviceScanResult.Compatible -> painterResource(id = R.drawable.ecg_heart)
                                        DeviceScanResult.Unknown -> painterResource(id = R.drawable.bluetooth_device)
                                    },
                                    contentDescription = when (item.first) {
                                        DeviceScanResult.Compatible -> "Compatible"
                                        DeviceScanResult.Unknown -> "Unknown"
                                    },
                                    tint = MaterialTheme.colors.onSurfaceVariant
                                )
                            },
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                }
                item {
                    Chip(
                        label = {
                            Text(text = if (isScanning.value == true) "Scanning..." else "Scanning paused")
                        },
                        onClick = {

                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.bluetooth_searching),
                                contentDescription = "Bluetooth Searching"
                            )
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 3.dp)
                    )
                }
            }
        }
    }
}