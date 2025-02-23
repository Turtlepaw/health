package com.turtlepaw.health

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.turtlepaw.shared.network.ClientType
import com.turtlepaw.shared.network.SettingsDataClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class HealthActivity : ComponentActivity() {
    val scope = CoroutineScope(Dispatchers.IO)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsClient = SettingsDataClient.getInstance(this, ClientType.Wear)

        scope.launch {
            settingsClient.syncAllSettings()
        }
    }
}