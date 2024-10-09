package com.turtlepaw.health.apps.health.presentation.pages

import android.Manifest
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.apps.health.presentation.Routes
import com.turtlepaw.health.components.Page
import com.turtlepaw.health.database.AppDatabase
import com.turtlepaw.health.database.CoachingType
import com.turtlepaw.health.services.scheduleHealthWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun WearHome(context: Context, database: AppDatabase, navController: NavController) {
    var stepStreak by remember { mutableStateOf(null as Int?) }
    var isCoaching by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val permissions = rememberPermissionState(
        Manifest.permission.ACTIVITY_RECOGNITION,
    ) {
        if (it == true) {
            coroutineScope.launch {
                context.scheduleHealthWorker()
                delay(1000)
                stepStreak = database.dayDao().getDays().filter { it.steps >= it.goal }.size
            }
        }
    }

    LaunchedEffect(Unit) {
        if (permissions.status.isGranted) {
            context.scheduleHealthWorker()
            delay(100)
            stepStreak = database.dayDao().getDays().filter { it.steps >= it.goal }.size
            isCoaching = database.coachingProgramDao().getProgram(CoachingType.Sleep.name) != null
        }
    }

    Page {
        item {
            Text(
                text = "Health",
                //modifier = Modifier.padding(bottom = 2.dp),
                color = MaterialTheme.colors.onSurfaceVariant,
                //fontWeight = FontWeight.W400
            )
        }
        item {
            Card(onClick = {
                if (!permissions.status.isGranted) {
                    permissions.launchPermissionRequest()
                }
            }, enabled = !permissions.status.isGranted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(com.turtlepaw.heart_connection.R.drawable.steps),
                        contentDescription = "Step",
                    )

                    Spacer(Modifier.width(5.dp))
                    Text("Step Streak")
                }

                Spacer(Modifier.height(5.dp))

                if (permissions.status.isGranted) {
                    Text("You're on day $stepStreak")
                } else {
                    Text(
                        "Tap to grant permissions and enable.",
                        style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Normal)
                    )
                }
            }
        }
        item {
            Button(
                onClick = {
                    navController.navigate(Routes.START_COACHING.getRoute())
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Flag,
                        contentDescription = "Flag",
                    )

                    Spacer(Modifier.width(5.dp))

                    Text(
                        text = "Start Coaching",
                    )
                }
            }
        }
        item {
            Button(
                onClick = {
                    navController.navigate(Routes.SETTINGS.getRoute())
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                    )

                    Spacer(Modifier.width(5.dp))

                    Text(
                        text = "Settings",
                    )
                }
            }
        }
    }
}

@Preview(
    device = WearDevices.SMALL_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
fun HomePreview() {
    WearHome(
        LocalContext.current,
        AppDatabase.getDatabase(LocalContext.current),
        navController = NavController(LocalContext.current)
    )
}