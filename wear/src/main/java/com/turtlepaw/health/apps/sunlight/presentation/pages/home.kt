package com.turtlepaw.health.apps.sunlight.presentation.pages

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.scrollAway
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.turtlepaw.health.R
import com.turtlepaw.health.apps.health.presentation.Routes
import com.turtlepaw.health.apps.health.presentation.theme.HealthTheme
import com.turtlepaw.health.components.ItemsListWithModifier
import com.turtlepaw.health.utils.Settings
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.defaultShimmerTheme
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun WearHome(
    navigate: (route: String) -> Unit,
    goal: Int,
    today: Int,
    sunlightLx: Float,
    threshold: Int
) {
    HealthTheme {
        val focusRequester = rememberActiveFocusRequester()
        val coroutineScope = rememberCoroutineScope()
        val scalingLazyListState = rememberScalingLazyListState()
        val animatedGoal = remember { Animatable(0f) }

        val infiniteTransition = rememberInfiniteTransition()
        val pulseAnimation by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "Icon Pulse"
        )

        val fadeInAnimation by animateFloatAsState(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )

        LaunchedEffect(today) {
            coroutineScope.launch {
//                animatedGoal.snapTo(
//                    0f
//                )

                animatedGoal.animateTo(
                    targetValue = today.toFloat(),
                    animationSpec = spring(
                        stiffness = Spring.StiffnessHigh,
                        dampingRatio = Spring.DampingRatioNoBouncy
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            TimeText(
                modifier = Modifier.scrollAway(scalingLazyListState)
            )
            PositionIndicator(
                scalingLazyListState = scalingLazyListState
            )
            ItemsListWithModifier(
                modifier = Modifier
                    .rotaryWithScroll(
                        reverseDirection = false,
                        focusRequester = focusRequester,
                        scrollableState = scalingLazyListState,
                    ),
                scrollableState = scalingLazyListState,
                verticalAlignment = Arrangement.spacedBy(
                    space = 0.dp,
                    alignment = Alignment.Top
                ),
            ) {
                item {
                    val size = 95.dp
                    val iconSize = 30.dp

                    Box(
                        modifier = Modifier
                            .size(size)
                            .padding(
                                top = 35.dp,
                            )
                    ) {
                        CircularProgressIndicator(
                            trackColor = MaterialTheme.colors.surface,
                            progress = animateFloatAsState(
                                targetValue = animatedGoal.value / goal.toFloat(),
                                label = "GoalProgress"
                            ).value, // Adjust this value to change the progress
                            modifier = Modifier
                                .size(size)
                        )
                        Icon(
                            painter = painterResource(
                                id = if (today == 0) R.drawable.cloudy_day
                                else R.drawable.sunlight_gold
                            ),
                            contentDescription = if (today == 0)
                                "cloud" else "sunlight",
                            tint = when {
                                today == 0 -> MaterialTheme.colors.primary.copy(0.6f)
                                    .compositeOver(Color.Gray)

                                else -> MaterialTheme.colors.primary
                            },
                            modifier = Modifier
                                .size(iconSize)
                                .align(Alignment.Center)
                                .then(
                                    if (sunlightLx >= threshold)
                                        Modifier.scale(pulseAnimation)
                                    else Modifier
                                )
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.padding(5.dp))
                }
                item {
                    Text(
                        text =
                        if (sunlightLx >= threshold)
                            "Earning Minutes"
                        else "Today",
                        //modifier = Modifier.padding(bottom = 2.dp),
                        color = MaterialTheme.colors.onSurfaceVariant,
                        //fontWeight = FontWeight.W400
                    )
                }
                item {
                    val secondaryColor =
                        if (today == 0) Color.Gray.copy(alpha = 0.8f) else Color.Unspecified.copy(
                            alpha = 0.8f
                        )

                    Box(
                        modifier = Modifier.then(
                            if (sunlightLx >= threshold)
                                Modifier.shimmer(
                                    rememberShimmer(
                                        shimmerBounds = ShimmerBounds.View,
                                        theme = defaultShimmerTheme.copy(
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(
                                                    durationMillis = 800, // Shorter duration for subtle shimmer
                                                    delayMillis = 400, // Less delay for a smoother transition
                                                    easing = LinearEasing,
                                                ),
                                            ),
                                            rotation = 10f, // Subtle rotation to avoid excessive motion
                                            shaderColors = listOf(
                                                secondaryColor,
                                                Color.White,
                                                secondaryColor,
                                            ),
                                            shaderColorStops = null,
                                            shimmerWidth = 100.dp, // Narrow shimmer width for better readability
                                        )
                                    )
                                )
                            else Modifier
                        )
                    ) {
                        Text(
                            text = if (today == 0)
                                buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontSize = 34.sp)) {
                                        append("No data")
                                    }
                                }
                            else buildAnnotatedString {
                                withStyle(style = SpanStyle(fontSize = 34.sp)) {
                                    append("$today")
                                }
                                withStyle(style = SpanStyle(fontSize = 30.sp)) {
                                    append(" min")
                                }
                            },
                            color = when {
                                //today == 0 -> Color.Gray
                                sunlightLx >= threshold -> MaterialTheme.colors.primary
                                else -> MaterialTheme.colors.primary
                            },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.W500,
                            modifier = Modifier.padding(3.dp)
                        )
                    }
                }
                item {
                    Text(
                        text = if (today >= goal)
                            "You've reached your goal"
                        else if (today == 0)
                            "Wear your watch in the sun to earn minutes"
                        else
                            "${abs(today - goal)}m left to your goal",
                        modifier = Modifier.padding(top = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
                item {
                    Spacer(modifier = Modifier.padding(vertical = 5.dp))
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 5.dp
                            ),
//                        horizontalAlignment = Arrangement.spacedBy(
//                            20.dp,
//                            Alignment.CenterHorizontally
//                        )
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
//                        Button(
//                            onClick = {
//                                navigate(
//                                    Routes.HISTORY.getRoute()
//                                )
//                            },
//                            colors = ButtonDefaults.secondaryButtonColors(),
//                            modifier = Modifier
//                                .size(ButtonDefaults.DefaultButtonSize)
//                                //.wrapContentSize(align = Alignment.Center)
//                        ) {
//                            // Icon for history button
//                            Icon(
//                                painter = painterResource(id = R.drawable.history),
//                                contentDescription = "History",
//                                tint = MaterialTheme.colors.primary,
//                                modifier = Modifier
//                                    .padding(2.dp)
//                            )
//                        }

                        Button(
                            onClick = {
                                navigate(
                                    Routes.SETTINGS.getRoute()
                                )
                            },
                            colors = ButtonDefaults.secondaryButtonColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                            //.wrapContentSize(align = Alignment.Center)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(2.dp),
                                verticalAlignment = Alignment.CenterVertically,

                                ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.settings),
                                    tint = MaterialTheme.colors.primary,
                                    contentDescription = "Settings",
                                )
                                Spacer(modifier = Modifier.padding(5.dp))
                                Text(
                                    text = "Settings",
                                    color = MaterialTheme.colors.primary,
                                    style = MaterialTheme.typography.title3
                                )
                            }
                        }

                        Button(
                            onClick = {
                                navigate(
                                    Routes.HISTORY.getRoute()
                                )
                            },
                            colors = ButtonDefaults.secondaryButtonColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                            //.wrapContentSize(align = Alignment.Center)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(2.dp),
                                verticalAlignment = Alignment.CenterVertically,

                                ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.history),
                                    tint = MaterialTheme.colors.primary,
                                    contentDescription = "History",
                                )
                                Spacer(modifier = Modifier.padding(5.dp))
                                Text(
                                    text = "History",
                                    color = MaterialTheme.colors.primary,
                                    style = MaterialTheme.typography.title3
                                )
                            }
                        }
                    }

                }
//                item {
//                    Text(
//                        text = "Made with ☀️ by turtlepaw",
//                        color = Color.White,
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier
//                            .padding(
//                                top = 10.dp
//                            )
//                    )
//                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun EarningMinutes() {
    WearHome(
        navigate = {},
        Settings.GOAL.getDefaultAsInt(),
        2,
        5000f,
        Settings.SUN_THRESHOLD.getDefaultAsInt()
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearHome(
        navigate = {},
        Settings.GOAL.getDefaultAsInt(),
        30,
        2000f,
        Settings.SUN_THRESHOLD.getDefaultAsInt()
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun CloudyDay() {
    WearHome(
        navigate = {},
        Settings.GOAL.getDefaultAsInt(),
        0,
        2000f,
        Settings.SUN_THRESHOLD.getDefaultAsInt()
    )
}