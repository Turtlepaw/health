/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.turtlepaw.health.apps.health.presentation

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.turtlepaw.health.apps.health.presentation.pages.WearHome
import com.turtlepaw.health.apps.health.presentation.theme.HealthTheme
import com.turtlepaw.health.services.SensorReceiver
import com.turtlepaw.health.services.scheduleResetWorker
import com.turtlepaw.health.utils.SettingsBasics
import com.turtlepaw.health.utils.SunlightViewModel
import com.turtlepaw.health.utils.SunlightViewModelFactory


enum class Routes(private val route: String) {
    HOME("/home"),
    SETTINGS("/settings"),
    GOAL_PICKER("/goal-picker"),
    SUN_PICKER("/sun-picker"),
    HISTORY("/history"),
    CLOCKWORK("/clockwork-toolkit"),
    NOTICES("/notices"),
    STATS("/stats");

    fun getRoute(query: String? = null): String {
        return if(query != null){
            "$route/$query"
        } else route
    }
}

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SettingsBasics.HISTORY_STORAGE_BASE.getKey())

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sunlightViewModelFactory: MutableState<SunlightViewModelFactory>
    private lateinit var sunlightViewModel: MutableState<SunlightViewModel>
    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private var sunlightLx = mutableFloatStateOf(0f)
    private val tag = "MainSunlightActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)
        val sharedPreferences = getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )

        // Initialize your BedtimeViewModelFactory here
        sunlightViewModelFactory = mutableStateOf(
            SunlightViewModelFactory(dataStore)
        )

        // Use the factory to create the BedtimeViewModel
        sunlightViewModel = mutableStateOf(
            ViewModelProvider(this, sunlightViewModelFactory.value)[SunlightViewModel::class.java]
        )

        // Initialize Sensor Manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
        lightSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)

        scheduleResetWorker()

        // Register Sensor Listener
        sensorManager!!.registerListener(
            this,
            lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        val receiver = SensorReceiver()
        // Start the alarm
        Log.d(tag, "Starting sunlight alarm")
        receiver.startService(this)

        setContent {
            WearPages(
                sharedPreferences,
                sunlightViewModel.value,
                this,
                sunlightLx.floatValue
            )
        }
    }

    override fun onResume() {
        super.onResume()

        // Register Sensor Listener
        sensorManager!!.registerListener(
            this,
            lightSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister Sensor Listener to avoid memory leaks
        sensorManager?.unregisterListener(this)
    }

    override fun onPause() {
        super.onPause()
        // Unregister Sensor Listener to avoid memory leaks
        sensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do nothing
    }

    override fun onSensorChanged(event: SensorEvent) {
        Log.d(tag, "Received light change")
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            val luminance = event.values[0]
            // Check if light intensity surpasses threshold
            sunlightLx.floatValue = luminance
        }
    }
}

@Composable
fun WearPages(
    sharedPreferences: SharedPreferences,
    sunlightViewModel: SunlightViewModel,
    context: Context,
    sunlightLx: Float
){
    HealthTheme {
        // Creates a navigation controller for our pages
        val navController = rememberSwipeDismissableNavController()

        SwipeDismissableNavHost(
            navController = navController,
            startDestination = Routes.HOME.getRoute()
        ) {
            composable(Routes.HOME.getRoute()) {
                WearHome()
            }
        }
    }
}