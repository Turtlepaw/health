/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.turtlepaw.health.apps.sleep.presentation

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.tooling.preview.devices.WearDevices
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.turtlepaw.health.HealthActivity
import com.turtlepaw.health.apps.sleep.presentation.pages.SessionDetails
import com.turtlepaw.health.apps.sleep.presentation.pages.Sessions
import com.turtlepaw.health.apps.sleep.presentation.pages.TimePicker
import com.turtlepaw.health.apps.sleep.presentation.pages.Tips
import com.turtlepaw.health.apps.sleep.presentation.pages.WearHome
import com.turtlepaw.health.apps.sleep.presentation.pages.settings.AlarmSettings
import com.turtlepaw.health.apps.sleep.presentation.pages.settings.Calibration
import com.turtlepaw.health.apps.sleep.presentation.pages.settings.WearBedtimeSensorSetting
import com.turtlepaw.health.apps.sleep.presentation.pages.settings.WearBedtimeSettings
import com.turtlepaw.health.apps.sleep.presentation.pages.settings.WearSettings
import com.turtlepaw.health.apps.sleep.presentation.theme.SleepTheme
import com.turtlepaw.health.apps.sleep.services.RegisterForPassiveDataWorker
import com.turtlepaw.health.apps.sleep.services.enqueueHealthWorker
import com.turtlepaw.health.apps.sleep.utils.Settings
import com.turtlepaw.health.apps.sleep.utils.SettingsBasics
import com.turtlepaw.shared.database.AppDatabase
import com.turtlepaw.shared.database.BedtimeSensor
import com.turtlepaw.shared.database.SleepDay
import com.turtlepaw.shared.getDefaultSharedSettings
import com.turtlepaw.sleeptools.utils.AlarmType
import com.turtlepaw.sleeptools.utils.AlarmsManager
import com.turtlepaw.sleeptools.utils.TimeManager
import java.time.LocalTime

enum class Routes(private val route: String) {
    HOME("/home"),
    SETTINGS("/settings"),
    SETTINGS_BEDTIME("/settings/bedtime"),
    SETTINGS_TIMEFRAME_START("/settings/bedtime/start"),
    SETTINGS_TIMEFRAME_END("/settings/bedtime/end"),
    SETTINGS_BEDTIME_SENSOR("/settings/bedtime/sensor"),
    TIME_PICKER("/time-picker"),
    ALARM_SETTINGS("/alarm-settings"),
    HISTORY("/history"),
    SESSION_DETAILS("/session-details/{id}"),
    TIPS("/tips"),
    CALIBRATION("/calibration");

    fun getRoute(query: String? = null): String {
        return if (query != null) {
            "$route/$query"
        } else route
    }
}

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SettingsBasics.HISTORY_STORAGE_BASE.getKey())
val LocalDatabase = staticCompositionLocalOf<AppDatabase> {
    error("No database provided")
}

class MainActivity : HealthActivity() {
    private lateinit var database: AppDatabase
    private val tag = "MainSleepActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)
        val sharedPreferences = getDefaultSharedSettings()

        database = AppDatabase.getDatabase(this)

        WorkManager.getInstance(this).enqueueHealthWorker(
            this
        )

        setContent {
            SleepTheme {
                CompositionLocalProvider(
                    LocalDatabase provides database
                ) {
                    AppScaffold {
                        WearPages(
                            sharedPreferences,
                            database,
                            this@MainActivity
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WearPages(
    sharedPreferences: SharedPreferences,
    database: AppDatabase,
    context: Context
) {
    // Creates a navigation controller for our pages
    val navController = rememberSwipeDismissableNavController()
    // Creates a new alarm & time manager
    val timeManager = TimeManager()
    val alarmManager = AlarmsManager()
    // Fetches the wake time from settings
    val wakeTimeString = sharedPreferences.getString(
        Settings.WAKE_TIME.getKey(),
        Settings.WAKE_TIME.getDefault()
    ) // Default to midnight
    // Settings timeframe start
    // Fetches the wake time from settings
    val timeframeStartString = sharedPreferences.getString(
        Settings.BEDTIME_START.getKey(),
        Settings.BEDTIME_START.getDefault()
    )
    val timeframeEndString = sharedPreferences.getString(
        Settings.BEDTIME_END.getKey(),
        Settings.BEDTIME_END.getDefault()
    )
    var useTimeframe = sharedPreferences.getBoolean(
        Settings.BEDTIME_TIMEFRAME.getKey(),
        Settings.BEDTIME_TIMEFRAME.getDefaultAsBoolean()
    )
    val bedtimeStringSensor = sharedPreferences.getString(
        Settings.BEDTIME_SENSOR.getKey(),
        Settings.BEDTIME_SENSOR.getDefault()
    )
    var bedtimeSensor =
        if (bedtimeStringSensor == "BEDTIME") BedtimeSensor.BEDTIME else BedtimeSensor.CHARGING
    // parsed
    var timeframeStart =
        timeManager.parseTime(timeframeStartString, Settings.BEDTIME_START.getDefaultAsLocalTime())
    var timeframeEnd =
        timeManager.parseTime(timeframeEndString, Settings.BEDTIME_END.getDefaultAsLocalTime())
    // Use Alerts - sends alerts when to go to bed
    val useAlertsBool = sharedPreferences.getBoolean(
        Settings.ALERTS.getKey(),
        Settings.ALERTS.getDefaultAsBoolean()
    ) // Default to on
    var useAlerts by remember { mutableStateOf(useAlertsBool) }
    // Fetches the next alarm from android's alarm manager
    val nextAlarm = alarmManager.fetchAlarms(context)
    // Uses Settings.Globals to get bedtime mode
    var bedtimeGoal by remember { mutableStateOf<LocalTime?>(null) }
    // History
    var history by remember { mutableStateOf<List<SleepDay>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    // Parses the wake time and decides if it should use
    // user defined or system defined
    var wakeTime = timeManager.getWakeTime(
        false,
        nextAlarm,
        wakeTimeString,
        Settings.WAKE_TIME.getDefaultAsLocalTime()
    )
    var userWakeTime =
        timeManager.parseTime(wakeTimeString, Settings.WAKE_TIME.getDefaultAsLocalTime())
    // Suspended functions
    rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    LaunchedEffect(key1 = state) {
        Log.d("LaunchedEffectSleep", "Updating (${state})")
        loading = true
        // Get all history (unused)
        // history = database.sleepDao().getHistory()
        // Calculate sleep quality
        bedtimeGoal = timeManager.calculateAvgBedtime(history)
        loading = false
        // Fetch the alarm
        wakeTime = timeManager.getWakeTime(
            false,
            nextAlarm,
            wakeTimeString,
            Settings.WAKE_TIME.getDefaultAsLocalTime()
        )
        userWakeTime =
            timeManager.parseTime(wakeTimeString, Settings.WAKE_TIME.getDefaultAsLocalTime())
    }

    val permission = rememberPermissionState(Manifest.permission.ACTIVITY_RECOGNITION) {
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<RegisterForPassiveDataWorker>().build()
        )
    }

    LaunchedEffect(Unit) {
        permission.launchPermissionRequest()
    }

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = Routes.HOME.getRoute()
    ) {
        composable(Routes.HOME.getRoute()) {
            WearHome(
                navigate = { route ->
                    navController.navigate(route)
                },
                wakeTime,
                nextAlarm = nextAlarm ?: wakeTime.first,
                timeManager,
                bedtimeGoal
            )
        }
        composable(Routes.SETTINGS.getRoute()) {
            WearSettings(
                navigate = { route ->
                    navController.navigate(route)
                },
                openAlarmSettings = {
                    navController.navigate(Routes.ALARM_SETTINGS.getRoute())
                },
                setAlerts = { value ->
                    useAlerts = value
                    sharedPreferences.edit {
                        putBoolean(Settings.ALERTS.getKey(), value)
                    }
                },
                useAlerts,
                context,
                bedtimeGoal
            )
        }
        composable(Routes.ALARM_SETTINGS.getRoute()) {
            AlarmSettings(
                sharedPreferences,
                openAlarmPicker = {
                    navController.navigate(Routes.TIME_PICKER.getRoute())
                }
            )
        }
        composable(Routes.SETTINGS_BEDTIME.getRoute()) {
            WearBedtimeSettings(
                navigator = navController,
                timeframeStart,
                setTimeStart = { value ->
                    timeframeStart = value
                    val editor = sharedPreferences.edit()
                    editor.putString(Settings.BEDTIME_START.getKey(), value.toString())
                    editor.apply()
                },
                timeframeEnd,
                setTimeEnd = { value ->
                    timeframeEnd = value
                    val editor = sharedPreferences.edit()
                    editor.putString(Settings.BEDTIME_END.getKey(), value.toString())
                    editor.apply()
                },
                timeframeEnabled = useTimeframe,
                setTimeframe = { value ->
                    useTimeframe = value
                    val editor = sharedPreferences.edit()
                    editor.putBoolean(Settings.BEDTIME_TIMEFRAME.getKey(), value)
                    editor.apply()
                }
            )
        }
        composable(Routes.SETTINGS_BEDTIME_SENSOR.getRoute()) {
            WearBedtimeSensorSetting(
                navigator = navController,
                sensor = bedtimeSensor,
                setSensor = { value ->
                    bedtimeSensor = value
                    val editor = sharedPreferences.edit()
                    editor.putString(Settings.BEDTIME_SENSOR.getKey(), value.toString())
                    editor.apply()

                    navController.popBackStack()
                }
            )
        }
        composable(Routes.TIME_PICKER.getRoute()) {
            val defaultTime = sharedPreferences.getString(
                Settings.ALARM.getKey(),
                null
            ).run {
                if (this == null) null
                else timeManager.parseTime(this, null)
            }
            TimePicker(
                close = {
                    navController.popBackStack()
                },
                defaultTime ?: LocalTime.of(10, 30),
                setTime = { value ->
                    sharedPreferences.edit {
                        putString(Settings.ALARM.getKey(), value.toString())
                    }
                }
            )
        }
        composable(Routes.SETTINGS_TIMEFRAME_START.getRoute()) {
            TimePicker(
                close = {
                    navController.popBackStack()
                },
                timeframeStart,
                setTime = { value ->
                    timeframeStart = value
                    val editor = sharedPreferences.edit()
                    editor.putString(Settings.BEDTIME_START.getKey(), value.toString())
                    editor.apply()
                }
            )
        }
        composable(Routes.SETTINGS_TIMEFRAME_END.getRoute()) {
            TimePicker(
                close = {
                    navController.popBackStack()
                },
                timeframeEnd,
                setTime = { value ->
                    timeframeEnd = value
                    val editor = sharedPreferences.edit()
                    editor.putString(Settings.BEDTIME_END.getKey(), value.toString())
                    editor.apply()
                }
            )
        }
        composable(Routes.SETTINGS_TIMEFRAME_END.getRoute()) {
            TimePicker(
                close = {
                    navController.popBackStack()
                },
                timeframeEnd,
                setTime = { value ->
                    timeframeEnd = value
                    val editor = sharedPreferences.edit()
                    editor.putString(Settings.BEDTIME_END.getKey(), value.toString())
                    editor.apply()
                }
            )
        }
        composable(Routes.HISTORY.getRoute()) {
            Sessions {
                navController.navigate(Routes.SESSION_DETAILS.getRoute(it))
            }
        }
        composable(Routes.SESSION_DETAILS.getRoute("{id}")) {
            SessionDetails(
                it.arguments?.getString("id")
            ) {
                navController.popBackStack()
            }
        }
//        composable(Routes.DELETE_HISTORY.getRoute("{id}")) {
//            WearHistoryDelete(
//                database,
//                it.arguments?.getString("id")!!,
//                navigation = navController,
//                onDelete = { time ->
//                    val mutated = history.toMutableList()
//                    if (time != "ALL") {
//                        mutated.removeIf { date ->
//                            date?.bedtime?.isEqual(
//                                LocalDateTime.parse(time)
//                            ) == true
//                        }
//                    } else {
//                        mutated.clear()
//                    }
//
//                    history = mutated
//
//                    coroutineScope.launch {
//                        // Re-calculate goal
//                        bedtimeGoal = timeManager.calculateAvgBedtime(history)
//                    }
//                }
//            )
//        }
        composable(Routes.TIPS.getRoute()) {
            var sunlight = sharedPreferences.getInt(
                Settings.SUNLIGHT.getKey(),
                Settings.SUNLIGHT.getDefaultAsInt()
            )
            var lastSleepTime = timeManager.parseDateTimeOrNull(
                sharedPreferences.getString(
                    Settings.LAST_BEDTIME.getKey(),
                    null
                )
            )

            LaunchedEffect(state) {
                sunlight = sharedPreferences.getInt(
                    Settings.SUNLIGHT.getKey(),
                    Settings.SUNLIGHT.getDefaultAsInt()
                )

                lastSleepTime = timeManager.parseDateTimeOrNull(
                    sharedPreferences.getString(
                        Settings.LAST_BEDTIME.getKey(),
                        null
                    )
                )
            }

            Tips(
                context,
                sunlight,
                bedtimeGoal,
                timeManager,
                history.lastOrNull()
            ) {
                navController.popBackStack()
            }
        }
        composable(Routes.CALIBRATION.getRoute()) {
            Calibration()
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearHome(
        navigate = {},
        wakeTime = Pair(
            LocalTime.of(10, 30),
            AlarmType.SYSTEM_ALARM
        ),
        nextAlarm = LocalTime.of(7, 30),
        timeManager = TimeManager(),
        null
    )
}