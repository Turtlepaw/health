package com.turtlepaw.health

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.animations.defaults.DefaultFadingTransitions
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavHostGraph
import com.ramcosta.composedestinations.annotation.parameters.CodeGenVisibility
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.SettingsPageDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.turtlepaw.health.components.SyncButton
import com.turtlepaw.shared.database.AppDatabase
import com.turtlepaw.shared.database.services.ServiceType
import com.turtlepaw.shared.network.ServiceSyncClient
import com.turtlepaw.shared.network.SyncState
import com.turtlepaw.shared.theming.PrimaryServiceColors
import com.turtlepaw.shared.theming.ServiceColors
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit

@NavHostGraph(
    defaultTransitions = DefaultFadingTransitions::class,
    route = "preferred_route",
    visibility = CodeGenVisibility.PUBLIC,
)
annotation class MainGraph

val LocalDatabase = staticCompositionLocalOf<AppDatabase> {
    error("No Database provided")
}

val LocalServiceSyncClient = staticCompositionLocalOf<ServiceSyncClient> {
    error("No ServiceSyncClient provided")
}

object DefaultPageTransitions : NavHostAnimatedDestinationStyle() {
    val animTime = 350 // Increased for more noticeable effect

    // Custom easing curve for pronounced slow->fast->slow
    private val SnapEasing = CubicBezierEasing(0.2f, 0f, 0.2f, 1f)

    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
        {
            slideInHorizontally(
                initialOffsetX = { it / 50 }, // Reduced offset for snappier feel
                animationSpec = tween(animTime, easing = SnapEasing)
            ) + fadeIn(
                animationSpec = tween(animTime / 2) // Faster fade for snappiness
            )
        }

    override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
        {
            slideOutHorizontally(
                targetOffsetX = { -it / 50 },
                animationSpec = tween(animTime, easing = SnapEasing)
            ) + fadeOut(
                animationSpec = tween(animTime / 2)
            )
        }

    override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
        {
            slideInHorizontally(
                initialOffsetX = { -it / 50 },
                animationSpec = tween(animTime, easing = SnapEasing)
            ) + fadeIn(
                animationSpec = tween(animTime / 2)
            )
        }

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
        {
            slideOutHorizontally(
                targetOffsetX = { it / 50 },
                animationSpec = tween(animTime, easing = SnapEasing)
            ) + fadeOut(
                animationSpec = tween(animTime / 2)
            )
        }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        scheduleAutomaticSync()

        setContent {
            val database = remember {
                AppDatabase.getDatabase(this)
            }

            AppTheme {
                val navHostEngine = rememberNavHostEngine()
                val navigator = navHostEngine.rememberNavController()
                val serviceSyncClient = remember {
                    ServiceSyncClient.create(this, database)
                }

                CompositionLocalProvider(
                    LocalDatabase provides database,
                    LocalServiceSyncClient provides serviceSyncClient
                ) {
                    DestinationsNavHost(
                        navGraph = NavGraphs.preferredRoute,
                        engine = navHostEngine,
                        navController = navigator,
                        defaultTransitions = DefaultPageTransitions
                    )
                }
            }
        }
    }

    fun scheduleAutomaticSync() {
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<SyncWorker>(
                repeatInterval = 6,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Destination<MainGraph>(start = true) // HomePage as the starting destination
@Composable
fun HomePage(navigator: DestinationsNavigator) {
    val viewModel = viewModel<MainViewModel>()
    var sunlight by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var lastSynced by remember { mutableStateOf<LocalTime?>(null) }
    var sunlightGoal by remember { mutableIntStateOf(0) }
    val context: Context = LocalContext.current
    val syncState by viewModel.syncState.collectAsState()
    rememberCoroutineScope()
    val database = LocalDatabase.current

    LaunchedEffect(Unit, syncState) {
        // Load data
        val prefs = SharedPrefs(context)
        lastSynced = prefs.getLastSynced()
        sunlightGoal = prefs.getSunlightGoal().plus(1)
        isLoading = false

        val history = database.sunlightDao().getHistory()
        Log.d("History", history.toString())
        val today = database.sunlightDao().getDay(LocalDate.now())
        Log.d("Today", today.toString())
        sunlight = database.sunlightDao().getDay(LocalDate.now())?.value ?: 0
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {}, actions = {
                SyncButton(syncState is SyncState.Syncing)
                Spacer(modifier = Modifier.width(5.dp))

                IconButton(
                    onClick = {
                        navigator.navigate(SettingsPageDestination) // Navigate to Settings
                    }
                ) {
                    Icon(
                        Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            })
        },
        content = { padding ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxWidth()
            ) {
                DataCard(
                    value = sunlight,
                    goal = sunlightGoal,
                    title = "Sunlight",
                    colors = PrimaryServiceColors[ServiceType.Sunlight]!!,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.sunlight),
                        contentDescription = "Sunlight",
                        modifier = Modifier.size(70.dp),
                    )
                }
            }
        }
    )
}

@Composable
fun DataCard(
    value: Int,
    goal: Int,
    title: String,
    colors: ServiceColors,
    icon: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = {
                if (value != 0) {
                    value.toFloat() / goal.toFloat()
                } else {
                    0f
                }
            },
            strokeCap = StrokeCap.Round,
            strokeWidth = 10.dp,
            modifier = Modifier.size(150.dp),
            gapSize = 6.dp,
            color = colors.primary,
            trackColor = if (isSystemInDarkTheme()) colors.trackColor.dark else colors.trackColor.light
        )
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            icon()
        }
    }
    Spacer(modifier = Modifier.height(15.dp))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            if (value != 0) "$value ${if (value <= 1) "minute" else "minutes"}" else "No data",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
    }
}

fun formatLocalTimeAsRelativeTime(localTime: LocalTime): String {
    val now = LocalTime.now()
    val duration = Duration.between(localTime, now)

    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    val seconds = duration.seconds % 60

    val builder = StringBuilder()
    if (hours > 0) {
        builder.append("$hours hours")
    }
    if (minutes > 0) {
        if (builder.isNotEmpty()) builder.append(", ")
        builder.append("$minutes minutes")
    }
    if (seconds > 0 && builder.isEmpty()) { // Show seconds only if other units are 0
        builder.append("$seconds seconds")
    }

    if (builder.isEmpty()) {
        return "just now"
    } else {
        return builder.append(" ago").toString()
    }
}
