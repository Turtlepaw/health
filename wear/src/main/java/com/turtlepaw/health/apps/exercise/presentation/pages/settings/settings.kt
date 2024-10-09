package com.turtlepaw.heartconnect.presentation.pages.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.apps.health.presentation.Routes
import com.turtlepaw.health.components.Page
import com.turtlepaw.heartconnect.presentation.theme.ExerciseTheme

@SuppressLint("InlinedApi")
@OptIn(
    ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class,
    ExperimentalPermissionsApi::class
)
@Composable
fun WearSettings(
    context: Context,
    navigate: (route: String) -> Unit,
) {
    ExerciseTheme {
        Page {
            item {
                Text(
                    text = "Settings",
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            item {
                Chip(
                    onClick = { navigate(Routes.GOAL_PICKER.getRoute()) },
                    label = {
                        Text(text = "Step Goal")
                    },
                    icon = {
                        Icon(
                            painter = painterResource(com.turtlepaw.heart_connection.R.drawable.steps),
                            contentDescription = "Step"
                        )
                    }
                )
            }
//                item {
//                    Text(
//                        text = "This app is open-source",
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

fun Context.getActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun SettingsPreview() {
    WearSettings(
        context = LocalContext.current,
        navigate = {}
    )
}