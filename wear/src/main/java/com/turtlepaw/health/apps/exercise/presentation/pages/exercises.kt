package com.turtlepaw.heartconnect.presentation.pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.apps.exercise.presentation.Routes
import com.turtlepaw.heart_connection.Exercises
import com.turtlepaw.heartconnect.presentation.theme.ExerciseTheme
import com.turtlepaw.shared.components.Page

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun ExerciseList(
    navigate: (route: String) -> Unit
) {
    ExerciseTheme {
        Page {
            item {
                Text(
                    text = "Start a workout",
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            items(Exercises.size) {
                val item = Exercises.elementAt(it)

                Chip(
                    label = {
                        Text(text = item.name)
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = "Exercise Icon",
                            tint = MaterialTheme.colors.primary
                        )
                    },
                    onClick = {
                        navigate(
                            Routes.EXERCISE_CONFIGURATION.getRoute(it.toString())
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ChipDefaults.chipColors(
                        backgroundColor = MaterialTheme.colors.surface
                    )
                )
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun ExercisesPreview() {
    ExerciseList {}
}

