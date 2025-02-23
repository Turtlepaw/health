package com.turtlepaw.health.apps.sleep.presentation.pages

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.AppCard
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.R
import com.turtlepaw.health.apps.sleep.presentation.LocalDatabase
import com.turtlepaw.shared.components.ErrorPage
import com.turtlepaw.shared.components.Material3Page
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun SessionDetails(sessionId: String?, onDelete: () -> Unit) {
    val database = LocalDatabase.current
    val session by database.sleepDao().getSessionById(sessionId ?: "").collectAsState(null)
    val coroutineScope = rememberCoroutineScope()
    if (session != null) {
        var isDeleteDialogVisible by remember { mutableStateOf(false) }
        AlertDialog(
            visible = isDeleteDialogVisible,
            onDismissRequest = {
                isDeleteDialogVisible = false
            },
            icon = {
                Icon(
                    painterResource(id = R.drawable.delete),
                    contentDescription = "Delete"
                )
            },
            title = {
                Text(
                    "Delete this session?",
                )
            },
//            edgeButton = { AlertDialogDefaults.EdgeButton(onClick = {
//                isDeleteDialogVisible = false
//            }) }
            confirmButton = {
                AlertDialogDefaults.ConfirmButton(
                    onClick = {
                        coroutineScope.launch(Dispatchers.Main) {
                            database.sleepDao().deleteSession(session!!.id)
                            isDeleteDialogVisible = false
                            onDelete()
                        }
                    }
                )
            }
        )
        Material3Page {
            item {
                val formatter = DateTimeFormatter.ofPattern("EEE d")
                Text(
                    formatter.format((session!!.endTime ?: session!!.startTime).toLocalDate()),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            item {
                val formatter = DateTimeFormatter.ofPattern("hh:mm a")
                Text(
                    "${session!!.startTime.format(formatter)} - ${
                        session!!.endTime?.format(
                            formatter
                        ) ?: "Ongoing"
                    }",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }
            item {
                AppCard(
                    onClick = {},
                    enabled = false,
                    title = {},
                    appName = {
                        Text(
                            "Time Asleep"
                        )
                    },
                    appImage = {
                        Icon(
                            imageVector = Icons.Rounded.Hotel,
                            contentDescription = "Hotel",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.height(18.dp)
                        )
                    }
                ) {
                    Text(
                        "${session!!.totalSleepMinutes ?: 0} minutes",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            item {
                AppCard(
                    onClick = {},
                    enabled = false,
                    title = {},
                    appName = {
                        Text(
                            "Average Motion",
                        )
                    },
                    appImage = {
                        Icon(
                            imageVector = Icons.Rounded.Waves,
                            contentDescription = "Waves",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.height(18.dp)
                        )
                    }
                ) {
                    Text(
                        session!!.averageMotion.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            item {
                AppCard(
                    onClick = {},
                    enabled = false,
                    title = {},
                    appName = {
                        Text(
                            "Baseline Heart Rate",
                        )
                    },
                    appImage = {
                        Icon(
                            painterResource(id = R.drawable.ic_vital_signs),
                            contentDescription = "Vital signs",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.height(18.dp)
                        )
                    }
                ) {
                    Text(
                        "${session!!.baselineHeartRate ?: "Unavailable"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        isDeleteDialogVisible = true
                    },
                    label = {
                        Text("Delete")
                    },
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.delete),
                            contentDescription = "Delete"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    } else {
        ErrorPage(
            message = "Session not found"
        )
    }
}