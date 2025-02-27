package com.turtlepaw.health.apps.reflections.presentation.pages

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.R
import com.turtlepaw.shared.database.reflect.ReflectionType

val iconMappings = mapOf(
    ReflectionType.Content to R.drawable.sentiment_neutral,
    ReflectionType.Stressed to R.drawable.sentiment_stressed,
    ReflectionType.Calm to R.drawable.sentiment_calm,
    ReflectionType.Excited to R.drawable.sentiment_excited,
    ReflectionType.Worried to R.drawable.sentiment_worried,
    ReflectionType.Sad to R.drawable.sentiment_sad,
    ReflectionType.Frustrated to R.drawable.sentiment_frustrated,
)

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun Reflect(
    context: Context,
    onConfirm: (reflection: ReflectionType) -> Unit,
    onListView: () -> Unit
) {
    val focusRequester = rememberActiveFocusRequester()
    var reflection = remember { mutableStateOf(ReflectionType.Content) }
    val reflectionTypes = ReflectionType.entries.toTypedArray()
    var accumulatedScroll = remember { mutableStateOf(0f) } // To accumulate scroll events
    val iconSize by animateDpAsState(
        targetValue = 50.dp,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                50.dp at 0 // Start at target value
                30.dp at 1000 with LinearEasing // Go lower slightly after 1 second
                50.dp at 2000 with LinearEasing // Return to target value after 2 seconds
            }
        )
    )

    val threshold = 70 // Define a threshold value (adjust as necessary)

    Column(
        modifier = Modifier
            .onRotaryScrollEvent { event ->
                // Handle rotary scroll events
                val delta = event.verticalScrollPixels // Use verticalScrollPixels for direction
                accumulatedScroll.value += delta

                var vibrate = {
                    val vibrator = getSystemService(context, Vibrator::class.java)
                    vibrator?.let {
                        if (Build.VERSION.SDK_INT >= 26) {
                            it.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                        } else {
                            @Suppress("DEPRECATION")
                            it.vibrate(100)
                        }
                    }
                }
                // Check if accumulated scroll exceeds the threshold
                if (accumulatedScroll.value > threshold) {
                    // Scrolling clockwise
                    val nextIndex =
                        (reflectionTypes.indexOf(reflection.value) + 1) % reflectionTypes.size
                    reflection.value = reflectionTypes[nextIndex]
                    accumulatedScroll.value = 0f // Reset accumulated scroll
                    vibrate()
                } else if (accumulatedScroll.value < -threshold) {
                    // Scrolling counter-clockwise
                    val previousIndex =
                        (reflectionTypes.indexOf(reflection.value) - 1 + reflectionTypes.size) % reflectionTypes.size
                    reflection.value = reflectionTypes[previousIndex]
                    accumulatedScroll.value = 0f // Reset accumulated scroll
                    vibrate()
                }

                true // Indicate that the event has been consumed
            }
            .focusRequester(focusRequester)
            .focusable()
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pass reflection.value to targetState for Crossfade
        Crossfade(
            targetState = reflection.value, label = "crossfade_reflections", animationSpec = tween(
                durationMillis = 400
            )
        ) { selectedReflection ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(
                        iconMappings[selectedReflection] ?: R.drawable.sentiment_neutral
                    ),
                    contentDescription = selectedReflection.displayName,
                    modifier = Modifier
                        .size(iconSize.value.dp)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = selectedReflection.displayName,
                    style = MaterialTheme.typography.title2
                )
            }
        }
        Spacer(Modifier.height(15.dp))
        Row {
            Button(onListView, modifier = Modifier.height(35.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.List,
                    contentDescription = reflection.value.displayName,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(5.dp))

            Button({ onConfirm(reflection.value) }, modifier = Modifier.height(35.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = reflection.value.displayName,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colors.primary.copy(.3f),
                        MaterialTheme.colors.primary.copy(.25f),
                        MaterialTheme.colors.primary.copy(.20f),
                        MaterialTheme.colors.primary.copy(.15f),
                        MaterialTheme.colors.primary.copy(.1f),
                        MaterialTheme.colors.primary.copy(.08f),
                        MaterialTheme.colors.primary.copy(.04f),
                        MaterialTheme.colors.background,
                    ),
                )
            )
            .blur(100.dp)
    )
}
