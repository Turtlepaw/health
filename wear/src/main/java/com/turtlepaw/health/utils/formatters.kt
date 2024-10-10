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
package com.turtlepaw.health.utils

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.turtlepaw.health.apps.exercise.presentation.pages.getSecondaryTextColor
import com.turtlepaw.health.apps.exercise.presentation.pages.getSecondaryTextModifier
import com.turtlepaw.health.apps.exercise.presentation.pages.getSecondaryTextStyle
import com.turtlepaw.health.apps.exercise.presentation.pages.getTextStyle
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

private val MINUTES_PER_HOUR = TimeUnit.HOURS.toMinutes(1)
private val SECONDS_PER_MINUTE = TimeUnit.MINUTES.toSeconds(1)
const val NO_DATA = "- -"

@Composable
fun DataSpacer() {
    Spacer(modifier = Modifier.padding(2.dp))
}

@Composable
fun formatElapsedTime(
    elapsedDuration: Duration?,
    includeSeconds: Boolean = false
) = Row(
    verticalAlignment = Alignment.Bottom
) {
    if (elapsedDuration == null) {
        Text(NO_DATA)
    } else {
        val hours = elapsedDuration.toHours()
        if (hours > 0) {
            Text(
                text = hours.toString(),
                style = getTextStyle()
            )

            Text(
                text = "h",
                style = getSecondaryTextStyle(),
                color = getSecondaryTextColor(),
                modifier = getSecondaryTextModifier()
            )

            DataSpacer()
        }
        val minutes = elapsedDuration.toMinutes() % MINUTES_PER_HOUR

        Text(
            text = "%02d".format(minutes),
            style = getTextStyle()
        )

        Text(
            text = "m",
            style = getSecondaryTextStyle(),
            color = getSecondaryTextColor(),
            modifier = getSecondaryTextModifier()
        )

        if (includeSeconds) {
            val seconds = elapsedDuration.seconds % SECONDS_PER_MINUTE
            DataSpacer()

            Text(
                text = "%02d".format(seconds),
                style = getTextStyle()
            )

            Text(
                text = "s",
                style = getSecondaryTextStyle(),
                color = getSecondaryTextColor(),
                modifier = getSecondaryTextModifier()
            )
        }
    }
}

@Composable
fun formatElapsedTimeToString(
    elapsedDuration: Duration?,
    includeSeconds: Boolean = false
): String {
    return if (elapsedDuration == null) {
        NO_DATA
    } else {
        val _hours = elapsedDuration.toHours()
        val hours = if (_hours < 10)
            "0$_hours"
        else _hours.toString()
        val minutes = elapsedDuration.toMinutes() % MINUTES_PER_HOUR
        val seconds = elapsedDuration.seconds % SECONDS_PER_MINUTE

        var text = "$hours:${"%02d".format(minutes)}.${seconds}"

        if (includeSeconds) text = text.plus("%02d".format(seconds))

        return text
    }
}

fun formatElapsedTime(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.toMinutesPart() // Get minutes within the hour

    return String.format("%d:%02d", hours, minutes)
}

/** Format calories burned to an integer with a "cal" suffix. */
@Composable
fun formatCalories(calories: Double?) = buildAnnotatedString {
    if (calories == null || calories.isNaN()) {
        append(NO_DATA)
    } else {
        append(calories.roundToInt().toString())
        /*        withStyle(style = MaterialTheme.typography.caption3.toSpanStyle()) {
                    append(" cal")
                }*/
    }
}

@Composable
fun formatSteps(steps: Long?) = buildAnnotatedString {
    if (steps == null) {
        append(NO_DATA)
    } else {
        val formattedSteps = AnnotatedString.Builder().apply {
            append(String.format("%,d", steps))
        }.toAnnotatedString()
        append(formattedSteps)
        /*        withStyle(style = MaterialTheme.typography.caption3.toSpanStyle()) {
                    append(" cal")
                }*/
    }
}

/** Format a distance to two decimals with a "km" suffix. */
@Composable
fun formatDistanceKm(meters: Double?) = buildAnnotatedString {
    if (meters == null) {
        append(NO_DATA)
    } else {
        append("%02.2f".format(meters / 1_000))
//        withStyle(
//            style = MaterialTheme.typography.caption3.toSpanStyle()
//        ) {
//            append("km")
//        }
    }
}

/** Format heart rate with a "bpm" suffix. */
@Composable
fun formatHeartRate(bpm: Double?) = buildAnnotatedString {
    if (bpm == null || bpm.isNaN()) {
        append(NO_DATA)
    } else {
        append("%.0f".format(bpm))
        withStyle(style = MaterialTheme.typography.caption3.toSpanStyle()) {
            append("bpm")
        }
    }
}
