package com.turtlepaw.health

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.turtlepaw.shared.database.SunlightViewModel
import java.time.Duration
import java.time.LocalTime
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the content to extend into the system bars (status and navigation bars)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Make the status bar and navigation bar transparent
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false // Adjust depending on your theme (light/dark)
            isAppearanceLightNavigationBars = false
        }

        setContent {
            val sunlightViewModel = ViewModelProvider(this).get(SunlightViewModel::class.java)
            HomePage(
                sunlightViewModel,
                context = this
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(viewModel: SunlightViewModel, context: Context) {
    val sunlight by viewModel.sunlightData.collectAsState()
    var isLoading by remember { mutableStateOf(true) }
    var lastSynced by remember { mutableStateOf<LocalTime?>(null) }
    var sunlightGoal by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val prefs = SharedPrefs(context)
        lastSynced = prefs.getLastSynced()
        sunlightGoal = prefs.getSunlightGoal()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {})
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

@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
    HomePage(
        SunlightViewModel(Application()), Application()
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