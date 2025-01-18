/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.turtlepaw.health.apps.exercise.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.currentBackStackEntryAsState
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.ambient.AmbientAware
import com.turtlepaw.health.apps.exercise.manager.ExerciseViewModel
import com.turtlepaw.health.apps.exercise.manager.HeartRateModel
import com.turtlepaw.health.apps.exercise.presentation.pages.BluetoothSearch
import com.turtlepaw.health.apps.exercise.presentation.pages.ExerciseConfiguration
import com.turtlepaw.health.apps.exercise.presentation.pages.ExerciseRoute
import com.turtlepaw.health.apps.exercise.presentation.pages.exercise.InExerciseSettings
import com.turtlepaw.health.apps.exercise.presentation.pages.summary.SummaryRoute
import com.turtlepaw.health.apps.exercise.presentation.pages.summary.SummaryScreenState
import com.turtlepaw.health.apps.exercise.presentation.pages.summary.averageHeartRateArg
import com.turtlepaw.health.apps.exercise.presentation.pages.summary.elapsedTimeArg
import com.turtlepaw.health.apps.exercise.presentation.pages.summary.maxHeartRateArg
import com.turtlepaw.health.apps.exercise.presentation.pages.summary.stepsArg
import com.turtlepaw.health.apps.exercise.presentation.pages.summary.totalCaloriesArg
import com.turtlepaw.health.apps.exercise.presentation.pages.summary.totalDistanceArg
import com.turtlepaw.health.apps.sunlight.presentation.pages.isServiceRunning
import com.turtlepaw.health.components.ErrorPage
import com.turtlepaw.health.services.LightWorker
import com.turtlepaw.heart_connection.Exercises
import com.turtlepaw.heart_connection.HeartConnection
import com.turtlepaw.heart_connection.Metric
import com.turtlepaw.heart_connection.createGattCallback
import com.turtlepaw.heartconnect.presentation.components.LoadingPage
import com.turtlepaw.heartconnect.presentation.pages.AppIntroduction
import com.turtlepaw.heartconnect.presentation.pages.ExerciseList
import com.turtlepaw.heartconnect.presentation.pages.MetricEditor
import com.turtlepaw.heartconnect.presentation.pages.MetricSelector
import com.turtlepaw.heartconnect.presentation.pages.PermissionsIntroduction
import com.turtlepaw.heartconnect.presentation.theme.ExerciseTheme
import com.turtlepaw.shared.Settings
import com.turtlepaw.shared.SettingsBasics
import com.turtlepaw.shared.database.AppDatabase
import com.turtlepaw.shared.database.ServiceType
import com.turtlepaw.shared.database.exercise.Preference
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Routes(private val route: String) {
    HOME("/home"),
    PAIR_DEVICE("/pair-device"),
    APP_INTRODUCTION("/app-introduction"),
    PERMISSIONS_INTRODUCTION("/permissions-introduction"),
    EXERCISE_LIST("/exercise-list"),
    EXERCISE_CONFIGURATION("/exercise-configuration"),
    EXERCISE("/exercise"),
    SUMMARY("/summary"),
    METRIC_EDITOR("/metric/editor"),
    IN_EXERCISE_SETTINGS("/in-exercise-settings");

    fun getRoute(query: String? = null): String {
        return if (query != null) {
            "$route/$query"
        } else route
    }
}

class MainActivity : ComponentActivity() {

    // ViewModel and essential managers
    private val exerciseViewModel: ExerciseViewModel by viewModels()
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle splash screen and permissions
        val splash = installSplashScreen()
        var pendingNavigation = true
        splash.setKeepOnScreenCondition { pendingNavigation }

        val sharedPreferences =
            getSharedPreferences(SettingsBasics.SHARED_PREFERENCES.getKey(), MODE_PRIVATE)
        val dao = AppDatabase.getDatabase(this)

        // UI Content
        setContent {
            navController = rememberSwipeDismissableNavController()
            ExerciseTheme {
                WearPages(this, exerciseViewModel, sharedPreferences, navController, dao)
            }

            // Handle delayed navigation based on whether an exercise session is ongoing
            LaunchedEffect(Unit) {
                prepareIfNoExercise()
                pendingNavigation = false
                //attemptLastDevice(sharedPreferences)
            }
        }
    }

    // Handle deep links
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("Intent", "Got ${intent.action}")
        navController.handleDeepLink(intent)
    }

//    // Handle permissions for Bluetooth and heart rate access
//    private fun attemptLastDevice(sharedPreferences: SharedPreferences): Boolean {
//        val macId = sharedPreferences.getString("default_device", null) ?: return false
//
//        return if (ActivityCompat.checkSelfPermission(
//                this, Manifest.permission.BLUETOOTH_CONNECT
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            exerciseSessionManager.connectDeviceByMac(macId, this)
//            true
//        } else false
//    }

    // If an exercise is in progress, resume the exercise screen, else start a new one
    private suspend fun prepareIfNoExercise() {
        delay(100) // simulate loading
        val isRegularLaunch = navController.currentDestination?.route?.startsWith("/home") == true

        val progress = exerciseViewModel.isExerciseInProgress(this)
        if (isRegularLaunch && progress.first && progress.second != null) {
            val id = Exercises.indexOfFirst { it == progress.second }
            navController.navigate(Routes.EXERCISE.getRoute(id.toString())) {
                popUpTo(Routes.HOME.getRoute()) {
                    inclusive = true
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalHorologistApi::class)
@Composable
fun WearPages(
    context: ComponentActivity,
    exerciseViewModel: ExerciseViewModel,
    sharedPreferences: SharedPreferences,
    navController: NavHostController,
    dao: AppDatabase
) {
    ExerciseTheme {
        // Creates a navigation controller for our pages
        //val navController = rememberSwipeDismissableNavController()
        val coroutineScope = rememberCoroutineScope()
        val heartRateModel: HeartRateModel by context.viewModels()
        //remember { mutableListOf<BluetoothDevice>() }
        val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
        val heartRate by heartRateModel.heartRate.observeAsState(null)
        val heartConnection = HeartConnection(
            createGattCallback {},
            context,
            context.application
        )
        var introComplete by remember {
            mutableStateOf(
                sharedPreferences.getBoolean(
                    Settings.INTRODUCTION_COMPLETE.getKey(),
                    false
                )
            )
        }
        var selectedDevice by remember {
            mutableStateOf(
                sharedPreferences.getString(
                    Settings.DEFAULT_DEVICE.getKey(),
                    Settings.DEFAULT_DEVICE.getDefault()
                )
            )
        }
        val permissionState: MultiplePermissionsState = rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.BODY_SENSORS
            )
        ) {
            //isPermissionsGranted.value = it.all { permission -> permission.value }
//            if(navController.currentDestination?.route == Routes.PERMISSIONS_INTRODUCTION.getRoute())
//                navController.navigate(Routes.HOME.getRoute())
        }

        LaunchedEffect(Unit) {
            val routes = listOf(
                Routes.APP_INTRODUCTION.getRoute(),
                Routes.PERMISSIONS_INTRODUCTION.getRoute()
            )
            if (!introComplete) {
                navController.graph.setStartDestination(Routes.APP_INTRODUCTION.getRoute())
                if (!routes.contains(navController.currentDestination?.route)) navController.navigate(
                    Routes.APP_INTRODUCTION.getRoute()
                ) {
                    popUpTo(Routes.HOME.getRoute()) {
                        inclusive = true
                    }
                }
            }
            if (!permissionState.isAllGranted()) {
                delay(1500)
                if (!routes.contains(navController.currentDestination?.route)) navController.navigate(
                    Routes.PERMISSIONS_INTRODUCTION.getRoute()
                )
            }
        }

//        DisposableEffect(
//            key1 = lifecycleOwner,
//            effect = {
//                val observer = LifecycleEventObserver { _, event ->
//                    if (event == Lifecycle.Event.ON_RESUME) {
//                        //permissionState.launchMultiplePermissionRequest()
//                    }
//                }
//                lifecycleOwner.lifecycle.addObserver(observer)
//                onDispose {
//                    lifecycleOwner.lifecycle.removeObserver(observer)
//                }
//            }
//        )

        val currentScreen by navController.currentBackStackEntryAsState()
        currentScreen?.destination?.route?.startsWith(Routes.EXERCISE.getRoute()) == true
        var swipeToDismissEnabled by remember { mutableStateOf(true) }
        var latestSummary by remember { mutableStateOf<SummaryScreenState?>(null) }

        AmbientAware { ambientStateUpdate ->
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = Routes.HOME.getRoute(),
                userSwipeEnabled = swipeToDismissEnabled
            ) {
                val navigate: (route: String) -> Unit = { navController.navigate(it) }
                composable(Routes.HOME.getRoute()) {
                    val state = permissionState.isAllGranted()
                    if (state == true) {
                        ExerciseList(navigate)
                    } else {
                        LaunchedEffect(Unit) {
                            if (!state) navController.navigate(Routes.APP_INTRODUCTION.getRoute())
                        }

                        ErrorPage(
                            message = "Some permissions have not been granted"
                        )
                    }
                }
                composable(Routes.PAIR_DEVICE.getRoute()) {
                    BluetoothSearch(
                        lifecycleOwner,
                        heartConnection,
                        selectedDevice,
                        context
                    ) {
                        coroutineScope.launch {
                            Log.d("BluetoothSearch", "${it.name} has been selected")
                            sharedPreferences.edit {
                                putString(Settings.DEFAULT_DEVICE.getKey(), it.mac)
                                commit()
                            }
                            selectedDevice = it.mac
                            heartRateModel.connectHeartRateMonitor(context, it)
                            exerciseViewModel?.reconnectHeartRateMonitor()
                            navController.popBackStack()
                        }
                    }
                }
                composable(Routes.APP_INTRODUCTION.getRoute()) {
                    AppIntroduction {
                        sharedPreferences.edit {
                            putBoolean(Settings.INTRODUCTION_COMPLETE.getKey(), true)
                            commit()
                        }
                        introComplete = true
                        navController.navigate(Routes.PERMISSIONS_INTRODUCTION.getRoute())
                    }
                }
                composable(Routes.PERMISSIONS_INTRODUCTION.getRoute()) {
                    PermissionsIntroduction {
                        permissionState.launchMultiplePermissionRequest()
                        navController.graph.setStartDestination(Routes.HOME.getRoute())
                        navController.navigate(Routes.HOME.getRoute()) {
                            popUpTo(Routes.APP_INTRODUCTION.getRoute()) {
                                inclusive = true
                            }
                        }
                    }
                }
                composable(
                    Routes.EXERCISE_CONFIGURATION.getRoute("{id}"),
                    deepLinks = listOf(navDeepLink {
                        uriPattern = "heartconnect://exercise-configuration/{id}"
                    })
                ) {
                    val id = it.arguments?.getString("id")!!.toInt()
                    val exercise = Exercises.elementAt(id)
                    exerciseViewModel

                    ExerciseConfiguration(
                        exercise,
                        navigate,
                        id,
                        context,
                        heartRate,
                        exerciseViewModel,
                        heartRateModel
                    ) {
                        coroutineScope
                        swipeToDismissEnabled = false
                        coroutineScope.launch {
                            launch {
                                val isEnabled = dao.serviceDao()
                                    .getService(ServiceType.SUNLIGHT.serviceName)?.isEnabled == true

                                if (isEnabled && !isServiceRunning(
                                        LightWorker::class.java,
                                        context
                                    )
                                ) {
                                    context.startForegroundService(
                                        Intent(
                                            context,
                                            LightWorker::class.java
                                        )
                                    )
                                }
                            }

                            exerciseViewModel.startExercise(exercise)
                        }
                        navController.navigate(Routes.EXERCISE.getRoute(id.toString())) {
                            popUpTo(Routes.HOME.getRoute()) {
                                inclusive = true
                            }
                        }
                    }
                }
                composable(Routes.EXERCISE.getRoute("{id}")) {
                    val id = it.arguments?.getString("id")!!.toInt()
                    val exercise = Exercises.elementAt(id)
                    var preference by remember { mutableStateOf<Preference?>(null) }

                    LaunchedEffect(exercise) {
                        preference = dao.preferenceDao().getOrInsertPreference(id)
                        //exerciseViewModel.setId(id, context)
                    }

                    if (preference == null) LoadingPage() else {
                        ExerciseRoute(
                            context,
                            preference!!,
                            bluetoothHeartRate = heartRate,
                            ambientState = ambientStateUpdate,
                            onSummary = { summary ->
                                val vibrator = context.getSystemService(Vibrator::class.java)

                                if (vibrator != null && vibrator.hasVibrator()) {
                                    vibrator.vibrate(
                                        VibrationEffect.startComposition().addPrimitive(
                                            VibrationEffect.Composition.PRIMITIVE_SLOW_RISE, 1f
                                        ).addPrimitive(
                                            VibrationEffect.Composition.PRIMITIVE_QUICK_RISE,
                                            1f,
                                            400
                                        ).compose()
                                    )
                                }

                                swipeToDismissEnabled = true
                                latestSummary = summary
                                navController.navigateToTopLevel(
                                    Routes.SUMMARY,
                                    "${Routes.SUMMARY.getRoute()}/${summary.averageHeartRate?.toInt()}/${summary.totalDistance?.toInt()}/${summary.totalCalories?.toInt()}/${summary.elapsedTime.seconds}/${summary.maxHeartRate}/${summary.steps}"
                                )
                            },
                            onRestart = {
                                navController.navigate(Routes.EXERCISE.getRoute(id.toString())) {
                                    popUpTo(Routes.EXERCISE.getRoute(id.toString())) {
                                        inclusive = true
                                    }
                                }
                            },
                            onFinishActivity = {},
                            exerciseViewModel = exerciseViewModel,
                            exercise = exercise,
                            navController = navController
                        )
                    }
                }
                composable(
                    Routes.SUMMARY.getRoute() + "/{averageHeartRate}/{totalDistance}/{totalCalories}/{elapsedTime}/{maxHeartRate}/{steps}",
                    arguments = listOf(
                        navArgument(averageHeartRateArg) { type = NavType.IntType },
                        navArgument(totalDistanceArg) { type = NavType.IntType },
                        navArgument(totalCaloriesArg) { type = NavType.IntType },
                        navArgument(elapsedTimeArg) { type = NavType.IntType },
                        navArgument(maxHeartRateArg) { type = NavType.IntType },
                        navArgument(stepsArg) { type = NavType.LongType }
                    )
                ) {
                    if (latestSummary != null) {
                        SummaryRoute(
                            uiState = latestSummary!!
                        ) {
                            navController.navigateToTopLevel(Routes.HOME)
                        }
                    } else {
                        ErrorPage(message = "Failed to retrieve latest summary", action = {
                            navController.navigateToTopLevel(Routes.HOME)
                        }, actionText = "Home")
                    }
                }
                composable(Routes.METRIC_EDITOR.getRoute("{id}")) {
                    val id = it.arguments?.getString("id")!!.toInt()
                    var preference by remember { mutableStateOf<Preference?>(null) }
                    LaunchedEffect(Unit) {
                        preference = dao.preferenceDao().getOrInsertPreference(id)
                    }

                    if (preference == null) {
                        LoadingPage()
                    } else {
                        MetricEditor(metrics = preference!!.metrics) {
                            navController.navigate(
                                Routes.METRIC_EDITOR.getRoute("${id}/${it}")
                            )
                        }
                    }
                }
                composable(Routes.METRIC_EDITOR.getRoute("{id}/{p}")) {
                    val id = it.arguments?.getString("id")!!.toInt()
                    val position = it.arguments?.getString("p")!!.toInt()
                    val coroutineScope = rememberCoroutineScope()

                    var selectedMetric by remember { mutableStateOf<Metric?>(null) }
                    LaunchedEffect(Unit) {
                        selectedMetric = dao.preferenceDao()
                            .getOrInsertPreference(id).metrics.elementAt(position)
                    }

                    if (selectedMetric == null) {
                        LoadingPage()
                    } else {
                        MetricSelector(
                            position,
                            selectedMetric!!,
                            Exercises.elementAt(id)
                        ) { metric ->
                            selectedMetric = metric

                            coroutineScope.launch {
                                dao.preferenceDao().changeMetric(
                                    id,
                                    position,
                                    metric
                                )
                            }
                        }
                    }
                }
                composable(Routes.IN_EXERCISE_SETTINGS.getRoute("{id}")) {
                    val id = it.arguments?.getString("id")!!.toInt()
                    InExerciseSettings(
                        heartConnection,
                        exerciseViewModel,
                        context,
                        navController,
                        id
                    )
                }
            }
        }
    }
}

fun NavController.navigateToTopLevel(screen: Routes, route: String = screen.getRoute()) {
    navigate(route) {
        popUpTo(graph.id) {
            inclusive = true
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
fun MultiplePermissionsState.isAllGranted(): Boolean {
    Log.d(
        "android.permission",
        "isAllGranted: ${permissions.map { it.status.isGranted to it.permission }}: ${permissions.all { it.status.isGranted == true }}"
    )
    return permissions.all { it.status.isGranted == true }
}