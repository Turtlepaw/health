package com.turtlepaw.health.apps.reflections.presentation.pages

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.ExperimentalWearFoundationApi
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.turtlepaw.health.R
import com.turtlepaw.shared.components.Page
import com.turtlepaw.shared.database.AppDatabase
import com.turtlepaw.shared.database.Reflection
import java.time.Duration
import java.time.LocalDateTime

@OptIn(ExperimentalHorologistApi::class, ExperimentalWearFoundationApi::class)
@Composable
fun WearHome(database: AppDatabase, onOpenHistory: () -> Unit, onCreateReflection: () -> Unit) {
    var latestReflection = remember { mutableStateOf<Reflection?>(null) }
    LaunchedEffect(Unit) {
        latestReflection.value = database.reflectionDao().getLatest()
    }
    Page {
        item {
            Text("Reflections")
        }
        item {
            Button(onCreateReflection, modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.sentiment_satisfied),
                        contentDescription = "Sentiment Satisfied"
                    )
                    Spacer(Modifier.width(5.dp))
                    Text("Reflect")
                }
            }
        }
        item {
            Button(onOpenHistory, modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = "Calendar Month"
                    )
                    Spacer(Modifier.width(5.dp))
                    Text("History")
                }
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
        }
        item {
            Crossfade(
                targetState = latestReflection.value,
                label = "crossfade_reflections",
                animationSpec = tween(
                    durationMillis = 400
                )
            ) { reflection ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (reflection != null) {
                        Text(
                            reflection.value.displayName + " - " + getRelativeTime(reflection.date),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text("No reflections yet")
                    }
                }
            }
        }
    }
}

fun getRelativeTime(pastTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val duration = Duration.between(pastTime, now)

    return when {
        duration.toDays() > 0 -> "${duration.toDays()} days ago"
        duration.toHours() > 0 -> "${duration.toHours()} hours ago"
        duration.toMinutes() > 0 -> "${duration.toMinutes()} minutes ago"
        else -> "just now"
    }
}
