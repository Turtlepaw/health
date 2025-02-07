package com.turtlepaw.heartconnect.presentation.pages

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.RadioButton
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.heart_connection.Exercise
import com.turtlepaw.heart_connection.Metric
import com.turtlepaw.heart_connection.Metrics
import com.turtlepaw.heart_connection.isAvailableFor
import com.turtlepaw.heartconnect.presentation.theme.ExerciseTheme
import com.turtlepaw.shared.components.Page

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun MetricEditor(
    metrics: List<Metric>,
    onNavigate: (position: Int) -> Unit
) {
    ExerciseTheme {
        Page {
            item {
                Text(
                    text = "Metrics",
                    modifier = Modifier.padding(bottom = 5.dp)
                )
            }
            items(4) {
                Chip(
                    label = {
                        Text(text = "Metric ${it.plus(1)}")
                    },
                    onClick = {
                        onNavigate(it)
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = metrics.elementAt(it).icon),
                            contentDescription = "Metric Icon"
                        )
                    },
                    secondaryLabel = {
                        Text(text = metrics.elementAt(it).name ?: "Unselected")
                    },
                    modifier = Modifier
                        .fillMaxSize()
                    //.padding(vertical = (0.5).dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun MetricSelector(
    position: Int,
    selectedMetric: Metric,
    exercise: Exercise,
    onSelect: (metric: Metric) -> Unit
) {
    ExerciseTheme {
        Page {
            item {
                Text(
                    text = "Metric ${position.plus(1)}",
                    modifier = Modifier.padding(bottom = 5.dp)
                )
            }
            items(Metrics.size) {
                val metric = Metrics.elementAt(it)
                Log.d("TAG", "MetricSelector: $metric and exercise is ${exercise.name}")
                ToggleChip(
                    enabled = metric.isAvailableFor(exercise),
                    label = {
                        Text(text = metric.name)
                    },
                    onCheckedChange = { changed ->
                        if (changed) {
                            onSelect(metric)
                        }
                    },
                    appIcon = {
                        Icon(
                            painter = painterResource(id = metric.icon),
                            contentDescription = "Metric Icon"
                        )
                    },
                    toggleControl = {
                        RadioButton(selected = selectedMetric == metric)
                    },
                    checked = selectedMetric == metric,
                    modifier = Modifier
                        .fillMaxSize()
                    //.padding(vertical = (0.5).dp)
                )
            }
        }
    }
}