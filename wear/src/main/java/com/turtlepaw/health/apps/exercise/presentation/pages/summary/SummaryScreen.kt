/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:OptIn(ExperimentalHorologistApi::class)

package com.turtlepaw.health.apps.exercise.presentation.pages.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewSmallRound
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.ItemType
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults.padding
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.google.android.horologist.compose.material.CompactChip
import com.google.android.horologist.compose.material.ListHeaderDefaults.firstItemPadding
import com.google.android.horologist.compose.material.ResponsiveListHeader
import com.google.android.horologist.compose.material.Title
import com.turtlepaw.health.R
import com.turtlepaw.health.apps.exercise.presentation.MapRoute
import com.turtlepaw.health.apps.exercise.presentation.components.CompleteButton
import com.turtlepaw.health.utils.NO_DATA
import com.turtlepaw.health.utils.formatElapsedTimeToString
import com.turtlepaw.heartconnect.presentation.theme.ExerciseTheme
import com.turtlepaw.shared.components.lottie.LottieAnimation
import java.time.Duration
import kotlin.math.roundToInt

/**End-of-workout summary screen**/
@Composable
fun SummaryRoute(
    uiState: SummaryScreenState,
    onCompleteClick: () -> Unit,
) {
    SummaryScreen(uiState = uiState, onCompleteClick = onCompleteClick)
}

@Composable
fun TooShort(onContinueClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        LottieAnimation(R.raw.thinking_face, modifier = Modifier.size(50.dp))
        Spacer(modifier = Modifier.padding(6.dp))
        Text("That was quite short, let's not record that...", textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.padding(2.dp))
        CompactChip(
            onClick = {
                onContinueClick()
            },
            colors = ChipDefaults.secondaryChipColors(),
            label = "Continue"
        )
    }
}

@WearPreviewSmallRound
@Composable
fun TooShortPreview() {
    ExerciseTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(10.dp), contentAlignment = Alignment.Center
        ) {
            TooShort(onContinueClick = {})
        }
    }
}

@Composable
fun SummaryScreen(
    uiState: SummaryScreenState,
    onCompleteClick: () -> Unit,
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = padding(
            first = ItemType.Text,
            last = ItemType.Chip
        )
    )
    val isTooShort = uiState.elapsedTime < Duration.ofSeconds(15)

    if (isTooShort) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp), contentAlignment = Alignment.Center
        ) {
            TooShort {
                onCompleteClick()
            }
        }
    } else {
        ScreenScaffold(scrollState = columnState) {
            ScalingLazyColumn(
                columnState = columnState
            ) {

                item {
                    ResponsiveListHeader(contentPadding = firstItemPadding()) {
                        Title(text = "Workout Complete!")
                    }
                }
                item {
                    CompactStatCard(
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.timer),
                                contentDescription = "Timer"
                            )
                        },
                        content = formatElapsedTimeToString(uiState.elapsedTime)
                    )
                    //formatElapsedTime(uiState.elapsedTime, includeSeconds = true) }
                }
                item {
                    CompactStatCard(
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.calorie),
                                contentDescription = "Calorie"
                            )
                        },
                        content = if (uiState.totalCalories == null) {
                            NO_DATA
                        } else {
                            "${uiState.totalCalories.roundToInt()} cals"
                        }
                    )
                }
                item {
                    CompactStatCard(
                        icon = {
                            Icon(
                                painter = painterResource(id = com.turtlepaw.heart_connection.R.drawable.steps),
                                contentDescription = "Steps"
                            )
                        },
                        content = if (uiState.steps == null) {
                            NO_DATA
                        } else {
                            "${String.format("%,d", uiState.steps)} steps"
                        }
                    )
                }
                item {
                    CompactStatCard(
                        icon = {
                            Icon(
                                painter = painterResource(id = com.turtlepaw.heart_connection.R.drawable.sunlight),
                                contentDescription = "Sunlight"
                            )
                        },
                        content = if (uiState.sunlight == null) {
                            NO_DATA
                        } else if (uiState.sunlight == (-1)) {
                            "Disabled"
                        } else {
                            "${String.format("%,d", uiState.sunlight)} sun min"
                        }
                    )
                }
                item {
                    ResponsiveListHeader(contentColor = MaterialTheme.colors.primary) {
                        Text(text = "Heart Rate")
                    }
                }
                item {
                    CompactStatCard(
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.hr_avg),
                                contentDescription = "Heart"
                            )
                        },
                        content = if (uiState.averageHeartRate == null || uiState.averageHeartRate.isNaN()) {
                            NO_DATA
                        } else {
                            "${uiState.averageHeartRate.roundToInt()} bpm avg"
                        }
                    )
                }
                item {
                    CompactStatCard(
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.max_hr),
                                contentDescription = "Calorie"
                            )
                        },
                        content = if (uiState.maxHeartRate == null) {
                            NO_DATA
                        } else {
                            "${uiState.maxHeartRate.toInt()} max bpm"
                        }
                    )
                }
                item {
                    CompactStatCard(
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.text_compare),
                                contentDescription = "Compare"
                            )
                        }
                    ) {
                        Column {
                            if (uiState.heartRateSimilarity == null) {
                                Text("No data to compare", style = MaterialTheme.typography.body1)
                            } else {
                                val progress = uiState.heartRateSimilarity.toFloat() / 100f
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = String.format(
                                            "%.1f%%",
                                            uiState.heartRateSimilarity
                                        ) + " similarity",
                                        style = MaterialTheme.typography.title3,
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    CircularProgressIndicator(
                                        progress = progress,
                                        strokeWidth = 2.5.dp,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Between external device and current device",
                                style = MaterialTheme.typography.caption3.copy(MaterialTheme.colors.onSurfaceVariant)
                            )
                        }
                    }
                }
                item {
                    ResponsiveListHeader(contentColor = MaterialTheme.colors.primary) {
                        Text(text = "Location")
                    }
                }
                item {
                    MapRoute(coordinates = uiState.locationData)
                }
                item {
                    CompactStatCard(
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.distance),
                                contentDescription = "Distance"
                            )
                        },
                        content = if (uiState.totalDistance == null) {
                            NO_DATA
                        } else {
                            "${"%02.2f".format(uiState.totalDistance / 1_000)} km"
                        }
                    )
                }
                item {
                    Box(modifier = Modifier.padding(top = 5.dp)) {
                        CompleteButton {
                            onCompleteClick()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryFormat(
    value: AnnotatedString,
    metric: String,
    modifier: Modifier = Modifier
) {
    SummaryFormat(metric = metric, modifier) {
        Text(
            textAlign = TextAlign.Center,
            text = value,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.secondary,
            fontSize = 25.sp
        )
    }
}

@Composable
fun StatCard(
    title: String,
    content: @Composable() (ColumnScope.() -> Unit)
) {
    Box(
        modifier = Modifier
            .background(
                MaterialTheme.colors.surface,
                RoundedCornerShape(20.dp)
            )
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(
                vertical = 12.dp,
                horizontal = 15.dp
            )
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.caption2,
                color = MaterialTheme.colors.onBackground.copy(0.8f),
                fontWeight = FontWeight.W500
            )

            Spacer(modifier = Modifier.padding(vertical = 2.dp))

            content()
        }
    }
}

@Composable
fun CompactStatCard(
    icon: @Composable() (RowScope.() -> Unit),
    content: String
) {
    CompactStatCard(icon) {
        Text(
            text = content,
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun CompactStatCard(
    icon: @Composable() (RowScope.() -> Unit),
    content: @Composable() (RowScope.() -> Unit)
) {
    Box(
        modifier = Modifier
            .background(
                MaterialTheme.colors.surface,
                RoundedCornerShape(25.dp)
            )
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(
                vertical = 12.dp,
                horizontal = 15.dp
            ),
            verticalAlignment = Alignment.CenterVertically

        ) {
            icon()
            Spacer(modifier = Modifier.padding(horizontal = 5.dp))
            content()
        }
    }
}

@Composable
fun SummaryFormat(
    metric: String,
    modifier: Modifier = Modifier,
    content: @Composable() (ColumnScope.() -> Unit)
) {
    StatCard(title = metric) {
        content()
//            Row(horizontalArrangement = Arrangement.Center, modifier = modifier) {
//
//            }
//            Row(horizontalArrangement = Arrangement.Center, modifier = modifier) {
//                Text(
//                    textAlign = TextAlign.Center, text = metric, fontSize = 10.sp
//                )
//            }
    }
}

@WearPreviewDevices
@Composable
fun SummaryScreenPreview() {
    SummaryScreen(
        uiState = SummaryScreenState(
            averageHeartRate = 75.0,
            totalDistance = 2000.0,
            totalCalories = 100.0,
            elapsedTime = Duration.ofMinutes(17).plusSeconds(1),
            maxHeartRate = 183,
            heartRateSimilarity = 90.0,
            steps = 1000,
            sunlight = 15,
            heartRate = emptyList(),
            locationData = emptyList()
        ),
        onCompleteClick = {},
    )
}

@Preview()
@Composable
fun Preview() {
    SummaryScreen(
        uiState = SummaryScreenState(
            averageHeartRate = 75.0,
            totalDistance = 2000.0,
            totalCalories = 100.0,
            elapsedTime = Duration.ofMinutes(17).plusSeconds(1),
            maxHeartRate = 186,
            heartRateSimilarity = 90.0,
            steps = 1000,
            sunlight = 15,
            heartRate = emptyList(),
            locationData = emptyList()
        ),
        onCompleteClick = {},
    )
}
