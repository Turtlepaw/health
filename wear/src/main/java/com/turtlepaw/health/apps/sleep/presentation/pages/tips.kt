package com.turtlepaw.health.apps.sleep.presentation.pages

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TitleCard
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.components.Page
import com.turtlepaw.sleeptools.utils.TimeManager
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(
    ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun Tips(
    context: Context,
    sunlight: Int,
    bedtimeGoal: LocalTime?,
    timeManager: TimeManager,
    lastBedtime: LocalTime?,
    lastSleepTime: LocalDateTime?,
    goBack: () -> Unit
) {
        val formatter = timeManager.getTimeFormatter(false)
        val formatterWithDetails = timeManager.getTimeFormatter()
        var sunlightInstallState by remember { mutableStateOf<Boolean?>(null) }
        DisposableEffect(Unit) {
            sunlightInstallState = context.isPackageInstalled(
                "com.turtlepaw.sunlight"
            )
            onDispose {}
        }

        Page {
                val padding = 5.dp
//                item {
//                    Spacer(
//                        modifier = Modifier.height(20.dp)
//                    )
//                }
                item {
                    Spacer(
                        modifier = Modifier.padding(20.dp)
                    )
                }
                item {
                    TitleCard(
                        onClick = { /*TODO*/ },
                        title = {
                            Text(
                                text = "Sunlight",
                                color = MaterialTheme.colors.primary,
                                style = MaterialTheme.typography.title3
                            )
                        },
                        modifier = Modifier.padding(padding)
                    ) {
                        Text(
                            text = "It's recommended to have 10 to 15 minutes of sunlight every day to improve your sleep",
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        if (sunlightInstallState == true) {
                            Text(
                                text = buildAnnotatedString {
                                    append(
                                        if (sunlight in 10..15)
                                            "You've achieved "
                                        else if (sunlight > 15)
                                            "You went above and beyond with "
                                        else "You currently have "
                                    )
                                    withStyle(style = SpanStyle(color = MaterialTheme.colors.primary)) {
                                        append("$sunlight minutes")
                                    }
                                    append(" of sunlight")
                                },
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }
                    }
                }
                if (sunlightInstallState == false) {
                    item {
                        val sunlightColor = MaterialTheme.colors.primary/*Color(
                                    android.graphics.Color.parseColor(
                                        "#f9b418"
                                    )
                                )*/

                        Card(
                            onClick = {
                                try {
                                    // Create an Intent to open the app's Play Store page
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("market://details?id=com.turtlepaw.sunlight")
                                        )
                                    )
                                } catch (e: ActivityNotFoundException) {
                                    Toast.makeText(
                                        context,
                                        "Failed to launch Play Store",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            backgroundPainter = CardDefaults.cardBackgroundPainter(
                                endBackgroundColor = sunlightColor
                                    .copy(alpha = 0.30f)
                                    .compositeOver(MaterialTheme.colors.background),
                                startBackgroundColor = MaterialTheme.colors.onSurfaceVariant.copy(
                                    alpha = 0.20f
                                ).compositeOver(MaterialTheme.colors.background)
                            ),
                            modifier = Modifier.padding(padding)
                        ) {
                            Text(
                                text = buildAnnotatedString {
                                    append(
                                        "Track your sunlight with "
                                    )
                                    withStyle(style = SpanStyle(color = sunlightColor)) {
                                        append("Sunlight by Beaverfy")
                                    }
                                },
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }


                item {
                    TitleCard(
                        onClick = { /*TODO*/ },
                        title = {
                            Text(
                                text = "Bedtime",
                                color = MaterialTheme.colors.primary,
                                style = MaterialTheme.typography.title3
                            )
                        },
                        modifier = Modifier.padding(padding)
                    ) {
                        Text(
                            text = if (bedtimeGoal != null) "Aim for a bedtime around ${
                                formatterWithDetails.format(
                                    bedtimeGoal
                                )
                            } to be consistent" else "No bedtime estimate, check tomorrow.",
                            textAlign = TextAlign.Start
                            //color = Color(0xFF939AA3)
                        )
                        if (lastBedtime != null && lastSleepTime != null) {
                            val difference = timeManager.calculateTimeDifference(
                                lastBedtime,
                                lastSleepTime.toLocalTime()
                            )
                            Spacer(modifier = Modifier.padding(10.dp))
                            Text(
                                text = buildAnnotatedString {
                                    append("It took you ")
                                    withStyle(style = SpanStyle(color = MaterialTheme.colors.primary)) {
                                        append("${difference.minutes} minutes")
                                    }
                                    append(" to fall asleep last night")
                                },
                            )
                        }
                    }
                }
            }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun TipsPreview() {
    Tips(
        context = LocalContext.current,
        sunlight = 0,
        bedtimeGoal = LocalTime.now(),
        timeManager = TimeManager(),
        LocalTime.of(22, 0),
        LocalDateTime.now()
    ) {

    }
}

fun Context.isPackageInstalled(packageName: String): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}
