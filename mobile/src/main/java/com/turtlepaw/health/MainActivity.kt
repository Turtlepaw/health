package com.turtlepaw.health

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.DefaultFadingTransitions
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavHostGraph
import com.ramcosta.composedestinations.annotation.parameters.CodeGenVisibility
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.SettingsPageDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.turtlepaw.shared.database.SunlightViewModel
import java.time.Duration
import java.time.LocalTime
import java.util.Locale

@NavHostGraph(
    defaultTransitions = DefaultFadingTransitions::class,
    route = "preferred_route",
    visibility = CodeGenVisibility.PUBLIC,
)
annotation class MainGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set flags to hide status bar and navigation bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContent {
            AppTheme {
                val navHostEngine = rememberNavHostEngine()
                val navigator = navHostEngine.rememberNavController()

                DestinationsNavHost(
                    navGraph = NavGraphs.preferredRoute,
                    engine = navHostEngine,
                    navController = navigator
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Destination<MainGraph>(start = true) // HomePage as the starting destination
@Composable
fun HomePage(navigator: DestinationsNavigator) {
    val viewModel = viewModel<SunlightViewModel>()
    val sunlight by viewModel.sunlightData.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    var lastSynced by remember { mutableStateOf<LocalTime?>(null) }
    var sunlightGoal by remember { mutableIntStateOf(0) }
    val context: Context = LocalContext.current

    LaunchedEffect(Unit) {
        // Load data
        val prefs = SharedPrefs(context)
        lastSynced = prefs.getLastSynced()
        sunlightGoal = prefs.getSunlightGoal()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {}, actions = {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.large)
                        .clickable(
                            onClick = {
                                navigator.navigate(SettingsPageDestination) // Navigate to Settings
                            },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple()
                        )
                        .clip(MaterialTheme.shapes.extraLarge)
                        .padding(5.dp)
                )
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
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        progress = {
                            if (sunlight != 0) {
                                sunlight.toFloat() / sunlightGoal.toFloat()
                            } else {
                                0f
                            }
                        },
                        strokeCap = StrokeCap.Round,
                        strokeWidth = 15.dp,
                        modifier = Modifier.size(150.dp)
                    )
                    Icon(
                        painter = painterResource(R.drawable.sunlight),
                        contentDescription = "Sunlight",
                        modifier = Modifier.size(70.dp)
                    )
                }
                Spacer(modifier = Modifier.height(15.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        if (sunlight != 0) sunlight.toString() else "No data",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text("Sunlight", style = MaterialTheme.typography.titleMedium)
                }

                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 15.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 20.dp, horizontal = 25.dp)
                    ) {
                        Row {
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = "Sunlight",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Last Synced", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            if (isLoading == true)
                                "Loading..."
                            else if (lastSynced == null)
                                "Never synced"
                            else formatLocalTimeAsRelativeTime(lastSynced!!).capitalize(Locale.ROOT),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    )
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
