package com.turtlepaw.health.apps.health.presentation.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.apps.health.presentation.Routes
import com.turtlepaw.health.components.Page
import com.turtlepaw.health.database.AppDatabase

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun WearHome(database: AppDatabase, navController: NavController) {
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
            Button(
                onClick = {
                    navController.navigate(Routes.START_COACHING.getRoute())
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Flag,
                        contentDescription = "Flag",
                    )

                    Text(
                        text = "Start Coaching",
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
        AppDatabase.getDatabase(LocalContext.current),
        navController = NavController(LocalContext.current)
    )
}