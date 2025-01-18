package com.turtlepaw.health.apps.exercise.presentation.pages.exercise

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.R
import com.turtlepaw.health.apps.exercise.manager.ExerciseViewModel
import com.turtlepaw.health.apps.exercise.manager.HeartRateSource
import com.turtlepaw.health.apps.exercise.presentation.Routes
import com.turtlepaw.health.components.Page
import com.turtlepaw.heart_connection.HeartConnection
import kotlinx.coroutines.launch

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun InExerciseSettings(
    heartConnection: HeartConnection,
    uiState: ExerciseViewModel,
    context: Context,
    navController: NavController,
    id: Int
) {
    Page {
        item {
            Text(text = "Settings")
        }
        item {
            var isLoading by remember { mutableStateOf(false) }
            val source by uiState.heartRateSource.observeAsState()
            val coroutineScope = rememberCoroutineScope()
            Chip(
                enabled = !isLoading,
                label = {
                    Text(text = if (source == HeartRateSource.HeartRateMonitor) "Disconnect sensor" else "Connect sensor")
                },
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        if (source == HeartRateSource.HeartRateMonitor && context.checkSelfPermission(
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            // Drop connection
                            uiState.onRequestDisconnect()
                            heartConnection.disconnect()
                        } else {
                            navController.navigate(Routes.PAIR_DEVICE.getRoute())
                        }
                        isLoading = false
                    }
                },
                icon = {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Icon(
                            painter = painterResource(
                                id =
                                    if (source == HeartRateSource.HeartRateMonitor) R.drawable.bluetooth_disabled else R.drawable.bluetooth_searching
                            ),
                            contentDescription = "Bluetooth"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
            )
        }
        item {
            Chip(
                onClick = { navController.navigate(Routes.METRIC_EDITOR.getRoute(id.toString())) },
                modifier = Modifier
                    .fillMaxWidth(),
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.build),
                        contentDescription = "Build"
                    )
                },
                label = {
                    Text(
                        text = "Metrics"
                    )
                }
            )
        }
    }
}