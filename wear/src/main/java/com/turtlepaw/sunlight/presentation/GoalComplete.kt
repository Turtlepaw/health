package com.turtlepaw.sunlight.presentation

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.scrollAway
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.turtlepaw.sunlight.presentation.components.ItemsListWithModifier
import com.turtlepaw.sunlight.presentation.theme.SleepTheme
import com.turtlepaw.sunlight.utils.Settings
import com.turtlepaw.sunlight.utils.SettingsBasics
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material.CircularProgressIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GoalCompleteActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)
        val sharedPreferences = getSharedPreferences(
            SettingsBasics.SHARED_PREFERENCES.getKey(),
            SettingsBasics.SHARED_PREFERENCES.getMode()
        )

        setContent {
            WearGoalComplete(
                sharedPreferences.getInt(Settings.GOAL.getKey(), Settings.GOAL.getDefaultAsInt()),
                context = this
            )
        }
    }
}

fun vibrate(context: Context) {
    val vibrator = context.getSystemService(Vibrator::class.java)
    if (vibrator != null && vibrator.hasVibrator()) {
        vibrator.vibrate(
            VibrationEffect.startComposition().addPrimitive(
                VibrationEffect.Composition.PRIMITIVE_SLOW_RISE, 1f
            ).addPrimitive(
                VibrationEffect.Composition.PRIMITIVE_SLOW_RISE, 1f
            ).addPrimitive(
                VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 1f, 5
            ).addPrimitive(
                VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 1f, 5
            ).compose()
        )
    }
}

@Composable
fun WearGoalComplete(
    goal: Int,
    context: Context
) {
    SleepTheme {
        val coroutineScope = rememberCoroutineScope()
        val progress = remember { Animatable(0f) }

        LaunchedEffect(true) {
            vibrate(context)

            coroutineScope.launch {
                progress.animateTo(
                    targetValue = goal.toFloat(),
                    animationSpec = tween(durationMillis = 1000)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxSize(),
                progress = animateFloatAsState(
                    targetValue = progress.value / goal,
                    label = "GoalProgress"
                ).value,
                indicatorColor = MaterialTheme.colors.primary,
                strokeWidth = 10.dp
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = goal.toString() + "m",
                    style = MaterialTheme.typography.display3,
                    color = MaterialTheme.colors.primary
                )
                Spacer(modifier = Modifier.padding(3.dp))
                Text(
                    text = "Goal Complete!"
                )
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun GoalPreview() {
    WearGoalComplete(
        goal = Settings.GOAL.getDefaultAsInt(),
        context = LocalContext.current
    )
}