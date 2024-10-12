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
@file:OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)

package com.turtlepaw.health.apps.exercise.presentation.pages

import android.app.Application
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.ArcPaddingValues
import androidx.wear.compose.foundation.CurvedDirection
import androidx.wear.compose.foundation.CurvedLayout
import androidx.wear.compose.foundation.CurvedModifier
import androidx.wear.compose.foundation.CurvedTextStyle
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.curvedRow
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.padding
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PageIndicatorState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.curvedText
import androidx.wear.compose.material.scrollAway
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.composables.ProgressIndicatorSegment
import com.google.android.horologist.composables.SegmentedProgressIndicator
import com.google.android.horologist.compose.ambient.AmbientState
import com.google.android.horologist.compose.material.AlertDialog
import com.google.android.horologist.compose.pager.VerticalPagerScreen
import com.google.android.horologist.health.composables.ActiveDurationText
import com.turtlepaw.health.R
import com.turtlepaw.health.apps.exercise.manager.ExerciseViewModel
import com.turtlepaw.health.apps.exercise.manager.FakeExerciseViewModel
import com.turtlepaw.health.apps.exercise.manager.HeartRateSource
import com.turtlepaw.health.apps.exercise.presentation.components.EndButton
import com.turtlepaw.health.apps.exercise.presentation.components.PauseButton
import com.turtlepaw.health.apps.exercise.presentation.components.StartButton
import com.turtlepaw.health.apps.exercise.presentation.pages.summary.SummaryScreenState
import com.turtlepaw.health.database.exercise.Preference
import com.turtlepaw.health.utils.NO_DATA
import com.turtlepaw.health.utils.formatCalories
import com.turtlepaw.health.utils.formatDistanceKm
import com.turtlepaw.health.utils.formatElapsedTime
import com.turtlepaw.health.utils.formatSteps
import com.turtlepaw.health.utils.formatSunlight
import com.turtlepaw.heart_connection.CaloriesMetric
import com.turtlepaw.heart_connection.DistanceMetric
import com.turtlepaw.heart_connection.ElapsedTimeMetric
import com.turtlepaw.heart_connection.Exercises
import com.turtlepaw.heart_connection.HeartRateMetric
import com.turtlepaw.heart_connection.StepsMetric
import com.turtlepaw.heart_connection.SunlightMetric
import com.turtlepaw.heartconnect.presentation.theme.ExerciseTheme
import kotlinx.coroutines.launch

@Composable
fun ExerciseRoute(
    context: Context,
    preference: Preference,
    bluetoothHeartRate: Int?,
    ambientState: AmbientState,
    modifier: Modifier = Modifier,
    onSummary: (SummaryScreenState) -> Unit,
    onRestart: () -> Unit,
    onFinishActivity: () -> Unit,
    exerciseViewModel: ExerciseViewModel
) {
    val coroutineScope = rememberCoroutineScope()
//    val isEnded by exerciseViewModel.isEnded.observeAsState(false)
//    if (isEnded) {
//        SideEffect {
//            onSummary(exerciseViewModel.toSummary())
//        }
//    }

    if (exerciseViewModel.error != null) {
        ErrorStartingExerciseScreen(
            onRestart = onRestart,
            onFinishActivity = onFinishActivity,
            exerciseViewModel = exerciseViewModel
        )
    } else if (ambientState is AmbientState.Interactive) {
        Exercise(
            preference = preference,
            bluetoothHeartRate = bluetoothHeartRate,
            onPauseClick = {
                coroutineScope.launch {
                    exerciseViewModel.pauseExercise()
                    val vibrator = context.getSystemService(Vibrator::class.java)

                    if (vibrator != null && vibrator.hasVibrator()) {
                        vibrator.vibrate(
                            VibrationEffect.startComposition().addPrimitive(
                                VibrationEffect.Composition.PRIMITIVE_CLICK, 1f
                            ).compose()
                        )
                    }
                }
            },
            onEndClick = {
                coroutineScope.launch {
                    exerciseViewModel.stopExercise()
                    onSummary(exerciseViewModel.toSummary())
                }
            },
            onResumeClick = {
                coroutineScope.launch {
                    exerciseViewModel.resumeExercise()
                    val vibrator = context.getSystemService(Vibrator::class.java)

                    if (vibrator != null && vibrator.hasVibrator()) {
                        vibrator.vibrate(
                            VibrationEffect.startComposition().addPrimitive(
                                VibrationEffect.Composition.PRIMITIVE_CLICK, 1f
                            ).compose()
                        )
                    }
                }
            },
            onStartClick = {
                coroutineScope.launch {
                    exerciseViewModel.resumeExercise()
                }
            },
            uiState = exerciseViewModel,
            modifier = modifier
        )
    }
}

/**
 * Shows an error that occured when starting an exercise
 */
@Composable
fun ErrorStartingExerciseScreen(
    onRestart: () -> Unit,
    onFinishActivity: () -> Unit,
    exerciseViewModel: ExerciseViewModel
) {
    AlertDialog(
        title = "Failed to start exercise",
        message = "${exerciseViewModel.error ?: "Unknown error"}, try again later.",
        onCancel = onFinishActivity,
        onOk = onRestart,
        showDialog = true,
    )
}

private fun PaddingValues.toArcPadding() = object : ArcPaddingValues {
    override fun calculateOuterPadding(radialDirection: CurvedDirection.Radial) =
        calculateTopPadding()

    override fun calculateInnerPadding(radialDirection: CurvedDirection.Radial) =
        calculateBottomPadding()

    override fun calculateAfterPadding(
        layoutDirection: LayoutDirection,
        angularDirection: CurvedDirection.Angular
    ) = calculateRightPadding(layoutDirection)

    override fun calculateBeforePadding(
        layoutDirection: LayoutDirection,
        angularDirection: CurvedDirection.Angular
    ) = calculateLeftPadding(layoutDirection)
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun Exercise(
    preference: Preference,
    bluetoothHeartRate: Int?,
    onPauseClick: () -> Unit,
    onEndClick: () -> Unit,
    onResumeClick: () -> Unit,
    onStartClick: () -> Unit,
    uiState: ExerciseViewModel,
    modifier: Modifier = Modifier
) {
    ExerciseTheme {
        //val focusRequester = rememberActiveFocusRequester()
        val scalingLazyListState = rememberScalingLazyListState()
        val maxPages = 2
        var selectedPage by remember { mutableStateOf(0) }
        var finalValue by remember { mutableStateOf(0) }
        var pagerState = rememberPagerState {
            maxPages
        }

        val animatedSelectedPage by animateFloatAsState(
            targetValue = selectedPage.toFloat(),
        ) {
            finalValue = it.toInt()
        }

        val MAX_HEART_RATE = 150
        remember {
            object : PageIndicatorState {
                override val pageOffset: Float
                    get() = animatedSelectedPage - finalValue
                override val selectedPage: Int
                    get() = finalValue
                override val pageCount: Int
                    get() = maxPages
            }
        }

        val segmentColor =
            if (bluetoothHeartRate == null && uiState.heartRate == null) MaterialTheme.colors.surface else null
        val defaultWeight = 0.1f
        val spacerWeight = 0.01f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            VerticalPagerScreen(
                state = pagerState
            ) {
                if (it == 0) {
                    val heartRate by uiState.heartRate.observeAsState()
                    val progress by animateFloatAsState(
                        (heartRate
                            ?: 0).toFloat() / MAX_HEART_RATE.toFloat(),
                        animationSpec = tween(250)
                    )
                    SegmentedProgressIndicator(
                        trackColor = Color.Transparent,
                        trackSegments = listOf(
                            ProgressIndicatorSegment(
                                defaultWeight,
                                segmentColor ?: MaterialTheme.colors.primary,
                                trackColor = MaterialTheme.colors.surface
                            ),
                            ProgressIndicatorSegment(
                                spacerWeight,
                                Color.Transparent
                            ),
                            ProgressIndicatorSegment(
                                defaultWeight,
                                segmentColor ?: MaterialTheme.colors.primary,
                                trackColor = MaterialTheme.colors.surface
                            ),
                            ProgressIndicatorSegment(
                                spacerWeight,
                                Color.Transparent
                            ),
                            ProgressIndicatorSegment(
                                defaultWeight,
                                segmentColor ?: MaterialTheme.colors.error,
                                trackColor = MaterialTheme.colors.surface
                            ),
                            ProgressIndicatorSegment(
                                spacerWeight,
                                Color.Transparent
                            ),
                            ProgressIndicatorSegment(
                                defaultWeight,
                                segmentColor ?: MaterialTheme.colors.error,
                                trackColor = MaterialTheme.colors.surface
                            )
                        ),
                        progress = progress,
                        startAngle = 130f,
                        endAngle = 230f,
                        strokeWidth = 9.dp,
                        modifier = Modifier.padding(
                            top = 5.dp,
                            bottom = 5.dp
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            preference.metrics.map { m ->
                                when (m) {
                                    ElapsedTimeMetric -> DurationRow(uiState)
                                    DistanceMetric -> DistanceRow(uiState)
                                    HeartRateMetric -> HeartRateRow(uiState, bluetoothHeartRate)
                                    CaloriesMetric -> CalorieRow(uiState)
                                    StepsMetric -> StepsRow(uiState)
                                    SunlightMetric -> SunlightRow(uiState)
                                }
                            }
                        }
                    }
                } else if (it == 1) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column {
                            ExerciseControlButtons(
                                bluetoothHeartRate != null,
                                uiState,
                                onStartClick,
                                onEndClick,
                                onResumeClick,
                                onPauseClick
                            )
                        }
                    }
                }
            }
//            ItemsListWithModifier(
//                modifier = Modifier
//                    .rotaryWithScroll(
//                        reverseDirection = false,
//                        focusRequester = focusRequester,
//                        scrollableState = scalingLazyListState,
//                    ),
//                scrollableState = scalingLazyListState,
//                verticalAlignment = Arrangement.spacedBy(
//                    space = 0.dp,
//                    alignment = Alignment.Top,
//                ),
//            ) {
//                item {
//                    DurationRow(uiState)
//                }
//
//                item {
//                    HeartRateRow(uiState, bluetoothHeartRate)
//                }
//
//                item {
//                    DistanceRow(uiState)
//                }
//
//                item {
//                    CalorieRow(uiState)
//                }
//
//                item {
//                    ExerciseControlButtons(
//                        uiState,
//                        onStartClick,
//                        onEndClick,
//                        onResumeClick,
//                        onPauseClick
//                    )
//                }
//            }
            val isPaused by uiState.isPaused.observeAsState(false)
            if (isPaused) {
                val infiniteTransition = rememberInfiniteTransition()
                val alpha = infiniteTransition.animateFloat(
                    initialValue = 0.7f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 800,
                            easing = LinearEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                val contentPadding = TimeTextDefaults.ContentPadding
                val timeTextStyle = TimeTextDefaults.timeTextStyle().copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal
                )
                val color = MaterialTheme.colors.secondary

                CurvedLayout(modifier = Modifier.alpha(alpha.value)) {
                    curvedRow(modifier = CurvedModifier.padding(contentPadding.toArcPadding())) {
                        curvedText(
                            text = "Paused",
                            style = CurvedTextStyle(timeTextStyle),
                            color = color
                        )
                    }
                }
            } else {
                TimeText(
                    modifier = Modifier.scrollAway(scalingLazyListState),
                )
            }
            PositionIndicator(
                scalingLazyListState = scalingLazyListState
            )
            /*VerticalPageIndicator(
                pageIndicatorState = pageIndicatorState
            )*/
            //Vignette(vignettePosition = VignettePosition.TopAndBottom)
        }
    }
}

@Composable
private fun ExerciseControlButtons(
    isConnectedToBluetooth: Boolean,
    uiState: ExerciseViewModel,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    onResumeClick: () -> Unit,
    onPauseClick: () -> Unit
) {
    val source by uiState.heartRateSource.observeAsState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        if (source == HeartRateSource.HeartRateMonitor) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(modifier = Modifier.padding(end = 5.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.bluetooth_searching),
                        contentDescription = "Bluetooth Connected",
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier
                            .size(17.dp)
                    )
                }
                Text(
                    text = "Connected"
                    //overflow = TextOverflow.Visible
                )
            }
        }

        val isPaused by uiState.isPaused.observeAsState(false)
        val isEnding by uiState.isEnding.observeAsState(false)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EndButton(isEnding, onEndClick)

                Text(
                    text = if (isEnding) "Ending" else "End",
                    modifier = Modifier
                        .padding(top = 5.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isPaused) {
                    StartButton(onClick = onResumeClick)
                } else {
                    PauseButton(onPauseClick)
                }

                Text(
                    text = if (isPaused) "Resume" else "Pause",
                    modifier = Modifier
                        .padding(top = 5.dp)
                )
            }
        }
    }
}

@Composable
fun getTextStyle(): TextStyle {
    return MaterialTheme.typography.title1.copy(
        fontWeight = FontWeight.Normal
    )
}

@Composable
fun getIconModifier(): Modifier {
    val fontSize: TextUnit = getTextStyle().fontSize
    val lineHeightDp: Dp = with(LocalDensity.current) {
        fontSize.toDp()
    }
    return Modifier
        .size(lineHeightDp + 4.dp)
        .padding(end = 5.dp)
}

@Composable
fun getDataRowModifier(): Modifier {
    val fontSize: TextUnit = getTextStyle().fontSize
    val lineHeightDp: Dp = with(LocalDensity.current) {
        fontSize.toDp()
    }
    return Modifier
        .height(lineHeightDp + 4.dp)
        .padding(end = 5.dp)
}

@Composable
fun getSecondaryTextColor(): Color {
    return MaterialTheme.colors.onBackground.copy(0.75f)
}

@Composable
fun getSecondaryTextModifier(): Modifier {
    return Modifier.padding(
        start = 3.dp,
        bottom = 2.dp
    )
}

@Composable
fun getSecondaryTextStyle(): TextStyle {
    return MaterialTheme.typography.caption2
}

@Composable
fun DataRow(content: @Composable() (RowScope.() -> Unit)) {
    Row(
        modifier = getDataRowModifier()
            .fillMaxWidth()
            .padding(start = 30.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
        content = content
    )
}

@Composable
private fun DistanceRow(uiState: ExerciseViewModel) {
    val distance by uiState.distance.observeAsState()
    DataRow {
        Icon(
            painter = painterResource(id = R.drawable.distance),
            contentDescription = "Distance",
            modifier = getIconModifier()
        )
        Text(
            text = formatDistanceKm(meters = distance),
            style = getTextStyle()
        )

        Text(
            text = "km",
            color = getSecondaryTextColor(),
            modifier = getSecondaryTextModifier(),
            style = getSecondaryTextStyle()
        )

//        Row {
//            Icon(
//                painter = painterResource(id = R.drawable.laps),
//                contentDescription = "Laps"
//            )
//            Text(text = uiState.exerciseState?.exerciseLaps?.toString() ?: NO_DATA)
//        }
    }
}

@Composable
private fun HeartRateRow(
    uiState: ExerciseViewModel,
    bluetoothHeartRate: Int?
) {
    val heartRate by uiState.heartRate.observeAsState()
    val source by uiState.heartRateSource.observeAsState()
    DataRow {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.heart),
                contentDescription = "Heart",
                modifier = getIconModifier()
            )
            Text(
                text = "${heartRate ?: NO_DATA}",
                //color = if (bluetoothHeartRate != null) MaterialTheme.colors.primary else Color.White,
                style = getTextStyle()
            )

            if (source == HeartRateSource.HeartRateMonitor) {
                Box(modifier = Modifier.padding(start = 5.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.bluetooth_searching),
                        contentDescription = "Bluetooth Connected",
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier
                            .size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalorieRow(uiState: ExerciseViewModel) {
    val calories by uiState.calories.observeAsState()
    DataRow {
        Icon(
            painter = painterResource(id = R.drawable.calorie),
            contentDescription = "Fire",
            modifier = getIconModifier()
        )
        Text(
            text = if (calories == null)
                NO_DATA else
                formatCalories(calories).toString(),
            style = getTextStyle()
        )

        Text(
            text = "cals",
            color = getSecondaryTextColor(),
            modifier = getSecondaryTextModifier(),
            style = getSecondaryTextStyle()
        )
    }
}

@Composable
private fun StepsRow(uiState: ExerciseViewModel) {
    val steps by uiState.steps.observeAsState()
    DataRow {
        Icon(
            painter = painterResource(id = com.turtlepaw.heart_connection.R.drawable.steps),
            contentDescription = "Step",
            modifier = getIconModifier()
        )
        Text(
            text = if (steps == null)
                NO_DATA else
                formatSteps(steps).toString(),
            style = getTextStyle()
        )

        Text(
            text = "steps",
            color = getSecondaryTextColor(),
            modifier = getSecondaryTextModifier(),
            style = getSecondaryTextStyle()
        )
    }
}

@Composable
private fun SunlightRow(uiState: ExerciseViewModel) {
    val sunlight by uiState.sunlightData.observeAsState()
    DataRow {
        Icon(
            painter = painterResource(id = com.turtlepaw.heart_connection.R.drawable.sunlight),
            contentDescription = "Sunlight",
            modifier = getIconModifier()
        )
        Text(
            text = if (sunlight == null)
                NO_DATA else if (sunlight == (-1)) "Disabled" else
                formatSunlight(sunlight!!).toString(),
            style = getTextStyle()
        )

        Text(
            text = "sunlight min",
            color = getSecondaryTextColor(),
            modifier = getSecondaryTextModifier(),
            style = getSecondaryTextStyle()
        )
    }
}

@Composable
private fun DurationRow(uiState: ExerciseViewModel) {
    val rawState by uiState.rawState.observeAsState()
    val lastActiveDurationCheckpoint = rawState?.activeDurationCheckpoint
    val exerciseState = rawState?.exerciseStateInfo?.state
    DataRow {
        Icon(
            painter = painterResource(id = R.drawable.timer),
            contentDescription = "Timer",
            modifier = getIconModifier()
        )
        if (exerciseState != null && lastActiveDurationCheckpoint != null) {
            ActiveDurationText(
                checkpoint = lastActiveDurationCheckpoint,
                state = exerciseState
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    formatElapsedTime(it, includeSeconds = true)
                }
            }
        } else {
            Text(text = NO_DATA, style = getTextStyle())
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun ExercisePreview() {
    Exercise(
        preference = Preference(
            id = 0,
            metrics = Exercises.first().defaultMetrics
        ),
        86,
        onPauseClick = {},
        onEndClick = {},
        onResumeClick = {},
        onStartClick = {},
        uiState = ExerciseViewModel(Application()),
    )
}


@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun FullExercisePreview() {
    Exercise(
        preference = Preference(
            id = 0,
            metrics = Exercises.first().defaultMetrics
        ),
        150,
        onPauseClick = {},
        onEndClick = {},
        onResumeClick = {},
        onStartClick = {},
        uiState = FakeExerciseViewModel(Application()),
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun ExerciseControls() {
    ExerciseTheme {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colors.background)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column {
                ExerciseControlButtons(
                    true,
                    FakeExerciseViewModel(Application()),
                    {},
                    {},
                    {},
                    {}
                )
            }
        }
    }
}
