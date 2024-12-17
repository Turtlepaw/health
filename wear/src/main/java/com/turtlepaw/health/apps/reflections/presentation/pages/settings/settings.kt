package com.turtlepaw.health.apps.reflections.presentation.pages.settings

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.foundation.rememberActiveFocusRequester
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.SwitchDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.scrollAway
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import com.turtlepaw.health.R
import com.turtlepaw.health.apps.health.presentation.Routes
import com.turtlepaw.health.apps.health.presentation.theme.HealthTheme
import com.turtlepaw.health.components.ItemsListWithModifier
import com.turtlepaw.shared.Settings

@OptIn(ExperimentalWearFoundationApi::class, ExperimentalHorologistApi::class)
@Composable
fun WearSettings(
    context: Context,
    navigate: (route: String) -> Unit,
    goal: Int,
    sunlightThreshold: Int,
    isBatterySaver: Boolean,
    goalNotifications: Boolean,
    setGoalNotifications: (state: Boolean) -> Unit,
    setBatterySaver: (state: Boolean) -> Unit
) {
    HealthTheme {
        val focusRequester = rememberActiveFocusRequester()
        val scalingLazyListState = rememberScalingLazyListState()
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
                    space = 4.dp,
                    alignment = Alignment.Top,
                )
            ) {
                item {
                    Text(
                        text = "Settings",
                        modifier = Modifier.padding(bottom = 10.dp, top = 20.dp)
                    )
                }
                item {
                    Button(
                        onClick = {
                            navigate(Routes.GOAL_PICKER.getRoute())
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.flag),
                                contentDescription = "Flag",
                                tint = MaterialTheme.colors.onPrimary,
                                modifier = Modifier
                                    .padding(2.dp)
                            )
                            Spacer(modifier = Modifier.padding(2.dp))
                            Text(
                                text = "Goal",
                                color = MaterialTheme.colors.onPrimary
                            )
                        }
                    }
                }
                item {
                    Button(
                        onClick = {
                            navigate(Routes.SUN_PICKER.getRoute())
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.sunlight),
                                contentDescription = "Sunlight",
                                tint = MaterialTheme.colors.onPrimary,
                                modifier = Modifier
                                    .padding(2.dp)
                            )
                            Spacer(modifier = Modifier.padding(2.dp))
                            Text(
                                text = "Threshold",
                                color = MaterialTheme.colors.onPrimary
                            )
                        }
                    }
                }
                item {
                    Button(
                        onClick = {
                            navigate(Routes.CLOCKWORK.getRoute())
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.build),
                                contentDescription = "Build",
                                tint = MaterialTheme.colors.onPrimary,
                                modifier = Modifier
                                    .padding(2.dp)
                            )
                            Spacer(modifier = Modifier.padding(2.dp))
                            Text(
                                text = "Toolkit",
                                color = MaterialTheme.colors.onPrimary
                            )
                        }
                    }
                }
                item {
                    Button(
                        onClick = {
                            navigate(Routes.STATS.getRoute())
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.bar_chart),
                                contentDescription = "Bar Chart",
                                tint = MaterialTheme.colors.onPrimary,
                                modifier = Modifier
                                    .padding(2.dp)
                            )
                            Spacer(modifier = Modifier.padding(2.dp))
                            Text(
                                text = "Stats",
                                color = MaterialTheme.colors.onPrimary
                            )
                        }
                    }
                }
//                item {
//                    Button(
//                        onClick = {
//                            navigate(Routes.NOTICES.getRoute())
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 10.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            backgroundColor = MaterialTheme.colors.primary
//                        )
//                    ) {
//                        Text(
//                            text = "Notices",
//                            color = Color.Black
//                        )
//                    }
//                }
                item {
                    ToggleChip(
                        modifier = Modifier
                            .fillMaxWidth(),
                        checked = goalNotifications,
                        onCheckedChange = { isEnabled ->
                            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
                                val status = ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                )

                                if (status == PackageManager.PERMISSION_DENIED) {
                                    if (Build.VERSION.SDK_INT >= 33) {
                                        val activity = context.getActivity() ?: return@ToggleChip
                                        ActivityCompat.requestPermissions(
                                            activity,
                                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                            0
                                        )
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Permission required for notifications",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    // Permission is granted, proceed with the action
                                    setGoalNotifications(isEnabled)
                                }
                            }
                        },
                        label = {
                            Text("Goal alerts", overflow = TextOverflow.Visible)
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
                                checked = goalNotifications,
                                enabled = true,
                                modifier = Modifier.semantics {
                                    this.contentDescription =
                                        if (goalNotifications) "On" else "Off"
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colors.primary
                                )
                            )
                        },
                        enabled = true,
                        colors = ToggleChipDefaults.toggleChipColors(
                            checkedEndBackgroundColor = MaterialTheme.colors.secondary
                        )
                    )
                }
                item {
                    Text(
                        text = "This app is open-source",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(
                                top = 10.dp
                            )
                    )
                }
            }
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
        navigate = {},
        goal = Settings.GOAL.getDefaultAsInt(),
        sunlightThreshold = Settings.SUN_THRESHOLD.getDefaultAsInt(),
        isBatterySaver = true,
        goalNotifications = true,
        setGoalNotifications = {},
        setBatterySaver = {}
    )
}