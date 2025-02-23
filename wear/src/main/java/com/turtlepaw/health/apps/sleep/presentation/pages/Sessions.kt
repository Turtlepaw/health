package com.turtlepaw.health.apps.sleep.presentation.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.AppCard
import androidx.wear.compose.material3.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.apps.sleep.presentation.LocalDatabase
import com.turtlepaw.shared.components.Material3Page
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun Sessions(
    onSessionClick: (id: String) -> Unit
) {
    val database = LocalDatabase.current
    val sessions by database.sleepDao().getAllSessions().collectAsState(emptyList())
    Material3Page {
        item {
            Text(text = "${sessions.size} Sessions")
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(sessions) {
            AppCard(
                onClick = {
                    onSessionClick(it.id)
                },
                appName = {
                    val formatter = DateTimeFormatter.ofPattern("EEE d")
                    Text(
                        formatter.format((it.endTime ?: it.startTime).toLocalDate())
                    )
                },
                title = {},
            ) {
                Column {
                    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
                    Text(
                        "${it.startTime.format(formatter)} - ${it.endTime?.format(formatter) ?: "Ongoing"}"
                    )
                }
            }
        }
    }
}