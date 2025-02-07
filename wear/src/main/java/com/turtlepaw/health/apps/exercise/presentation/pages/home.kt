package com.turtlepaw.health.apps.exercise.presentation.pages

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.R
import com.turtlepaw.health.apps.exercise.presentation.Routes
import com.turtlepaw.heartconnect.presentation.theme.ExerciseTheme
import com.turtlepaw.shared.components.Page

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun Home(
    navigate: (route: String) -> Unit,
    heartRate: Int?
) {
    ExerciseTheme {
        Page {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.height(35.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ecg_heart),
                        contentDescription = "ECG Heart",
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier
                            .size(25.dp)
                            .padding(bottom = 3.dp)
                    )
                    Text(
                        text = "Heart Connect",
                        style = MaterialTheme.typography.title2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
            item {
                Text(
                    text = "Track wearable heart rate devices from your watch",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            item {
                Box(modifier = Modifier.padding(bottom = 15.dp)) {
                    if (heartRate != null) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontSize = 36.sp)) {
                                    append(heartRate.toString())
                                }
                                append("bpm")
                            },
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "No heart rate",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = { navigate(Routes.EXERCISE_LIST.getRoute()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.exercise),
                            contentDescription = "Exercise"
                        )
                        Spacer(modifier = Modifier.padding(2.dp))
                        Text(text = "Start Exercise")
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.padding(3.dp))
            }
            item {
                Button(
                    onClick = { navigate(Routes.PAIR_DEVICE.getRoute()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.bluetooth_searching),
                            contentDescription = "Bluetooth Searching"
                        )
                        Spacer(modifier = Modifier.padding(2.dp))
                        Text(text = "Pair new device")
                    }
                }
            }
        }
    }
}