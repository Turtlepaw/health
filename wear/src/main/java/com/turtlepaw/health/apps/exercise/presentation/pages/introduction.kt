package com.turtlepaw.heartconnect.presentation.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.R
import com.turtlepaw.heartconnect.presentation.theme.ExerciseTheme
import com.turtlepaw.shared.components.Page

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun AppIntroduction(
    onComplete: () -> Unit
) {
    ExerciseTheme {
        Page {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ecg_heart),
                        contentDescription = "ECG Heart",
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier
                            .size(35.dp)
                            .padding(bottom = 4.dp)
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
                Button(
                    onClick = { onComplete() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Arrow forward"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun PermissionsIntroduction(
    onRequest: () -> Unit
) {
    ExerciseTheme {
        Page {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.settings_heart),
                        contentDescription = "Settings Heart",
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier
                            .size(35.dp)
                            .padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Permissions",
                        style = MaterialTheme.typography.title2,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
            item {
                Text(
                    text = "Heart Connect uses specific permissions to scan, connect, and stream heart rate data from devices.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            item {
                Button(
                    onClick = { onRequest() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Arrow forward"
                    )
                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun AppIntroPreview() {
    AppIntroduction {}
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun PermissionsIntroPreview() {
    PermissionsIntroduction {}
}