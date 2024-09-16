/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.turtlepaw.health.apps.sunlight.presentation

import android.content.Context
import android.content.Intent
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.turtlepaw.health.apps.sunlight.SunlightViewModel
import com.turtlepaw.health.apps.sunlight.presentation.pages.ClockworkToolkit
import com.turtlepaw.health.apps.sunlight.presentation.pages.History
import com.turtlepaw.health.apps.sunlight.presentation.pages.StatePicker
import com.turtlepaw.health.apps.sunlight.presentation.pages.Stats
import com.turtlepaw.health.apps.sunlight.presentation.pages.WearHome
import com.turtlepaw.health.apps.sunlight.presentation.pages.settings.WearNotices
import com.turtlepaw.health.apps.sunlight.presentation.pages.settings.WearSettings
import com.turtlepaw.health.apps.sunlight.presentation.theme.SunlightTheme
import com.turtlepaw.health.database.AppDatabase
import com.turtlepaw.health.database.SunlightDay
import com.turtlepaw.health.services.SensorReceiver
import com.turtlepaw.health.services.scheduleResetWorker
import com.turtlepaw.health.utils.Settings
import com.turtlepaw.health.utils.SettingsBasics


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
        return if (query != null) {
            "$route/$query"
        } else route
    }
}

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SettingsBasics.HISTORY_STORAGE_BASE.getKey())

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sunlightViewModel: SunlightViewModel

    private lateinit var database: AppDatabase
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

        database = AppDatabase.getDatabase(this)

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
        receiver.startAlarm(this)

        setContent {
            sunlightViewModel = ViewModelProvider(this).get(SunlightViewModel::class.java)

            WearPages(
                sharedPreferences,
                database,
                this,
                sunlightLx.floatValue,
                sunlightViewModel
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
    database: AppDatabase,
    context: Context,
    sunlightLx: Float,
    sunlightViewModel: SunlightViewModel
) {
    SunlightTheme {
        // Creates a navigation controller for our pages
        val navController = rememberSwipeDismissableNavController()
        // Goal - the user's sun daily sun goal
        val goalInt =
            sharedPreferences.getInt(Settings.GOAL.getKey(), Settings.GOAL.getDefaultAsInt())
        val thresholdInt = sharedPreferences.getInt(
            Settings.SUN_THRESHOLD.getKey(),
            Settings.SUN_THRESHOLD.getDefaultAsInt()
        )
        var goal by remember { mutableIntStateOf(goalInt) }
        var threshold by remember { mutableIntStateOf(thresholdInt) }
        // Sunlight
        var sunlightHistory by remember {
            mutableStateOf<
                    List<SunlightDay>>(emptyList())
        }
        //var sunlightToday by //by remember { mutableIntStateOf(0) }
        val sunlightToday by sunlightViewModel.sunlightData.collectAsState()
        // Battery Saver
        val goalNotificatinsRaw = sharedPreferences.getBoolean(
            Settings.GOAL_NOTIFICATIONS.getKey(),
            Settings.GOAL_NOTIFICATIONS.getDefaultAsBoolean()
        )
        val isBatterySaverRaw = sharedPreferences.getBoolean(
            Settings.BATTERY_SAVER.getKey(),
            Settings.BATTERY_SAVER.getDefaultAsBoolean()
        )
        var isBatterySaver by remember { mutableStateOf(isBatterySaverRaw) }
        var goalNotifications by remember { mutableStateOf(goalNotificatinsRaw) }
        var loading by remember { mutableStateOf(true) }
        val lifecycleOwner = LocalLifecycleOwner.current
        val state by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
        // Suspended functions
        LaunchedEffect(state) {
            Log.d("SunlightLifecycle", "Lifecycle state updated: $state (${state.name})")
            //sunlightToday = database.sunlightDao().getDay(LocalDate.now())?.value ?: 0
            sunlightHistory = database.sunlightDao().getHistory()
            loading = false
        }

        /*        LaunchedEffect(Unit) {
                    val handler = Handler(Looper.getMainLooper())
                    // Use a coroutine to run the code on the main thread
        //            while (true) {
        //                // Delay until the next minute
        //                delay(15_000 - (System.currentTimeMillis() % 15_000))
        //
        //                // Update the current sunlight
        //                val today = database.sunlightDao().getDay(LocalDate.now())
        //
        //                // Re-compose the composable
        //                handler.post {
        //                    if (today?.value != null) sunlightToday = today.value
        //                }
        //            }
                }*/

        SwipeDismissableNavHost(
            navController = navController,
            startDestination = Routes.HOME.getRoute()
        ) {
            composable(Routes.HOME.getRoute()) {
                if (loading) {
                    TimeText()
                    CircularProgressIndicator()
                } else {
                    WearHome(
                        navigate = { route ->
                            navController.navigate(route)
                        },
                        goal,
                        sunlightToday,
                        sunlightLx,
                        threshold
                    )
                }
            }
            composable(Routes.SETTINGS.getRoute()) {
                WearSettings(
                    context,
                    navigate = { route ->
                        navController.navigate(route)
                    },
                    goal,
                    threshold,
                    isBatterySaver,
                    goalNotifications,
                    setGoalNotifications = {
                        goalNotifications = it
                        val editor = sharedPreferences.edit()
                        editor.putBoolean(Settings.GOAL_NOTIFICATIONS.getKey(), it)
                        editor.apply()
                    }
                ) { value ->
                    isBatterySaver = value
                    val editor = sharedPreferences.edit()
                    editor.putBoolean(Settings.BATTERY_SAVER.getKey(), value)
                    editor.apply()
                }
            }
            composable(Routes.GOAL_PICKER.getRoute()) {
                StatePicker(
                    List(60) {
                        it.plus(1)
                    },
                    unitOfMeasurement = "m",
                    goal
                ) { value ->
                    goal = value
                    val editor = sharedPreferences.edit()
                    editor.putInt(Settings.GOAL.getKey(), value)
                    editor.apply()
                    // Send the broadcast
                    val intent = Intent("${context.packageName}.GOAL_UPDATED").apply {
                        putExtra("goal", value)
                    }
                    context.sendBroadcast(intent)
                    navController.popBackStack()
                }
            }
            composable(Routes.SUN_PICKER.getRoute()) {
                StatePicker(
                    List(10) {
                        it.times(1000).plus(1000)
                    },
                    unitOfMeasurement = "lx",
                    threshold,
                    recommendedItem = 4
                ) { value ->
                    threshold = value
                    val editor = sharedPreferences.edit()
                    editor.putInt(Settings.SUN_THRESHOLD.getKey(), value)
                    editor.apply()
                    // Send the broadcast
                    val intent = Intent("${context.packageName}.THRESHOLD_UPDATED").apply {
                        putExtra("threshold", value)
                    }
                    context.sendBroadcast(intent)
                    navController.popBackStack()
                }
            }
//            composable(Routes.HISTORY.getRoute()){
//                WearHistory(
//                    goal,
//                    sunlightHistory,
//                    loading
//                )
//            }
            composable(Routes.CLOCKWORK.getRoute()) {
                ClockworkToolkit(
                    light = sunlightLx,
                    context = context,
                    sunlightHistory
                )
            }
            composable(Routes.STATS.getRoute()) {
                Stats(
                    sunlightHistory
                )
            }
            composable(Routes.NOTICES.getRoute()) {
                WearNotices()
            }
            composable(Routes.HISTORY.getRoute()) {
                History(
                    sunlightHistory
                )
            }
        }
    }
}