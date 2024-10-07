package com.turtlepaw.heartconnect.presentation.pages.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.SwitchDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.R
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
        rememberActiveFocusRequester()
        rememberScalingLazyListState()
        val safetyNotifications = rememberPermissionState(
            Manifest.permission.POST_NOTIFICATIONS
        )
        val color = MaterialTheme.colors.primary
        Page {
            item {
                Text(
                    text = "Settings",
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
            item {
                ToggleChip(
                    modifier = Modifier
                        .fillMaxWidth(),
                    checked = safetyNotifications.status.isGranted,
                    onCheckedChange = { isEnabled ->
                        if (!isEnabled) {
                            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
                                safetyNotifications.launchPermissionRequest()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Permission already granted",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    label = {
                        Text("Warnings", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    appIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.alert),
                            contentDescription = "goal notifications",
                            modifier = Modifier
                                .size(24.dp)
                                .wrapContentSize(align = Alignment.Center),
                        )
                    },
                    toggleControl = {
                        Switch(
                            checked = safetyNotifications.status.isGranted,
                            enabled = true,
                            modifier = Modifier.semantics {
                                this.contentDescription =
                                    if (safetyNotifications.status.isGranted) "On" else "Off"
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = color
                            )
                        )
                    },
                    enabled = true,
                    colors = ToggleChipDefaults.toggleChipColors(
                        checkedEndBackgroundColor = MaterialTheme.colors.surface
                    )
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